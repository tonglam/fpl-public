package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.config.collector.ElementLiveCollector;
import com.tong.fpl.constant.enums.*;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.live.*;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutEventResultData;
import com.tong.fpl.domain.letletme.tournament.TournamentRoyaleData;
import com.tong.fpl.service.IDataService;
import com.tong.fpl.service.ILiveService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.db.EntryEventPickService;
import com.tong.fpl.service.db.EntryEventResultService;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.service.db.TournamentPointsGroupResultService;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create by tong on 2020/7/13
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveServiceImpl implements ILiveService {

    private final IQueryService queryService;
    private final IDataService dataService;

    private final EntryInfoService entryInfoService;
    private final EntryEventPickService entryEventPickService;
    private final EntryEventResultService entryEventResultService;
    private final TournamentPointsGroupResultService tournamentPointsGroupResultService;

    @Override
    public LiveCalcData calcLivePointsByEntry(int event, int entry) {
        if (event <= 0 || entry <= 0) {
            return new LiveCalcData();
        }
        // prepare
        Map<String, PlayerEntity> playerInfoMap = Maps.newHashMap();
        Map<Integer, EventLiveEntity> eventLiveMap = this.getEventLiveByEvent(event);
        Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap = this.getEventLiveFixtureMap();
        Table<Integer, Integer, Integer> liveBonusTable = this.getLiveBonusTable();
        // calc entry points
        LiveCalcData liveCalcData = this.calcLiveSingleEntryPoints(event, entry, playerInfoMap,
                teamLiveFixtureMap, Maps.newHashMap(), eventLiveMap, liveBonusTable, new ForkJoinPool(4));
        // entry info
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(entry);
        if (entryInfoData != null) {
            BeanUtil.copyProperties(entryInfoData, liveCalcData);
            liveCalcData
                    .setValue(entryInfoData.getTeamValue() / 10.0)
                    .setBank(entryInfoData.getBank() / 10.0)
                    .setTeamValue((entryInfoData.getTeamValue() - entryInfoData.getBank()) / 10.0);
        }
        // entry result
        EntryEventResultEntity lastEntryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event - 1)
                .eq(EntryEventResultEntity::getEntry, entry));
        if (lastEntryEventResultEntity != null) {
            liveCalcData.setLiveTotalPoints(lastEntryEventResultEntity.getOverallPoints() + liveCalcData.getLiveNetPoints());
        } else {
            liveCalcData.setLiveTotalPoints(liveCalcData.getLiveNetPoints());
        }
        return liveCalcData;
    }

    @Override
    public List<LiveCalcData> calcLivePointsByEntryList(int event, List<Integer> entryList) {
        if (event <= 0 || CollectionUtils.isEmpty(entryList)) {
            return Lists.newArrayList();
        }
        // prepare
        Map<String, PlayerEntity> playerInfoMap = this.queryService.getPlayerMap();
        Map<Integer, EntryEventPickEntity> entryEventPickMap = this.qryEventPickMapByEntryList(event, entryList);
        Map<Integer, EventLiveEntity> eventLiveMap = this.getEventLiveByEvent(event);
        Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap = this.getEventLiveFixtureMap();
        Table<Integer, Integer, Integer> liveBonusTable = this.getLiveBonusTable();
        // calc live entry points
        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        List<CompletableFuture<LiveCalcData>> future = entryList.stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.calcLiveSingleEntryPoints(event, o, playerInfoMap,
                        teamLiveFixtureMap, entryEventPickMap, eventLiveMap, liveBonusTable, forkJoinPool), forkJoinPool))
                .toList();
        List<LiveCalcData> liveCalcList = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        // entry result
        Map<Integer, Integer> lastOverallPointsMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                        .eq(EntryEventResultEntity::getEvent, event - 1)
                        .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, EntryEventResultEntity::getOverallPoints));
        // entry info
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
                        .in(EntryInfoEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryInfoEntity::getEntry, v -> v));
        liveCalcList.forEach(liveCalcData -> {
            int entry = liveCalcData.getEntry();
            EntryInfoEntity entryInfoEntity = entryInfoMap.getOrDefault(entry, null);
            if (entryInfoEntity != null) {
                BeanUtil.copyProperties(entryInfoEntity, liveCalcData);
                liveCalcData
                        .setLiveTotalPoints(lastOverallPointsMap.getOrDefault(entry, 0) + liveCalcData.getLiveNetPoints())
                        .setPickList(null);
            }
        });
        return liveCalcList;
    }

    private Map<Integer, EntryEventPickEntity> qryEventPickMapByEntryList(int event, List<Integer> entryList) {
        return this.queryService.qryEventPickByEntryList(event, entryList)
                .stream()
                .collect(Collectors.toMap(EntryEventPickEntity::getEntry, o -> o));
    }

    @Override
    public LiveTournamentCalcData calcLivePointsByTournament(int event, int tournamentId) {
        if (event <= 0 || tournamentId <= 0) {
            return new LiveTournamentCalcData();
        }
        // prepare
        Map<String, PlayerEntity> playerInfoMap = this.queryService.getPlayerMap();
        Map<Integer, EntryEventPickEntity> entryEventPickMap = this.qryTournamentEntryEventPickMap(event, tournamentId);
        Map<Integer, EventLiveEntity> eventLiveMap = this.getEventLiveByEvent(event);
        Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap = this.getEventLiveFixtureMap();
        Table<Integer, Integer, Integer> liveBonusTable = this.getLiveBonusTable();
        // get entry list
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // calc live entry points
        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        List<CompletableFuture<LiveCalcData>> future = entryList.stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.calcLiveSingleEntryPoints(event, o, playerInfoMap,
                        teamLiveFixtureMap, entryEventPickMap, eventLiveMap, liveBonusTable, forkJoinPool), forkJoinPool))
                .toList();
        List<LiveCalcData> liveCalcList = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        // entry result
        Map<Integer, Integer> lastOverallPointsMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                        .eq(EntryEventResultEntity::getEvent, event - 1)
                        .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, EntryEventResultEntity::getOverallPoints));
        // entry info
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
                        .in(EntryInfoEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryInfoEntity::getEntry, v -> v));
        liveCalcList.forEach(liveCalcData -> {
            int entry = liveCalcData.getEntry();
            EntryInfoEntity entryInfoEntity = entryInfoMap.getOrDefault(entry, null);
            if (entryInfoEntity != null) {
                BeanUtil.copyProperties(entryInfoEntity, liveCalcData);
                liveCalcData
                        .setLiveTotalPoints(lastOverallPointsMap.getOrDefault(entry, 0) + liveCalcData.getLiveNetPoints())
                        .setPickList(null);
            }
        });
        // tournament info
        TournamentInfoData tournamentInfoData = this.queryService.qryTournamentDataById(tournamentId);
        if (tournamentInfoData == null) {
            log.error("tournament info is null, tournamentId: {}", tournamentId);
            return new LiveTournamentCalcData();
        }
        String leagueType = tournamentInfoData.getLeagueType();
        // sort
        Map<Integer, Integer> rankMap;
        if (StringUtils.equals(LeagueType.Royale.name(), leagueType)) {
            rankMap = this.sortRoyaleTournamentLivePointsRank(liveCalcList);
        } else {
            rankMap = this.sortTournamentLivePointsRank(liveCalcList);
        }
        if (!CollectionUtils.isEmpty(rankMap)) {
            liveCalcList.forEach(o -> o.setRank(rankMap.get(o.getLivePoints())));
        }
        // data
        LiveTournamentCalcData data = new LiveTournamentCalcData()
                .setLiveCalcDataList(liveCalcList)
                .setLeagueType(leagueType)
                .setEventEliminatedList(Lists.newArrayList())
                .setWaitingEliminatedList(Lists.newArrayList())
                .setEliminatedList(Lists.newArrayList());
        // not royale
        if (!StringUtils.equalsIgnoreCase(LeagueType.Royale.name(), leagueType)) {
            return data;
        }
        // get tournament_royale data
        TournamentRoyaleData tournamentRoyaleData = this.queryService.qryEventTournamentRoyale(event, tournamentId);
        if (tournamentRoyaleData == null) {
            return data;
        }
        return this.calcLivePointsByRoyaleTournament(event, liveCalcList, tournamentInfoData, tournamentRoyaleData);
    }

    private LiveTournamentCalcData calcLivePointsByRoyaleTournament(int event, List<LiveCalcData> liveCalcList, TournamentInfoData tournamentInfoData, TournamentRoyaleData tournamentRoyaleData) {
        // live_calc_list
        List<LiveCalcData> list = liveCalcList
                .stream()
                .sorted(Comparator.comparing(LiveCalcData::getRank))
                .toList();
        // get the waiting eliminated list
        List<LiveCalcData> waitingEliminatedList = this.getEliminatedEntryListFromText(tournamentRoyaleData.getWaitingEliminatedEntries(), liveCalcList);
        List<LiveCalcData> eliminatedList = this.getEliminatedEntryListFromText(tournamentRoyaleData.getAllEliminatedEntries(), liveCalcList);
        List<Integer> eventEliminatedList = this.calcEliminateList(event, tournamentInfoData, waitingEliminatedList, eliminatedList, liveCalcList);
        // remove eliminated entry
        if (!CollectionUtils.isEmpty(eliminatedList)) {
            liveCalcList = list.stream().filter(o -> !eliminatedList.contains(o)).collect(Collectors.toList());
        }
        List<LiveCalcData> restLiveCalcList = liveCalcList
                .stream()
                .filter(o -> !(waitingEliminatedList.contains(o) && eventEliminatedList.contains(o.getEntry()))
                        && !eliminatedList.contains(o))
                .collect(Collectors.toList());
        // return
        return new LiveTournamentCalcData()
                .setLiveCalcDataList(restLiveCalcList)
                .setLeagueType(tournamentInfoData.getLeagueType())
                .setEventEliminatedNum(tournamentRoyaleData.getEventEliminatedNum())
                .setEventEliminatedList(eventEliminatedList)
                .setWaitingEliminatedList(waitingEliminatedList)
                .setEliminatedList(eliminatedList);
    }


    private List<Integer> calcEliminateList(int event, TournamentInfoData tournamentInfoData,
                                            List<LiveCalcData> waitingEliminatedList, List<LiveCalcData> eliminatedList,
                                            List<LiveCalcData> liveCalcDataList) {
        List<Integer> waitingEliminatedEntryList = waitingEliminatedList.stream().map(LiveCalcData::getEntry).collect(Collectors.toList());
        // exclude eliminated list
        liveCalcDataList = liveCalcDataList.stream().filter(o -> !eliminatedList.contains(o)).toList();
        int startEvent = Integer.parseInt(tournamentInfoData.getGroupStartGw());
        int endEvent = Integer.parseInt(tournamentInfoData.getGroupEndGw());
        if (event < startEvent || event > endEvent) {
            return Lists.newArrayList();
        }
        // make every two events as a round, and each round will eliminate two entries
        boolean settleEvent = (event - startEvent + 1) % 2 == 0;
        // if an event is not a settle event, it will eliminate 0 or 1 entry, depending on whether two or more entries score the same event points
        // if an event is a settle event, it will eliminate accurately 2 entries
        TournamentRoyaleData lastTournamentRoyaleData = this.queryService.qryEventTournamentRoyale(event - 1, tournamentInfoData.getId());
        if (lastTournamentRoyaleData == null) {
            return Lists.newArrayList();
        }
        if (settleEvent) {
            return this.getLiveEventElimatedList(waitingEliminatedEntryList, liveCalcDataList, 2 - lastTournamentRoyaleData.getEventEliminatedNum());
        } else {
            int minEventNetPoints = liveCalcDataList.stream().mapToInt(LiveCalcData::getLiveNetPoints).min().orElse(0);
            int minNums = (int) liveCalcDataList.stream().filter(o -> o.getLiveNetPoints() == minEventNetPoints).count();
            if (minNums > 1) {
                return Lists.newArrayList();
            } else {
                return this.getLiveEventElimatedList(waitingEliminatedEntryList, liveCalcDataList, 1);
            }
        }
    }

    private List<Integer> getLiveEventElimatedList(List<Integer> waitingEliminatedList, List<LiveCalcData> liveCalcDataList, int eliminatedNum) {
        int restNum = eliminatedNum;
        List<Integer> eventEliminatedList = Lists.newArrayList();
        // reOrder
        List<Integer> liveCalcList = this.reOrderEliminateList(liveCalcDataList);
        // eliminate from waiting list
        if (!CollectionUtils.isEmpty(waitingEliminatedList)) {
            restNum -= 1;
            List<Integer> currentWaitingEliminatedList = liveCalcList
                    .stream()
                    .filter(waitingEliminatedList::contains)
                    .limit(1)
                    .toList();
            if (!CollectionUtils.isEmpty(currentWaitingEliminatedList)) {
                eventEliminatedList.addAll(currentWaitingEliminatedList);
            }
            liveCalcList = liveCalcList.stream().filter(o -> !waitingEliminatedList.contains(o)).toList();
        }
        if (restNum > 0) {
            List<Integer> currentEliminatedList = liveCalcList
                    .stream()
                    .limit(restNum)
                    .toList();
            if (!CollectionUtils.isEmpty(currentEliminatedList)) {
                eventEliminatedList.addAll(currentEliminatedList);
            }
        }
        return eventEliminatedList;
    }

    private List<Integer> reOrderEliminateList(List<LiveCalcData> list) {
        // live_net_points asc -> over_all_rank desc -> entry asc
        return list
                .stream()
                .sorted(Comparator.comparing(LiveCalcData::getLiveNetPoints)
                        .thenComparing(Comparator.comparing(LiveCalcData::getOverallRank).reversed())
                        .thenComparing(LiveCalcData::getEntry))
                .map(LiveCalcData::getEntry)
                .collect(Collectors.toList());
    }

    private List<LiveCalcData> getEliminatedEntryListFromText(String eliminatedEntries, List<LiveCalcData> liveCalcDataList) {
        if (StringUtils.isBlank(eliminatedEntries)) {
            return Lists.newArrayList();
        }
        List<Integer> entryList = Stream.of(eliminatedEntries.split(",")).map(Integer::valueOf).toList();
        return liveCalcDataList
                .stream()
                .filter(o -> entryList.contains(o.getEntry()))
                .sorted(Comparator.comparing(LiveCalcData::getLiveNetPoints))
                .collect(Collectors.toList());
    }

    private Map<Integer, Integer> sortTournamentLivePointsRank(List<LiveCalcData> liveCalcDataList) {
        Map<Integer, Integer> rankMap = Maps.newHashMap();
        Map<Integer, Integer> rankCountMap = Maps.newLinkedHashMap();
        liveCalcDataList
                .stream()
                .sorted(Comparator.comparing(LiveCalcData::getLivePoints).reversed())
                .forEachOrdered(o -> {
                    int key = o.getLivePoints();
                    if (rankCountMap.containsKey(key)) {
                        rankCountMap.put(key, rankCountMap.get(key) + 1);
                    } else {
                        rankCountMap.put(key, 1);
                    }
                });
        int index = 1;
        for (int key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    // Royale: 1. net_points; 2. overall_points; 3. entry
    private Map<Integer, Integer> sortRoyaleTournamentLivePointsRank(List<LiveCalcData> liveCalcDataList) {
        Map<Integer, Integer> rankMap = Maps.newHashMap();
        Map<Integer, Integer> rankCountMap = Maps.newLinkedHashMap();
        liveCalcDataList
                .stream()
                .sorted(
                        Comparator.comparing(LiveCalcData::getLivePoints).reversed()
                                .thenComparing(LiveCalcData::getOverallRank)
                                .thenComparing(LiveCalcData::getEntry)
                )
                .forEachOrdered(o -> {
                    int key = o.getLivePoints();
                    if (rankCountMap.containsKey(key)) {
                        rankCountMap.put(key, rankCountMap.get(key) + 1);
                    } else {
                        rankCountMap.put(key, 1);
                    }
                });
        int index = 1;
        for (int key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    private Map<Integer, Integer> sortTournamentLiveNetPointsRank(List<LiveCalcData> liveCalcDataList) {
        Map<Integer, Integer> rankMap = Maps.newHashMap();
        Map<Integer, Integer> rankCountMap = Maps.newLinkedHashMap();
        liveCalcDataList
                .stream()
                .sorted(Comparator.comparing(LiveCalcData::getLiveNetPoints).reversed())
                .forEachOrdered(o -> {
                    int key = o.getLiveNetPoints();
                    if (rankCountMap.containsKey(key)) {
                        rankCountMap.put(key, rankCountMap.get(key) + 1);
                    } else {
                        rankCountMap.put(key, 1);
                    }
                });
        int index = 1;
        for (int key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    private Map<Integer, Integer> sortTournamentLiveTotalPointsRank(List<LiveCalcData> liveCalcDataList) {
        Map<Integer, Integer> rankMap = Maps.newHashMap();
        Map<Integer, Integer> rankCountMap = Maps.newLinkedHashMap();
        liveCalcDataList
                .stream()
                .sorted(Comparator.comparing(LiveCalcData::getLiveTotalPoints).reversed())
                .forEachOrdered(o -> {
                    int key = o.getLiveTotalPoints();
                    if (rankCountMap.containsKey(key)) {
                        rankCountMap.put(key, rankCountMap.get(key) + 1);
                    } else {
                        rankCountMap.put(key, 1);
                    }
                });
        int index = 1;
        for (int key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    @Override
    public SearchLiveTournamentCalcData calcSearchLivePointsByTournament(LiveCalcSearchParamData liveCalcSearchParamData) {
        int event = liveCalcSearchParamData.getEvent();
        int tournamentId = liveCalcSearchParamData.getTournamentId();
        List<Integer> elementList = liveCalcSearchParamData.getElementList();
        if (event <= 0 || tournamentId <= 0 || CollectionUtils.isEmpty(elementList)) {
            return new SearchLiveTournamentCalcData();
        }
        // prepare
        Map<String, PlayerEntity> playerInfoMap = this.queryService.getPlayerMap();
        Map<Integer, EntryEventPickEntity> entryEventPickMap = this.qryTournamentEntryEventPickMap(event, tournamentId);
        Map<Integer, EventLiveEntity> eventLiveMap = this.getEventLiveByEvent(event);
        Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap = this.getEventLiveFixtureMap();
        Table<Integer, Integer, Integer> liveBonusTable = this.getLiveBonusTable();
        // get entry list
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // calc live entry points
        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        List<CompletableFuture<LiveCalcData>> future = entryList.stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.calcLiveSingleEntryPoints(event, o, playerInfoMap,
                        teamLiveFixtureMap, entryEventPickMap, eventLiveMap, liveBonusTable, forkJoinPool), forkJoinPool))
                .toList();
        List<LiveCalcData> liveCalcList = future
                .stream()
                .map(CompletableFuture::join)
                .filter(o -> this.containSearchElementList(elementList, o.getPickList(), o.getChip(), liveCalcSearchParamData.isLineup()))
                .collect(Collectors.toList());
        // entry result
        Map<Integer, Integer> lastOverallPointsMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                        .eq(EntryEventResultEntity::getEvent, event - 1)
                        .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, EntryEventResultEntity::getOverallPoints));
        // entry info
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list()
                .stream()
                .collect(Collectors.toMap(EntryInfoEntity::getEntry, v -> v));
        liveCalcList.forEach(liveCalcData -> {
            int entry = liveCalcData.getEntry();
            EntryInfoEntity entryInfoEntity = entryInfoMap.getOrDefault(entry, null);
            if (entryInfoEntity != null) {
                BeanUtil.copyProperties(entryInfoEntity, liveCalcData);
                liveCalcData.setLiveTotalPoints(lastOverallPointsMap.getOrDefault(entry, 0) + liveCalcData.getLiveNetPoints());
            }
        });
        // tournament info
        TournamentInfoData tournamentInfoData = this.queryService.qryTournamentDataById(tournamentId);
        if (tournamentInfoData == null) {
            log.error("tournament info is null, tournamentId: {}", tournamentId);
            return new SearchLiveTournamentCalcData();
        }
        String leagueType = tournamentInfoData.getLeagueType();
        // sort
        Map<Integer, Integer> rankMap;
        if (StringUtils.equals(LeagueType.Royale.name(), leagueType)) {
            rankMap = this.sortRoyaleTournamentLivePointsRank(liveCalcList);
        } else {
            rankMap = this.sortTournamentLivePointsRank(liveCalcList);
        }
        if (!CollectionUtils.isEmpty(rankMap)) {
            liveCalcList.forEach(o -> o.setRank(rankMap.get(o.getLivePoints())));
        }
        List<LiveCalcData> list = liveCalcList
                .stream()
                .sorted(Comparator.comparing(LiveCalcData::getRank))
                .toList();
        list.forEach(o -> o.setPickList(null));
        // web_list
        List<String> webList = elementList
                .stream()
                .map(o -> playerInfoMap.get(String.valueOf(o)).getWebName())
                .collect(Collectors.toList());
        return new SearchLiveTournamentCalcData()
                .setWebNameList(webList)
                .setSelectNum(list.size())
                .setSelectByPercent(list.isEmpty() ? "0%" : CommonUtils.getPercentResult(list.size(), entryList.size()))
                .setLiveCalcDataList(liveCalcList);
    }

    private boolean containSearchElementList(List<Integer> elementList, List<ElementEventResultData> pickList, String chip, boolean lineup) {
        for (Integer element :
                elementList) {
            boolean contain = this.containSearchElement(element, pickList, chip, lineup);
            if (!contain) {
                return false;
            }
        }
        return true;
    }

    private boolean containSearchElement(int element, List<ElementEventResultData> pickList, String chip, boolean lineup) {
        for (ElementEventResultData data :
                pickList) {
            if (lineup && !StringUtils.equalsIgnoreCase(Chip.BB.getValue(), chip)) {
                if (data.getPosition() > 12) {
                    continue;
                }
            }
            if (data.getElement() == element) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<LiveKnockoutResultData> calcLivePointsByKnockout(int event, int tournamentId) {
        if (event <= 0 || tournamentId <= 0) {
            return Lists.newArrayList();
        }
        // prepare
        Map<String, PlayerEntity> playerInfoMap = this.queryService.getPlayerMap();
        Map<Integer, EntryEventPickEntity> entryEventPickMap = this.qryTournamentEntryEventPickMap(event, tournamentId);
        Map<Integer, EventLiveEntity> eventLiveMap = this.getEventLiveByEvent(event);
        Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap = this.getEventLiveFixtureMap();
        Table<Integer, Integer, Integer> liveBonusTable = this.getLiveBonusTable();
        // get entry list
        List<Integer> entryList = this.queryService.qryEntryListByKnockout(tournamentId, event);
        // calc live entry points
        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        List<CompletableFuture<LiveCalcData>> future = entryList.stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.calcLiveSingleEntryPoints(event, o, playerInfoMap,
                        teamLiveFixtureMap, entryEventPickMap, eventLiveMap, liveBonusTable, forkJoinPool), forkJoinPool))
                .toList();
        List<LiveCalcData> liveCalcList = future
                .stream()
                .map(CompletableFuture::join)
                .toList();
        // entry info
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
                        .in(EntryInfoEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryInfoEntity::getEntry, v -> v));
        Map<Integer, LiveCalcData> liveCalcMap = Maps.newHashMap(); // entry -> live_calc_data
        liveCalcList.forEach(liveCalcData -> {
            int entry = liveCalcData.getEntry();
            EntryInfoEntity entryInfoEntity = entryInfoMap.getOrDefault(entry, null);
            if (entryInfoEntity != null) {
                BeanUtil.copyProperties(entryInfoEntity, liveCalcData);
                liveCalcData
                        .setLiveTotalPoints(0)
                        .setPickList(null);
            }
            liveCalcMap.put(entry, liveCalcData);
        });
        // tournament_info
        TournamentInfoData tournamentInfoData = this.queryService.qryTournamentDataById(tournamentId);
        if (tournamentInfoData == null) {
            log.error("tournament info is null, tournamentId: {}", tournamentId);
            return Lists.newArrayList();
        }
        int playAgainstNum = tournamentInfoData.getKnockoutPlayAgainstNum();
        // knockout_info
        Map<Integer, TournamentKnockoutEntity> liveTournamentKnockoutMap = Maps.newHashMap(); // match_id -> tournament_knockout
        this.queryService.qryKnockoutMapByTournament(tournamentId, event)
                .forEach((k, v) -> liveTournamentKnockoutMap.put(Integer.parseInt(k), v));
        if (CollectionUtils.isEmpty(liveTournamentKnockoutMap)) {
            return Lists.newArrayList();
        }
        int round = liveTournamentKnockoutMap.values().stream().findFirst().map(TournamentKnockoutEntity::getRound).orElse(0);
        // tournament_knockout_result
        Map<Integer, List<TournamentKnockoutEventResultData>> tournamentKnockoutMap = Maps.newHashMap(); // match_id -> tournament_knockout_result
        this.queryService.qryKnockoutRoundMapByTournament(tournamentId, round, event - 1)
                .forEach((k, v) -> tournamentKnockoutMap.put(Integer.parseInt(k), v));
        // collect data
        List<LiveKnockoutResultData> list = Lists.newArrayList();
        liveTournamentKnockoutMap.keySet().forEach(matchId -> {
            TournamentKnockoutEntity tournamentKnockoutEntity = liveTournamentKnockoutMap.get(matchId);
            if (tournamentKnockoutEntity == null) {
                return;
            }
            int homeEntry = tournamentKnockoutEntity.getHomeEntry();
            LiveCalcData homeLiveData = liveCalcMap.getOrDefault(homeEntry, null);
            if (homeLiveData == null) {
                return;
            }
            String homeEntryName = homeLiveData.getEntryName();
            String homePlayerName = homeLiveData.getPlayerName();
            int homeEntryNetPoints = homeLiveData.getLiveNetPoints();
            int awayEntry = tournamentKnockoutEntity.getAwayEntry();
            LiveCalcData awayLiveData = liveCalcMap.getOrDefault(awayEntry, null);
            if (awayLiveData == null) {
                return;
            }
            String awayEntryName = awayLiveData.getEntryName();
            String awayPlayerName = awayLiveData.getPlayerName();
            int awayEntryNetPoints = awayLiveData.getLiveNetPoints();
            List<TournamentKnockoutEventResultData> liveAgainstDataList = tournamentKnockoutMap.getOrDefault(matchId, Lists.newArrayList());
            if (!CollectionUtils.isEmpty(liveAgainstDataList)) {
                liveAgainstDataList.forEach(o -> {
                    LiveCalcData homeData = liveCalcMap.getOrDefault(o.getHomeEntry(), new LiveCalcData());
                    LiveCalcData awayData = liveCalcMap.getOrDefault(o.getAwayEntry(), new LiveCalcData());
                    o
                            .setHomeEntryName(homeData.getEntryName())
                            .setHomePlayerName(homeData.getPlayerName())
                            .setAwayEntryName(awayData.getEntryName())
                            .setAwayPlayerName(awayData.getPlayerName());
                });
            }
            TournamentKnockoutEventResultData liveAgainstCalcData = BeanUtil.copyProperties(tournamentKnockoutEntity, TournamentKnockoutEventResultData.class);
            liveAgainstCalcData
                    .setEvent(event)
                    .setPlayAgainstId(liveAgainstDataList.size() + 1)
                    .setHomeEntry(homeEntry)
                    .setHomeEntryName(homeEntryName)
                    .setHomePlayerName(homePlayerName)
                    .setHomeEntryNetPoints(homeEntryNetPoints)
                    .setAwayEntry(awayEntry)
                    .setAwayEntryName(awayEntryName)
                    .setAwayPlayerName(awayPlayerName)
                    .setAwayEntryNetPoints(awayEntryNetPoints)
                    .setLive(true);
            // net_points
            int liveWinnerEntry = this.calcLiveKnockoutMatchWinner(liveAgainstCalcData.getHomeEntry(), liveAgainstCalcData.getAwayEntry(), liveAgainstCalcData.getHomeEntryNetPoints(), liveAgainstCalcData.getAwayEntryNetPoints());
            liveAgainstCalcData.setMatchWinner(liveWinnerEntry);
            liveAgainstDataList.add(liveAgainstCalcData);
            LiveKnockoutResultData data = new LiveKnockoutResultData()
                    .setRound(round)
                    .setMatchId(matchId)
                    .setNextMatchId(tournamentKnockoutEntity.getNextMatchId())
                    .setHomeEntry(homeEntry)
                    .setHomeEntryName(homeEntryName)
                    .setHomePlayerName(homePlayerName)
                    .setAwayEntry(awayEntry)
                    .setAwayEntryName(awayEntryName)
                    .setAwayPlayerName(awayPlayerName)
                    .setLiveAgainstDataList(liveAgainstDataList.stream().sorted(Comparator.comparing(TournamentKnockoutEventResultData::getEvent)).toList())
                    .setRoundWinner(this.calcLiveKnockoutRoundWinner(liveAgainstDataList, playAgainstNum));
            // next opponent entry names
            int nextMatchId = tournamentKnockoutEntity.getNextMatchId();
            TournamentKnockoutEntity nextTournamentKnockoutEntity = liveTournamentKnockoutMap.values().stream().filter(o -> o.getNextMatchId() == nextMatchId && !o.getMatchId().equals(matchId)).findFirst().orElse(null);
            if (nextTournamentKnockoutEntity != null) {
                int nextPossibleHomeEntry = nextTournamentKnockoutEntity.getHomeEntry();
                int nextPossibleAwayEntry = nextTournamentKnockoutEntity.getAwayEntry();
                String nextOpponents = "[" + liveCalcMap.getOrDefault(nextPossibleHomeEntry, new LiveCalcData()).getEntryName() + "]"
                        + " or "
                        + "[" + liveCalcMap.getOrDefault(nextPossibleAwayEntry, new LiveCalcData()).getEntryName() + "]";
                data.setNextOpponents(nextOpponents);
            }
            list.add(data);
        });
        return list;
    }

    private int calcLiveKnockoutMatchWinner(int homeEntry, int awayEntry, int homeEntryNetPoints, int awayEntryNetPoints) {
        if (homeEntryNetPoints > awayEntryNetPoints) {
            return homeEntry;
        } else if (homeEntryNetPoints < awayEntryNetPoints) {
            return awayEntry;
        }
        return 0;
    }

    private int calcLiveKnockoutRoundWinner(List<TournamentKnockoutEventResultData> liveDataList, int playAgainstNum) {
        List<Integer> winnerEntryList = liveDataList
                .stream()
                .map(TournamentKnockoutEventResultData::getMatchWinner)
                .filter(o -> o > 0)
                .toList();
        if (winnerEntryList.stream().distinct().toList().size() == 1) {
            return winnerEntryList.get(0); // win all the games
        }
        int winnerTimes = playAgainstNum % 2 + 1;
        int firstWinner = winnerEntryList.get(0);
        int firstWinnerTimes = Collections.frequency(winnerEntryList, firstWinner);
        if (firstWinnerTimes == winnerTimes) {
            return firstWinner;
        }
        List<Integer> restList = winnerEntryList.stream().filter(o -> o != firstWinner).toList();
        int secondWinner = restList.get(0);
        int secondWinnerTimes = Collections.frequency(restList, secondWinner);
        if (secondWinnerTimes == winnerTimes) {
            return secondWinner;
        }
        return 0;
    }

    @Override
    public LiveCalcData calcLivePointsByElementList(LiveCalcParamData liveCalcParamData) {
        int event = liveCalcParamData.getEvent();
        if (event <= 0 || CollectionUtils.isEmpty(liveCalcParamData.getElementMap())) {
            return new LiveCalcData();
        }
        // prepare
        Map<String, PlayerEntity> playerInfoMap = Maps.newHashMap();
        Map<Integer, EventLiveEntity> eventLiveMap = this.getEventLiveByEvent(event);
        Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap = this.getEventLiveFixtureMap();
        Table<Integer, Integer, Integer> liveBonusTable = this.getLiveBonusTable();
        // init user pick from elementMap
        int captain = liveCalcParamData.getCaptain();
        int viceCaptain = liveCalcParamData.getViceCaptain();
        List<Pick> picks = Lists.newArrayList();
        liveCalcParamData.getElementMap().forEach((position, element) ->
                picks.add(
                        new Pick()
                                .setElement(element)
                                .setPosition(position)
                                .setCaptain(element == captain)
                                .setViceCaptain(element == viceCaptain)
                )
        );
        // initialize element_live_data, static part
        List<ElementEventResultData> elementEventResultDataList = this.qryEntryLiveStaticData(event, picks, playerInfoMap, teamLiveFixtureMap, new ForkJoinPool(4));
        // initialize element_live_data, event_live part
        this.initEventLiveData(elementEventResultDataList, eventLiveMap, liveBonusTable);
        // get active picks
        List<ElementEventResultData> pickList = this.getPickList(elementEventResultDataList);
        // calc live points
        String chip = liveCalcParamData.getChip();
        int livePoints = this.calcActivePoints(Chip.getChipFromValue(chip), pickList);
        return new LiveCalcData()
                .setEntry(0)
                .setEvent(event)
                .setPickList(pickList)
                .setChip(chip)
                .setLiveTotalPoints(livePoints)
                .setLivePoints(livePoints)
                .setTransferCost(0)
                .setLiveNetPoints(livePoints);
    }

    @Override
    public List<LiveCalcData> calcLiveGwPointsByChampionLeague(int event, int tournamentId, String stage) {
        if (event <= 0 || tournamentId <= 0 || StringUtils.isEmpty(stage)) {
            return Lists.newArrayList();
        }
        // prepare
        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        Map<String, PlayerEntity> playerInfoMap = this.queryService.getPlayerMap();
        Map<Integer, EntryEventPickEntity> entryEventPickMap = this.qryTournamentEntryEventPickMap(event, tournamentId);
        Map<Integer, EventLiveEntity> eventLiveMap = this.getEventLiveByEvent(event);
        Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap = this.getEventLiveFixtureMap();
        Table<Integer, Integer, Integer> liveBonusTable = this.getLiveBonusTable();
        // get tournament info
        TournamentInfoData tournamentInfoData = this.queryService.qryTournamentDataById(tournamentId);
        if (tournamentInfoData == null) {
            return Lists.newArrayList();
        }
        int groupStartGw = Integer.parseInt(tournamentInfoData.getGroupStartGw());
        int groupEndGw = Integer.parseInt(tournamentInfoData.getGroupEndGw());
        int knockoutStartGw = Integer.parseInt(tournamentInfoData.getKnockoutStartGw());
        int knockoutEndGw = Integer.parseInt(tournamentInfoData.getKnockoutEndGw());
        // get entry list
        List<Integer> entryList = Lists.newArrayList();
        if (stage.startsWith("小组赛")) {
            String groupName = StringUtils.substringBetween(stage, "-", "组");
            entryList = this.queryService.qryEntryListByChampionLeagueGroup(tournamentId, groupName);
        } else if (stage.startsWith("淘汰赛")) {
            int round = Integer.parseInt(StringUtils.substringBetween(stage, "第", "轮"));
            entryList = this.queryService.qryEntryListByChampionLeagueKnockout(tournamentId, round);
        }
        // get calc event
        if (event < groupStartGw) {
            event = 0;
        } else if (event > groupEndGw && event < knockoutStartGw) {
            event = groupEndGw;
        } else if (event > knockoutEndGw) {
            event = knockoutEndGw;
        }
        int calcEvent = event;
        if (calcEvent == 0) {
            List<CompletableFuture<LiveCalcData>> future = entryList.stream()
                    .map(o -> CompletableFuture.supplyAsync(() -> this.getLivePointsByChampionLeagueWithoutPoints(o), forkJoinPool))
                    .toList();
            return future
                    .stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        }
        // calc live entry points
        List<CompletableFuture<LiveCalcData>> future = entryList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.calcLiveSingleEntryPoints(calcEvent, o, playerInfoMap,
                        teamLiveFixtureMap, entryEventPickMap, eventLiveMap, liveBonusTable, forkJoinPool), forkJoinPool))
                .toList();
        List<LiveCalcData> liveCalcList = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        // entry info
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
                        .in(EntryInfoEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryInfoEntity::getEntry, v -> v));
        liveCalcList.forEach(liveCalcData -> {
            int entry = liveCalcData.getEntry();
            EntryInfoEntity entryInfoEntity = entryInfoMap.getOrDefault(entry, null);
            if (entryInfoEntity != null) {
                BeanUtil.copyProperties(entryInfoEntity, liveCalcData);
                liveCalcData
                        .setLiveTotalPoints(0)
                        .setPickList(null);
            }
        });
        // sort
        Map<Integer, Integer> rankMap = this.sortTournamentLiveNetPointsRank(liveCalcList);
        if (!CollectionUtils.isEmpty(rankMap)) {
            liveCalcList.forEach(o -> o.setRank(rankMap.get(o.getLiveNetPoints())));
        }
        return liveCalcList
                .stream()
                .sorted(Comparator.comparing(LiveCalcData::getRank))
                .collect(Collectors.toList());
    }

    @Override
    public List<LiveCalcData> calcLiveTotalPointsByChampionLeague(int event, int tournamentId, String stage) {
        if (event <= 0 || tournamentId <= 0 || StringUtils.isEmpty(stage)) {
            return Lists.newArrayList();
        }
        // prepare
        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        Map<String, PlayerEntity> playerInfoMap = this.queryService.getPlayerMap();
        Map<Integer, EntryEventPickEntity> entryEventPickMap = this.qryTournamentEntryEventPickMap(event, tournamentId);
        Map<Integer, EventLiveEntity> eventLiveMap = this.getEventLiveByEvent(event);
        Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap = this.getEventLiveFixtureMap();
        Table<Integer, Integer, Integer> liveBonusTable = this.getLiveBonusTable();
        Map<Integer, Integer> groupTotalPointsMap = this.getGroupTotalPointsMap(event, tournamentId);
        // get tournament info
        TournamentInfoData tournamentInfoData = this.queryService.qryTournamentDataById(tournamentId);
        if (tournamentInfoData == null) {
            return Lists.newArrayList();
        }
        int groupStartGw = Integer.parseInt(tournamentInfoData.getGroupStartGw());
        int groupEndGw = Integer.parseInt(tournamentInfoData.getGroupEndGw());
        int knockoutStartGw = Integer.parseInt(tournamentInfoData.getKnockoutStartGw());
        int knockoutEndGw = Integer.parseInt(tournamentInfoData.getKnockoutEndGw());
        // get entry list
        List<Integer> entryList = Lists.newArrayList();
        if (stage.startsWith("小组赛")) {
            String groupName = StringUtils.substringBetween(stage, "-", "组");
            entryList = this.queryService.qryEntryListByChampionLeagueGroup(tournamentId, groupName);
        } else if (stage.startsWith("淘汰赛")) {
            int round = Integer.parseInt(StringUtils.substringBetween(stage, "第", "轮"));
            entryList = this.queryService.qryEntryListByChampionLeagueKnockout(tournamentId, round);
        }
        // get calc event
        if (event < groupStartGw) {
            event = 0;
        } else if (event > groupEndGw && event < knockoutStartGw) {
            event = groupEndGw;
        } else if (event > knockoutEndGw) {
            event = knockoutEndGw;
        }
        int calcEvent = event;
        if (calcEvent == 0) {
            List<CompletableFuture<LiveCalcData>> future = entryList.stream()
                    .map(o -> CompletableFuture.supplyAsync(() -> this.getLivePointsByChampionLeagueWithoutPoints(o), forkJoinPool))
                    .toList();
            return future
                    .stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());
        }
        // calc live entry points
        List<CompletableFuture<LiveCalcData>> future = entryList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.calcLiveSingleEntryPoints(calcEvent, o, playerInfoMap,
                        teamLiveFixtureMap, entryEventPickMap, eventLiveMap, liveBonusTable, forkJoinPool), forkJoinPool))
                .toList();
        List<LiveCalcData> liveCalcList = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        // entry info
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
                        .in(EntryInfoEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryInfoEntity::getEntry, v -> v));
        liveCalcList.forEach(liveCalcData -> {
            int entry = liveCalcData.getEntry();
            EntryInfoEntity entryInfoEntity = entryInfoMap.getOrDefault(entry, null);
            if (entryInfoEntity != null) {
                BeanUtil.copyProperties(entryInfoEntity, liveCalcData);
                liveCalcData
                        .setLiveTotalPoints(0)
                        .setPickList(null);
            }
            // total points
            if (stage.startsWith("小组赛")) {
                liveCalcData.setLiveTotalPoints(groupTotalPointsMap.getOrDefault(entry, 0) + liveCalcData.getLiveNetPoints());
            }
        });
        // sort
        Map<Integer, Integer> rankMap = this.sortTournamentLiveTotalPointsRank(liveCalcList);
        if (!CollectionUtils.isEmpty(rankMap)) {
            liveCalcList.forEach(o -> o.setRank(rankMap.get(o.getLiveTotalPoints())));
        }
        return liveCalcList
                .stream()
                .sorted(Comparator.comparing(LiveCalcData::getRank))
                .collect(Collectors.toList());
    }

    private Map<Integer, Integer> getGroupTotalPointsMap(int event, int tournamentId) {
        Map<Integer, Integer> map = Maps.newHashMap();
        Multimap<Integer, TournamentPointsGroupResultEntity> multimap = HashMultimap.create();
        this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                        .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                        .lt(TournamentPointsGroupResultEntity::getEvent, event))
                .forEach(o -> multimap.put(o.getEntry(), o));
        multimap.keySet().forEach(entry -> {
            int totalPoints = multimap.get(entry)
                    .stream()
                    .mapToInt(TournamentPointsGroupResultEntity::getEventNetPoints)
                    .sum();
            map.put(entry, totalPoints);
        });
        return map;
    }

    private LiveCalcData getLivePointsByChampionLeagueWithoutPoints(int entry) {
        if (entry <= 0) {
            return new LiveCalcData()
                    .setRank(0)
                    .setEvent(0)
                    .setEntry(entry)
                    .setEntryName("轮空")
                    .setPlayerName("轮空")
                    .setRegion("")
                    .setStartedEvent(0)
                    .setOverallPoints(0)
                    .setOverallRank(0)
                    .setValue(0)
                    .setBank(0)
                    .setTeamValue(0)
                    .setTotalTransfers(0)
                    .setLastOverallPoints(0)
                    .setLastOverallRank(0)
                    .setLastValue(0)
                    .setChip("")
                    .setLivePoints(0)
                    .setTransferCost(0)
                    .setLiveNetPoints(0)
                    .setLiveTotalPoints(0)
                    .setPlayed(0)
                    .setToPlay(0)
                    .setPlayedCaptain(0)
                    .setCaptainName("")
                    .setPickList(Lists.newArrayList())
                    .setTransfersList(Lists.newArrayList());
        }
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(entry);
        if (entryInfoData == null) {
            return new LiveCalcData()
                    .setRank(0)
                    .setEvent(0)
                    .setEntry(entry)
                    .setEntryName("")
                    .setPlayerName("")
                    .setRegion("")
                    .setStartedEvent(0)
                    .setOverallPoints(0)
                    .setOverallRank(0)
                    .setValue(0)
                    .setBank(0)
                    .setTeamValue(0)
                    .setTotalTransfers(0)
                    .setLastOverallPoints(0)
                    .setLastOverallRank(0)
                    .setLastValue(0)
                    .setChip("")
                    .setLivePoints(0)
                    .setTransferCost(0)
                    .setLiveNetPoints(0)
                    .setLiveTotalPoints(0)
                    .setPlayed(0)
                    .setToPlay(0)
                    .setPlayedCaptain(0)
                    .setCaptainName("")
                    .setPickList(Lists.newArrayList())
                    .setTransfersList(Lists.newArrayList());
        }
        return new LiveCalcData()
                .setRank(0)
                .setEvent(0)
                .setEntry(entry)
                .setEntryName(entryInfoData.getEntryName())
                .setPlayerName(entryInfoData.getPlayerName())
                .setRegion(entryInfoData.getRegion())
                .setStartedEvent(entryInfoData.getStartedEvent())
                .setOverallPoints(entryInfoData.getOverallPoints())
                .setOverallRank(entryInfoData.getOverallRank())
                .setValue(entryInfoData.getValue())
                .setBank(entryInfoData.getBank())
                .setTeamValue(entryInfoData.getTeamValue())
                .setTotalTransfers(entryInfoData.getTotalTransfers())
                .setLastOverallPoints(0)
                .setLastOverallRank(0)
                .setLastValue(0)
                .setChip("")
                .setLivePoints(0)
                .setTransferCost(0)
                .setLiveNetPoints(0)
                .setLiveTotalPoints(0)
                .setPlayed(0)
                .setToPlay(0)
                .setPlayedCaptain(0)
                .setCaptainName("")
                .setPickList(Lists.newArrayList())
                .setTransfersList(Lists.newArrayList());
    }

    private Map<Integer, EntryEventPickEntity> qryTournamentEntryEventPickMap(int event, int tournamentId) {
        return this.queryService.qryTournamentEntryEventPick(event, tournamentId)
                .stream()
                .collect(Collectors.toMap(EntryEventPickEntity::getEntry, o -> o));
    }

    private Map<Integer, EventLiveEntity> getEventLiveByEvent(int event) {
        Map<Integer, EventLiveEntity> map = Maps.newHashMap();
        this.queryService.getEventLiveByEvent(event).forEach((k, v) -> map.put(Integer.valueOf(k), v));
        return map;
    }

    private Map<Integer, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap() {
        Map<Integer, Map<String, List<LiveFixtureData>>> map = Maps.newHashMap(); // key:teamId -> value:(key:status -> value:liveFixtureData)
        this.queryService.getEventLiveFixtureMap().forEach((k, v) -> map.put(Integer.valueOf(k), v));
        return map;
    }

    private Table<Integer, Integer, Integer> getLiveBonusTable() {
        Table<Integer, Integer, Integer> table = HashBasedTable.create();
        Map<String, Map<String, Integer>> liveBonusMap = this.queryService.getLiveBonusCacheMap();
        if (CollectionUtils.isEmpty(liveBonusMap)) {
            return table;
        }
        liveBonusMap.keySet().forEach(teamId ->
                liveBonusMap.get(teamId).forEach((element, bonus) ->
                        table.put(Integer.parseInt(teamId), Integer.parseInt(element), bonus))
        );
        return table;
    }

    private LiveCalcData calcLiveSingleEntryPoints(int event, int entry, Map<String, PlayerEntity> playerInfoMap,
                                                   Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap,
                                                   Map<Integer, EntryEventPickEntity> entryEventPickMap, Map<Integer, EventLiveEntity> eventLiveMap,
                                                   Table<Integer, Integer, Integer> liveBonusTable,
                                                   ForkJoinPool forkJoinPool) {
        if (event <= 0) {
            return new LiveCalcData()
                    .setRank(0)
                    .setEvent(event)
                    .setEntry(entry)
                    .setEntryName("")
                    .setPlayerName("")
                    .setRegion("")
                    .setStartedEvent(0)
                    .setOverallPoints(0)
                    .setOverallRank(0)
                    .setValue(0)
                    .setBank(0)
                    .setTeamValue(0)
                    .setTotalTransfers(0)
                    .setLastOverallPoints(0)
                    .setLastOverallRank(0)
                    .setLastValue(0)
                    .setChip("")
                    .setLivePoints(0)
                    .setTransferCost(0)
                    .setLiveNetPoints(0)
                    .setLiveTotalPoints(0)
                    .setPlayed(0)
                    .setToPlay(0)
                    .setPlayedCaptain(0)
                    .setCaptainName("")
                    .setPickList(Lists.newArrayList())
                    .setTransfersList(Lists.newArrayList());
        }
        // blank entry
        if (entry < 0) {
            return new LiveCalcData()
                    .setRank(0)
                    .setEvent(event)
                    .setEntry(entry)
                    .setEntryName("轮空")
                    .setPlayerName("轮空")
                    .setRegion("")
                    .setStartedEvent(0)
                    .setOverallPoints(0)
                    .setOverallRank(0)
                    .setValue(0)
                    .setBank(0)
                    .setTeamValue(0)
                    .setTotalTransfers(0)
                    .setLastOverallPoints(0)
                    .setLastOverallRank(0)
                    .setLastValue(0)
                    .setChip("")
                    .setLivePoints(0)
                    .setTransferCost(0)
                    .setLiveNetPoints(0)
                    .setLiveTotalPoints(0)
                    .setPlayed(0)
                    .setToPlay(0)
                    .setPlayedCaptain(0)
                    .setCaptainName("")
                    .setPickList(Lists.newArrayList())
                    .setTransfersList(Lists.newArrayList());
        }
        // get user pick
        EntryEventPickEntity entryEventPickEntity = this.getEntryEventPick(event, entry, entryEventPickMap);
        if (entryEventPickEntity == null) {
            return new LiveCalcData();
        }
        // initialize element_live_data, static part
        List<Pick> eventPickList = JsonUtils.json2Collection(entryEventPickEntity.getPicks(), List.class, Pick.class);
        if (CollectionUtils.isEmpty(eventPickList)) {
            return new LiveCalcData();
        }
        List<ElementEventResultData> elementEventResultDataList = this.qryEntryLiveStaticData(event, eventPickList, playerInfoMap, teamLiveFixtureMap, forkJoinPool);
        // initialize element_live_data, event_live part
        this.initEventLiveData(elementEventResultDataList, eventLiveMap, liveBonusTable);
        // get active picks
        List<ElementEventResultData> pickList = this.getPickList(elementEventResultDataList);
        // calc live points
        int livePoints = this.calcActivePoints(Chip.getChipFromValue(entryEventPickEntity.getChip()), pickList);
        // played captain
        ElementEventResultData playedCaptain = this.selectPlayedCaptain(pickList);
        return new LiveCalcData()
                .setEntry(entry)
                .setEvent(event)
                .setPickList(pickList)
                .setChip(entryEventPickEntity.getChip())
                .setLivePoints(livePoints)
                .setTransferCost(entryEventPickEntity.getTransfersCost())
                .setLiveNetPoints(livePoints - entryEventPickEntity.getTransfersCost())
                .setPlayed(
                        (int) pickList
                                .stream()
                                .filter(o -> o.isPickActive() && (o.isPlayed() || StringUtils.equals("BLANK", o.getAgainstShortName()) || o.isGwFinished()))
                                .count()
                )
                .setToPlay(
                        (int) pickList
                                .stream()
                                .filter(o -> o.isPickActive() && !o.isGwStarted() && !o.isGwFinished())
                                .count()
                )
                .setPlayedCaptain(playedCaptain.getElement())
                .setCaptainName(playedCaptain.getWebName());
    }

    private EntryEventPickEntity getEntryEventPick(int event, int entry, Map<Integer, EntryEventPickEntity> entryEventPickMap) {
        EntryEventPickEntity entryEventPickEntity;
        if (CollectionUtils.isEmpty(entryEventPickMap) || !entryEventPickMap.containsKey(entry)) {
            entryEventPickEntity = this.entryEventPickService.getOne(new QueryWrapper<EntryEventPickEntity>().lambda()
                    .eq(EntryEventPickEntity::getEvent, event)
                    .eq(EntryEventPickEntity::getEntry, entry));
            if (entryEventPickEntity == null) {
                this.dataService.insertEntryEventPick(event, entry);
            }
        } else {
            entryEventPickEntity = entryEventPickMap.get(entry);
        }
        return entryEventPickEntity;
    }

    public List<ElementEventResultData> qryEntryLiveStaticData(int event, List<Pick> picks, Map<String, PlayerEntity> playerInfoMap, Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap, ForkJoinPool forkJoinPool) {
        List<CompletableFuture<ElementEventResultData>> future = picks.stream()
                .map(o -> CompletableFuture.supplyAsync(() ->
                        this.qryElementLiveStaticData(event, o.getElement(), o, playerInfoMap, teamLiveFixtureMap), forkJoinPool))
                .toList();
        return future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public ElementEventResultData qryElementLiveStaticData(int event, int element, Pick pick, Map<String, PlayerEntity> playerInfoMap, Map<Integer, Map<String, List<LiveFixtureData>>> teamLiveFixtureMap) {
        // from user pick
        ElementEventResultData elementEventResultData = new ElementEventResultData();
        elementEventResultData
                .setEvent(event)
                .setElement(element)
                .setPosition(pick.getPosition())
                .setMultiplier(pick.getMultiplier())
                .setCaptain(pick.isCaptain())
                .setViceCaptain(pick.isViceCaptain());
        // player info
        PlayerEntity playerEntity;
        if (playerInfoMap.containsKey(String.valueOf(element))) {
            playerEntity = playerInfoMap.get(String.valueOf(element));
        } else {
            playerEntity = this.queryService.getPlayerByElement(element);
        }
        if (playerEntity != null) {
            elementEventResultData
                    .setElementType(playerEntity.getElementType())
                    .setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType()))
                    .setPrice(playerEntity.getPrice() / 10.0)
                    .setWebName(playerEntity.getWebName());
            // event fixture
            int teamId = playerEntity.getTeamId();
            elementEventResultData.setTeamId(teamId);
            Map<String, List<LiveFixtureData>> liveFixtureMap = teamLiveFixtureMap.get(teamId);
            if (!CollectionUtils.isEmpty(liveFixtureMap)) {
                this.setMatchInfo(elementEventResultData, liveFixtureMap);
                this.setMatchPlayStatus(elementEventResultData, liveFixtureMap);
            } else {
                Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
                elementEventResultData
                        .setTeamId(teamId)
                        .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(teamId), ""))
                        .setAgainstId(0)
                        .setAgainstShortName("BLANK")
                        .setWasHome("")
                        .setScore("")
                        .setGwStarted(true)
                        .setGwFinished(true)
                        .setPlayStatus(MatchPlayStatus.Blank.getStatus())
                        .setBgw(true);
            }
        }
        return elementEventResultData;
    }

    private ElementEventResultData selectPlayedCaptain(List<ElementEventResultData> pickList) {
        ElementEventResultData captain = pickList
                .stream()
                .filter(ElementEventResultData::isCaptain)
                .findFirst()
                .orElse(new ElementEventResultData());
        ElementEventResultData viceCaptain = pickList
                .stream()
                .filter(ElementEventResultData::isViceCaptain)
                .findFirst()
                .orElse(new ElementEventResultData());
        if (captain.isPlayed() && captain.getMinutes() == 0 && viceCaptain.getMinutes() > 0) {
            return viceCaptain;
        }
        return captain;
    }

    private void setMatchInfo(ElementEventResultData elementEventResultData, Map<String, List<LiveFixtureData>> liveFixtureMap) {
        List<LiveFixtureData> list = Lists.newArrayList();
        liveFixtureMap.values().forEach(list::addAll);
        if (list.size() > 1) {
            elementEventResultData.setDgw(true);
        }
        // team short name
        List<String> teamShortNameList = list
                .stream()
                .map(LiveFixtureData::getTeamShortName)
                .collect(Collectors.toList());
        String teamShortName = this.setMultiMatch(teamShortNameList);
        // against short name
        List<String> againstShortNameList = list
                .stream()
                .map(LiveFixtureData::getAgainstShortName)
                .collect(Collectors.toList());
        String againstShortName = this.setMultiMatch(againstShortNameList);
        // was home
        List<String> wasHomeList = list
                .stream()
                .map(o -> o.isWasHome() ? "是" : "否")
                .collect(Collectors.toList());
        String wasHome = this.setMultiMatch(wasHomeList);
        // score
        List<String> scoreList = list
                .stream()
                .map(o -> o.getTeamScore() + "-" + o.getAgainstTeamScore())
                .collect(Collectors.toList());
        String score = this.setMultiMatch(scoreList);
        elementEventResultData
                .setTeamShortName(teamShortName)
                .setAgainstShortName(againstShortName)
                .setWasHome(wasHome)
                .setScore(score);
    }

    private String setMultiMatch(List<String> list) {
        StringBuffer buffer = new StringBuffer();
        list.forEach(str -> buffer.append(str).append(","));
        return buffer.substring(0, buffer.lastIndexOf(","));
    }

    private void setMatchPlayStatus(ElementEventResultData elementEventResultData, Map<String, List<LiveFixtureData>> liveFixtureMap) {
        int playingSize = 0;
        int notStartSize = 0;
        int finishSize = 0;
        if (!CollectionUtils.isEmpty(liveFixtureMap)) {
            playingSize = liveFixtureMap.get(MatchPlayStatus.Playing.name()).size();
            notStartSize = liveFixtureMap.get(MatchPlayStatus.Not_Start.name()).size();
            finishSize = liveFixtureMap.get(MatchPlayStatus.Finished.name()).size();
        }
        if (playingSize > 0) {
            elementEventResultData.setGwStarted(true).setGwFinished(false).setPlayStatus(MatchPlayStatus.Playing.getStatus());
        } else {
            if (notStartSize != 0) {
                if (finishSize == 0) { // n,0,0
                    elementEventResultData.setGwStarted(false).setGwFinished(false).setPlayStatus(MatchPlayStatus.Not_Start.getStatus());
                } else { // n,0,n
                    elementEventResultData.setGwStarted(true).setGwFinished(false).setPlayStatus(MatchPlayStatus.Event_Not_Finished.getStatus());
                }
            } else {
                if (finishSize == 0) { // 0,0,0
                    elementEventResultData.setGwStarted(false).setGwFinished(false).setPlayStatus(MatchPlayStatus.Blank.getStatus());
                } else { // 0,0,n
                    elementEventResultData.setGwStarted(true).setGwFinished(true).setPlayStatus(MatchPlayStatus.Finished.getStatus());
                }
            }
        }
    }

    private void initEventLiveData(List<ElementEventResultData> elementEventResultDataList, Map<Integer, EventLiveEntity> eventLiveMap, Table<Integer, Integer, Integer> liveBonusTable) {
        if (CollectionUtils.isEmpty(eventLiveMap)) {
            return;
        }
        elementEventResultDataList.forEach(elementEventResultData -> {
            EventLiveEntity eventLiveEntity = eventLiveMap.get(elementEventResultData.getElement());
            if (eventLiveEntity != null) {
                BeanUtil.copyProperties(eventLiveEntity, elementEventResultData, CopyOptions.create().ignoreNullValue());
                // calc total point
                elementEventResultData.setTotalPoints(elementEventResultData.isDgw() ? eventLiveEntity.getTotalPoints() : this.calcElementLivePoints(eventLiveEntity));
                // calc played
                elementEventResultData.setPlayed(elementEventResultData.getMinutes() > 0 || elementEventResultData.getYellowCards() > 0 || elementEventResultData.getRedCards() > 0);
                this.setEventLiveBonusData(elementEventResultData, liveBonusTable);
            }
        });
    }

    private void setEventLiveBonusData(ElementEventResultData elementEventResultData, Table<Integer, Integer, Integer> liveBonusTable) {
        if (elementEventResultData.getBonus() > 0 || liveBonusTable == null) {
            return;
        }
        int teamId = elementEventResultData.getTeamId();
        int element = elementEventResultData.getElement();
        if (liveBonusTable.contains(teamId, element)) {
            int bonus = liveBonusTable.get(teamId, element);
            elementEventResultData.setBonus(bonus);
            elementEventResultData.setTotalPoints(elementEventResultData.getTotalPoints() + elementEventResultData.getBonus());
        }
    }

    private List<ElementEventResultData> getPickList(List<ElementEventResultData> elementEventResultDataList) {
        // element_type -> active -> start
        Map<Integer, Table<Boolean, Boolean, List<ElementEventResultData>>> map = elementEventResultDataList
                .stream()
                .collect(new ElementLiveCollector());
        // gkp
        List<ElementEventResultData> gkps = this.createSteam(map.get(Position.GKP.getElementType()).get(true, true),
                        map.get(Position.GKP.getElementType()).get(true, false),
                        map.get(Position.GKP.getElementType()).get(false, true))
                .flatMap(Collection::stream)
                .limit(PositionRule.MIN_NUM_GKP.getNum())
                .collect(Collectors.toList());
        // active defs
        List<ElementEventResultData> defs = this.createSteam(map.get(Position.DEF.getElementType()).get(true, true))
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                .collect(Collectors.toList());
        // def rule, at least 3
        if (defs.size() < PositionRule.MIN_NUM_DEF.getNum()) {
            defs = this.createSteam(defs,
                            map.get(Position.DEF.getElementType()).get(true, false),
                            map.get(Position.DEF.getElementType()).get(false, true))
                    .flatMap(Collection::stream)
                    .limit(PositionRule.MIN_NUM_DEF.getNum())
                    .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                    .collect(Collectors.toList());
        }
        // active fwds
        List<ElementEventResultData> fwds = this.createSteam(map.get(Position.FWD.getElementType()).get(true, true))
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                .collect(Collectors.toList());
        // fwd rule, at least 1
        if (fwds.size() < PositionRule.MIN_NUM_FWD.getNum()) {
            fwds = this.createSteam(fwds,
                            map.get(Position.FWD.getElementType()).get(true, false),
                            map.get(Position.FWD.getElementType()).get(false, true))
                    .flatMap(Collection::stream)
                    .limit(PositionRule.MIN_NUM_FWD.getNum())
                    .collect(Collectors.toList());
        }
        // mids
        int maxMidNum = PositionRule.MIN_PLAYERS.getNum() - gkps.size() - defs.size() - fwds.size();
        List<ElementEventResultData> mids = this.createSteam(map.get(Position.MID.getElementType()).get(true, true))
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                .limit(maxMidNum)
                .collect(Collectors.toList());
        // active_list
        List<ElementEventResultData> activeList = this.createSteam(gkps, defs, fwds, mids)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(ElementEventResultData::getElementType)
                        .thenComparing(ElementEventResultData::getPosition))
                .collect(Collectors.toList());
        // sub list
        if (activeList.size() < PositionRule.MIN_PLAYERS.getNum()) {
            List<ElementEventResultData> subList = this.createSteam(
                            map.get(Position.DEF.getElementType()).get(true, false),
                            map.get(Position.MID.getElementType()).get(true, false),
                            map.get(Position.FWD.getElementType()).get(true, false))
                    .flatMap(Collection::stream)
                    .filter(o -> !activeList.contains(o))
                    .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                    .limit(PositionRule.MIN_PLAYERS.getNum() - activeList.size())
                    .toList();
            activeList.addAll(subList);
        }
        if (activeList.size() < PositionRule.MIN_PLAYERS.getNum()) {
            // stand by list
            List<ElementEventResultData> standByList = this.createSteam(
                            map.get(Position.DEF.getElementType()).get(false, true),
                            map.get(Position.MID.getElementType()).get(false, true),
                            map.get(Position.FWD.getElementType()).get(false, true))
                    .flatMap(Collection::stream)
                    .filter(o -> !activeList.contains(o))
                    .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                    .limit(PositionRule.MIN_PLAYERS.getNum() - activeList.size())
                    .toList();
            activeList.addAll(standByList);
        }
        List<ElementEventResultData> pickList = activeList
                .stream()
                .sorted(Comparator.comparing(ElementEventResultData::getElementType)
                        .thenComparing(ElementEventResultData::getPosition))
                .collect(Collectors.toList());
        pickList.addAll(
                elementEventResultDataList.stream()
                        .filter(o -> !pickList.contains(o))
                        .sorted(Comparator.comparing(ElementEventResultData::getPosition))
                        .toList()
        );
        return pickList;
    }

    private int calcActivePoints(Chip chip, List<ElementEventResultData> pickList) {
        int activeCaptain = this.getActiveCaptain(pickList);
        if (activeCaptain == 0) {
            return 0;
        }
        // only 3c and bb change calculate rule
        return switch (chip) {
            case TC -> {
                pickList.subList(0, 11).forEach(o -> o.setPickActive(true));
                yield pickList.subList(0, 11).stream().filter(o -> o.getElement() != activeCaptain).mapToInt(ElementEventResultData::getTotalPoints).sum()
                        + pickList.stream().filter(o -> o.getElement() == activeCaptain).mapToInt(o -> 3 * o.getTotalPoints()).sum();
            }
            case BB -> {
                pickList.forEach(o -> o.setPickActive(true));
                yield pickList.stream().filter(o -> o.getElement() != activeCaptain).mapToInt(ElementEventResultData::getTotalPoints).sum()
                        + pickList.stream().filter(o -> o.getElement() == activeCaptain).mapToInt(o -> 2 * o.getTotalPoints()).sum();
            }
            case NONE, WC, FH -> {
                pickList.subList(0, 11).forEach(o -> o.setPickActive(true));
                yield pickList.subList(0, 11).stream().filter(o -> o.getElement() != activeCaptain).mapToInt(ElementEventResultData::getTotalPoints).sum()
                        + pickList.stream().filter(o -> o.getElement() == activeCaptain).mapToInt(o -> 2 * o.getTotalPoints()).sum();
            }
        };
    }

    private int getActiveCaptain(List<ElementEventResultData> pickList) {
        // captain played
        int activeCaptain = pickList.stream()
                .filter(ElementEventResultData::isCaptain)
                .filter(o -> !o.isGwStarted() || o.isPlayed())
                .map(ElementEventResultData::getElement)
                .findFirst()
                .orElse(0);
        // vice captain played
        if (activeCaptain == 0) {
            activeCaptain = pickList.stream()
                    .filter(ElementEventResultData::isViceCaptain)
                    .filter(o -> !o.isGwStarted() || o.isPlayed())
                    .map(ElementEventResultData::getElement)
                    .findFirst()
                    .orElse(0);
        }
        // none played
        if (activeCaptain == 0) {
            activeCaptain = pickList.stream()
                    .filter(ElementEventResultData::isCaptain)
                    .map(ElementEventResultData::getElement)
                    .findFirst()
                    .orElse(0);
        }
        return activeCaptain;
    }

    @Override
    public LiveCalcElementData calcLivePointsByElement(int event, int element) {
        EventLiveEntity eventLiveEntity = this.queryService.getEventLiveByEvent(event).getOrDefault(String.valueOf(element), null);
        if (eventLiveEntity == null) {
            return new LiveCalcElementData();
        }
        int elementType = eventLiveEntity.getElementType();
        int teamId = eventLiveEntity.getTeamId();
        int bonus = eventLiveEntity.getBonus() > 0 ? eventLiveEntity.getBonus() :
                this.queryService.getLiveBonusCacheMap().getOrDefault(String.valueOf(teamId), Maps.newHashMap()).getOrDefault(String.valueOf(element), 0);
        return BeanUtil.copyProperties(eventLiveEntity, LiveCalcElementData.class)
                .setEvent(event)
                .setLivePoints(this.calcElementLivePoints(eventLiveEntity))
                .setLiveBonus(bonus)
                .setMinutesPoints(this.calcElementPlayingPoints(eventLiveEntity.getMinutes()))
                .setGoalsScoredPoints(this.calcElementGoalsScoredPoints(elementType, eventLiveEntity.getGoalsScored()))
                .setAssistsPoints(this.calcElementGoalsAssistPoints(eventLiveEntity.getAssists()))
                .setCleanSheetsPoints(this.calcElementCleanSheetsPoints(elementType, eventLiveEntity.getCleanSheets()))
                .setGoalsConcededPoints(this.calcElementGoalsConcededPoints(elementType, eventLiveEntity.getGoalsConceded()))
                .setOwnGoalsPoints(this.calcElementOwnGoalsPoints(eventLiveEntity.getOwnGoals()))
                .setPenaltiesMissedPoints(this.calcElementPenaltiesMissedPoints(eventLiveEntity.getPenaltiesMissed()))
                .setPenaltiesSavedPoints(this.calcElementPenaltiesSavedPoints(eventLiveEntity.getPenaltiesSaved()))
                .setYellowCardsPoints(this.calcElementYellowCardsPoints(eventLiveEntity.getYellowCards()))
                .setRedCardsPoints(this.calcElementRedCardsPoints(eventLiveEntity.getRedCards()))
                .setSavesPoints(this.calcElementSavesPoints(eventLiveEntity.getSaves()));
    }

    // dgw is shit
    private int calcElementLivePoints(EventLiveEntity eventLiveEntity) {
        int elementType = eventLiveEntity.getElementType();
        return this.calcElementPlayingPoints(eventLiveEntity.getMinutes())
                + this.calcElementGoalsScoredPoints(elementType, eventLiveEntity.getGoalsScored())
                + this.calcElementGoalsAssistPoints(eventLiveEntity.getAssists())
                + this.calcElementCleanSheetsPoints(elementType, eventLiveEntity.getCleanSheets())
                + this.calcElementGoalsConcededPoints(elementType, eventLiveEntity.getGoalsConceded())
                + this.calcElementPenaltiesSavedPoints(eventLiveEntity.getPenaltiesSaved())
                + this.calcElementPenaltiesMissedPoints(eventLiveEntity.getPenaltiesMissed())
                + this.calcElementOwnGoalsPoints(eventLiveEntity.getOwnGoals())
                + this.calcElementYellowCardsPoints(eventLiveEntity.getYellowCards())
                + this.calcElementRedCardsPoints(eventLiveEntity.getRedCards())
                + this.calcElementSavesPoints(eventLiveEntity.getSaves())
                + this.calcElementBonusPoints(eventLiveEntity.getBonus());
    }

    private int calcElementPlayingPoints(int minutes) {
        if (minutes > 0 && minutes < 60) {
            return 1;
        } else if (minutes >= 60 && minutes <= 90) {
            return 2;
        }
        return 0;
    }

    private int calcElementGoalsScoredPoints(int elementType, int goalsScored) {
        return switch (elementType) {
            case 1, 2 -> 6 * goalsScored;
            case 3 -> 5 * goalsScored;
            case 4 -> 4 * goalsScored;
            default -> 0;
        };
    }

    private int calcElementGoalsAssistPoints(int assists) {
        return 3 * assists;
    }

    private int calcElementCleanSheetsPoints(int elementType, int cleanSheet) {
        return switch (elementType) {
            case 1, 2 -> 4 * cleanSheet;
            case 3 -> cleanSheet;
            default -> 0;
        };
    }

    private int calcElementGoalsConcededPoints(int elementType, int goalsConceded) {
        if (elementType == 1 || elementType == 2) {
            return -1 * ((int) Math.floor(goalsConceded * 1.0 / 2));
        }
        return 0;
    }

    private int calcElementPenaltiesSavedPoints(int penaltiesSaved) {
        return 5 * penaltiesSaved;
    }

    private int calcElementPenaltiesMissedPoints(int penaltiesMissed) {
        return -2 * penaltiesMissed;
    }

    private int calcElementOwnGoalsPoints(int ownGoals) {
        return -2 * ownGoals;
    }

    private int calcElementYellowCardsPoints(int yellowCards) {
        return -1 * yellowCards;
    }

    private int calcElementRedCardsPoints(int redCards) {
        return -3 * redCards;
    }

    private int calcElementSavesPoints(int saves) {
        return (int) Math.floor(saves * 1.0 / 3);
    }

    private int calcElementBonusPoints(int bonus) {
        return bonus;
    }

    @SafeVarargs
    private final <T> Stream<T> createSteam(T... values) {
        Stream.Builder<T> builder = Stream.builder();
        Arrays.asList(values).forEach(builder::add);
        return builder.build();
    }

}

package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import com.tong.fpl.config.collector.ElementLiveCollector;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.MatchPlayStatus;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.constant.enums.PositionRule;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.live.*;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.ILiveService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.db.EntryEventPickService;
import com.tong.fpl.service.db.EntryEventResultService;
import com.tong.fpl.service.db.EntryInfoService;
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

    private final IInterfaceService interfaceService;
    private final IQueryService queryService;

    private final EntryInfoService entryInfoService;
    private final EntryEventPickService entryEventPickService;
    private final EntryEventResultService entryEventResultService;

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
    public List<LiveCalcData> calcLivePointsByTournament(int event, int tournamentId) {
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
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // calc live entry points
        ForkJoinPool forkJoinPool = new ForkJoinPool(10);
        List<CompletableFuture<LiveCalcData>> future = entryList.stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.calcLiveSingleEntryPoints(event, o, playerInfoMap,
                        teamLiveFixtureMap, entryEventPickMap, eventLiveMap, liveBonusTable, forkJoinPool), forkJoinPool))
                .collect(Collectors.toList());
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
        // sort
        Map<Integer, Integer> rankMap = this.sortTournamentLivePointsRank(liveCalcList);
        if (!CollectionUtils.isEmpty(rankMap)) {
            liveCalcList.forEach(o -> o.setRank(rankMap.get(o.getLivePoints())));
        }
        return liveCalcList
                .stream()
                .sorted(Comparator.comparing(LiveCalcData::getRank))
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

    @Override
    public SearchLiveCalcData calcSearchLivePointsByTournament(int event, int tournamentId, int element) {
        if (event <= 0 || tournamentId <= 0 || element <= 0) {
            return new SearchLiveCalcData();
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
                .collect(Collectors.toList());
        List<LiveCalcData> liveCalcList = future
                .stream()
                .map(CompletableFuture::join)
                .filter(o -> this.containSearchElement(element, o.getPickList(), o.getChip()))
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
        // sort
        Map<Integer, Integer> rankMap = this.sortTournamentLivePointsRank(liveCalcList);
        if (!CollectionUtils.isEmpty(rankMap)) {
            liveCalcList.forEach(o -> o.setRank(rankMap.get(o.getLivePoints())));
        }
        List<LiveCalcData> list = liveCalcList
                .stream()
                .sorted(Comparator.comparing(LiveCalcData::getRank))
                .collect(Collectors.toList());
        List<Integer> searchElementList = Lists.newArrayList();
        list.forEach(o -> o.getPickList().forEach(i -> searchElementList.add(i.getElement())));
        list.forEach(o -> o.setPickList(null));
        List<Integer> searchList = searchElementList
                .stream()
                .filter(o -> o == element)
                .collect(Collectors.toList());
        return new SearchLiveCalcData()
                .setElement(element)
                .setWebName(playerInfoMap.get(String.valueOf(element)).getWebName())
                .setSelectNum(searchList.size())
                .setSelectByPercent(searchList.size() == 0 ? "0%" : CommonUtils.getPercentResult(searchList.size(), entryList.size()))
                .setLiveCalcDataList(liveCalcList);
    }

    private boolean containSearchElement(int element, List<ElementEventResultData> pickList, String chip) {
        for (ElementEventResultData data :
                pickList) {
            if (!StringUtils.equalsIgnoreCase(Chip.BB.getValue(), chip)) {
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
                this.interfaceService.refreshEntryEventPick(event, entry);
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
                .collect(Collectors.toList());
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
        return buffer.toString().substring(0, buffer.lastIndexOf(","));
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
        if (liveBonusTable == null) {
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
                    .collect(Collectors.toList());
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
                    .collect(Collectors.toList());
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
                        .collect(Collectors.toList())
        );
        return pickList;
    }

    private int calcActivePoints(Chip chip, List<ElementEventResultData> pickList) {
        int activeCaptain = this.getActiveCaptain(pickList);
        if (activeCaptain == 0) {
            return 0;
        }
        switch (chip) {
            case TC:
                pickList.subList(0, 11).forEach(o -> o.setPickActive(true));
                return pickList.subList(0, 11).stream().filter(o -> o.getElement() != activeCaptain).mapToInt(ElementEventResultData::getTotalPoints).sum()
                        + pickList.stream().filter(o -> o.getElement() == activeCaptain).mapToInt(o -> 3 * o.getTotalPoints()).sum();
            case BB:
                pickList.forEach(o -> o.setPickActive(true));
                return pickList.stream().filter(o -> o.getElement() != activeCaptain).mapToInt(ElementEventResultData::getTotalPoints).sum()
                        + pickList.stream().filter(o -> o.getElement() == activeCaptain).mapToInt(o -> 2 * o.getTotalPoints()).sum();
            // only 3c and bb change calculate rule
            case NONE:
            case WC:
            case FH:
                pickList.subList(0, 11).forEach(o -> o.setPickActive(true));
                return pickList.subList(0, 11).stream().filter(o -> o.getElement() != activeCaptain).mapToInt(ElementEventResultData::getTotalPoints).sum()
                        + pickList.stream().filter(o -> o.getElement() == activeCaptain).mapToInt(o -> 2 * o.getTotalPoints()).sum();
            default:
                return 0;
        }
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
        switch (elementType) {
            case 1:
            case 2: {
                return 6 * goalsScored;
            }
            case 3: {
                return 5 * goalsScored;
            }
            case 4: {
                return 4 * goalsScored;
            }
        }
        return 0;
    }

    private int calcElementGoalsAssistPoints(int assists) {
        return 3 * assists;
    }

    private int calcElementCleanSheetsPoints(int elementType, int cleanSheet) {
        switch (elementType) {
            case 1:
            case 2: {
                return 4 * cleanSheet;
            }
            case 3: {
                return cleanSheet;
            }
        }
        return 0;
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

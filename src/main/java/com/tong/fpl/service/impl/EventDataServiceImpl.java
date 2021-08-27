package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.domain.data.entry.Match;
import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.domain.data.userHistory.Current;
import com.tong.fpl.domain.data.userpick.AutoSubs;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventAutoSubsData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutNextRoundData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import com.tong.fpl.service.IEventDataService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.IReportService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.JsonUtils;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/3/10
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventDataServiceImpl implements IEventDataService {

    private final IQueryService queryService;
    private final IRedisCacheService redisCacheService;
    private final IReportService reportService;
    private final EntryEventResultService entryEventResultService;
    private final EntryEventCupResultService entryEventCupResultService;
    private final EntryEventPickService entryEventPickService;
    private final EntryEventTransfersService entryEventTransferService;
    private final EntryInfoService entryInfoService;
    private final TournamentEntryService tournamentEntryService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
    private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
    private final TournamentKnockoutService tournamentKnockoutService;
    private final TournamentKnockoutResultService tournamentKnockoutResultService;
    private final ZjTournamentResultService zjTournamentResultService;

    /**
     * @implNote daily
     */
    @Override
    public void updateEventData() {
        this.redisCacheService.insertEvent();
        this.redisCacheService.insertEventFixture();
    }

    @Override
    public void updatePlayerData() {
        this.redisCacheService.insertPlayer();
        this.redisCacheService.insertPlayerStat();
        this.redisCacheService.insertPlayerValue();
        RedisUtils.removeCacheByKey("api::qryPlayer");
    }

    @Override
    public void updateEntryInfo(List<Integer> entryList) {
        List<EntryInfoEntity> updateEntryInfoList = Lists.newArrayList();
        // get entry info list
        List<EntryInfoEntity> entryInfoList = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
                .in(EntryInfoEntity::getEntry, entryList));
        if (CollectionUtils.isEmpty(entryInfoList)) {
            return;
        }
        entryInfoList.parallelStream().forEach(entryInfoEntity -> {
            int entry = entryInfoEntity.getEntry();
            // entry
            EntryRes entryRes = this.queryService.getEntry(entry);
            if (entryRes == null) {
                return;
            }
            entryInfoEntity
                    .setEntryName(entryRes.getName())
                    .setPlayerName(entryRes.getPlayerFirstName() + " " + entryRes.getPlayerLastName())
                    .setOverallPoints(entryRes.getSummaryOverallPoints())
                    .setOverallRank(entryRes.getSummaryOverallRank())
                    .setBank(entryRes.getLastDeadlineBank())
                    .setTeamValue(entryRes.getLastDeadlineValue())
                    .setTotalTransfers(entryRes.getLastDeadlineTotalTransfers())
                    .setLastOverallPoints(entryRes.getSummaryOverallPoints())
                    .setLastOverallRank(entryRes.getSummaryOverallRank())
                    .setLastTeamValue(entryRes.getLastDeadlineValue());
            // user_history
            Current lastCurrent = this.getUserLastCurrent(entry);
            entryInfoEntity
                    .setLastOverallPoints(lastCurrent == null ? 0 : lastCurrent.getTotalPoints())
                    .setLastOverallRank(lastCurrent == null ? 0 : lastCurrent.getOverallRank())
                    .setLastTeamValue(lastCurrent == null ? 0 : lastCurrent.getValue());
            updateEntryInfoList.add(entryInfoEntity);
        });
        // update
        this.entryInfoService.updateBatchById(updateEntryInfoList);
        log.info("update entry info size:{}!", updateEntryInfoList.size());
    }

    private Current getUserLastCurrent(int entry) {
        UserHistoryRes userHistoryRes = this.queryService.getUserHistory(entry);
        if (userHistoryRes == null) {
            return null;
        }
        int lastEvent = this.queryService.getLastEvent();
        return userHistoryRes.getCurrent()
                .stream()
                .filter(o -> o.getEvent() == lastEvent)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateEntryInfoByEntry(int entry) {
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        // entry
        EntryRes entryRes = this.queryService.getEntry(entry);
        if (entryRes == null) {
            return;
        }
        entryInfoEntity
                .setEntryName(entryRes.getName())
                .setPlayerName(entryRes.getPlayerFirstName() + " " + entryRes.getPlayerLastName())
                .setOverallPoints(entryRes.getSummaryOverallPoints())
                .setOverallRank(entryRes.getSummaryOverallRank())
                .setBank(entryRes.getLastDeadlineBank())
                .setTeamValue(entryRes.getLastDeadlineValue())
                .setTotalTransfers(entryRes.getLastDeadlineTotalTransfers())
                .setLastOverallPoints(entryRes.getSummaryOverallPoints())
                .setLastOverallRank(entryRes.getSummaryOverallRank())
                .setLastTeamValue(entryRes.getLastDeadlineValue());
        // user_history
        Current lastCurrent = this.getUserLastCurrent(entry);
        entryInfoEntity
                .setLastOverallPoints(lastCurrent == null ? 0 : lastCurrent.getTotalPoints())
                .setLastOverallRank(lastCurrent == null ? 0 : lastCurrent.getOverallRank())
                .setLastTeamValue(lastCurrent == null ? 0 : lastCurrent.getValue());
        // update
        this.entryInfoService.updateById(entryInfoEntity);
    }

    /**
     * @implNote after deadline
     */
    @Override
    public void insertEntryEventPick(int event, int entry) {
        if (entry <= 0) {
            return;
        }
        if (this.entryEventPickService.count(new QueryWrapper<EntryEventPickEntity>().lambda()
                .eq(EntryEventPickEntity::getEntry, entry)
                .eq(EntryEventPickEntity::getEvent, event)) > 0) {
            return;
        }
        UserPicksRes userPicksRes = this.queryService.getUserPicks(event, entry);
        if (userPicksRes == null || CollectionUtils.isEmpty(userPicksRes.getPicks())) {
            return;
        }
        this.entryEventPickService.save(this.initEventEntryPicks(event, entry, 0));
    }

    private EntryEventPickEntity initEventEntryPicks(int event, int entry, int tryTimes) {
        if (entry <= 0) {
            return null;
        }
        UserPicksRes userPicksRes = this.queryService.getUserPicks(event, entry);
        if (userPicksRes == null || CollectionUtils.isEmpty(userPicksRes.getPicks())) {
            tryTimes++;
            if (tryTimes >= 5) {
                return null;
            }
            this.initEventEntryPicks(event, entry, tryTimes);
        }
        if (userPicksRes == null) {
            return null;
        }
        return new EntryEventPickEntity()
                .setEntry(entry)
                .setEvent(event)
                .setTransfers(userPicksRes.getEntryHistory().getEventTransfers())
                .setTransfersCost(userPicksRes.getEntryHistory().getEventTransfersCost())
                .setChip(userPicksRes.getActiveChip() == null ? Chip.NONE.getValue() : userPicksRes.getActiveChip())
                .setPicks(JsonUtils.obj2json(userPicksRes.getPicks()));
    }

    @Override
    public void insertEventPickByEntryList(int event, List<Integer> entryList) {
        // init
        List<Integer> existsList = this.entryEventPickService.list(new QueryWrapper<EntryEventPickEntity>().lambda()
                .eq(EntryEventPickEntity::getEvent, event)
                .in(EntryEventPickEntity::getEntry, entryList))
                .stream()
                .map(EntryEventPickEntity::getEntry)
                .collect(Collectors.toList());
        entryList = entryList
                .stream()
                .filter(o -> !existsList.contains(o))
                .collect(Collectors.toList());
        // save
        List<CompletableFuture<EntryEventPickEntity>> future = entryList
                .stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> this.initEventEntryPicks(event, entry, 0)))
                .collect(Collectors.toList());
        List<EntryEventPickEntity> list = future
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        this.entryEventPickService.saveBatch(list);
        log.info("event:{}, insert event pick size:{}!", event, list.size());
    }

    @Override
    public void insertEntryEventTransfers(int entry) {
        if (entry <= 0) {
            return;
        }
        List<TransferRes> transferResList = this.queryService.getTransfer(entry);
        if (CollectionUtils.isEmpty(transferResList)) {
            return;
        }
        List<EntryEventTransfersEntity> list = Lists.newArrayList();
        // get data from transfer
        List<EntryEventTransfersEntity> entryEventTransferList = this.getEntryEventTransfer(transferResList);
        if (CollectionUtils.isEmpty(entryEventTransferList)) {
            return;
        }
        // insert or update
        Map<String, EntryEventTransfersEntity> entryEventTransferMap = this.entryEventTransferService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                .eq(EntryEventTransfersEntity::getEntry, entry))
                .stream()
                .collect(Collectors.toMap(k -> StringUtils.joinWith("-", k.getEvent(), k.getEntry(), k.getElementIn(), k.getElementOut(), k.getTime()), o -> o));
        entryEventTransferList.forEach(o -> {
            if (!entryEventTransferMap.containsKey(StringUtils.joinWith("-", o.getEvent(), o.getEntry(), o.getElementIn(), o.getElementOut(), o.getTime()))) {
                list.add(o);
            }
        });
        this.entryEventTransferService.saveBatch(list);
    }

    @Override
    public void insertEventTransfersByEntryList(List<Integer> entryList) {
        Map<String, EntryEventTransfersEntity> entryEventTransferMap = this.entryEventTransferService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                .in(EntryEventTransfersEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(k -> StringUtils.joinWith("-", k.getEvent(), k.getEntry(), k.getElementIn(), k.getElementOut(), k.getTime()), o -> o));
        // upsert entry_event_transfer
        List<EntryEventTransfersEntity> list = Lists.newArrayList();
        entryList.forEach(entry -> {
            if (entry <= 0) {
                return;
            }
            List<TransferRes> transferResList = this.queryService.getTransfer(entry);
            if (CollectionUtils.isEmpty(transferResList)) {
                return;
            }
            List<EntryEventTransfersEntity> entryEventTransferList = this.getEntryEventTransfer(transferResList);
            if (CollectionUtils.isEmpty(entryEventTransferList)) {
                return;
            }
            entryEventTransferList.forEach(o -> {
                if (!entryEventTransferMap.containsKey(StringUtils.joinWith("-", o.getEvent(), o.getEntry(), o.getElementIn(), o.getElementOut(), o.getTime()))) {
                    list.add(o);
                }
            });
        });
        this.entryEventTransferService.saveBatch(list);
        log.info("insert event transfers size:{}!", list.size());
    }

    @Override
    public void insertEntryEventCupResult(int event, int entry) {
        if (entry < 0) {
            return;
        }
        if (this.entryEventCupResultService.count(new QueryWrapper<EntryEventCupResultEntity>().lambda()
                .eq(EntryEventCupResultEntity::getEvent, event)
                .eq(EntryEventCupResultEntity::getEntry, entry)) > 0) {
            return;
        }
        EntryCupRes entryCupRes = this.queryService.getEntryCup(entry);
        if (entryCupRes == null) {
            return;
        }
        Match cupMatch = entryCupRes.getCupMatches()
                .stream()
                .filter(o -> o.getEvent() == event)
                .findFirst()
                .orElse(null);
        if (cupMatch == null) {
            return;
        }
        // entry_event_cup_result
        EntryEventCupResultEntity entryEventCupResult = new EntryEventCupResultEntity()
                .setEvent(event)
                .setEntry(entry)
                .setEntryName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1Name() : cupMatch.getEntry2Name())
                .setPlayerName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1PlayerName() : cupMatch.getEntry2PlayerName())
                .setEventPoints(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1Points() : cupMatch.getEntry2Points())
                .setAgainstEntry(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Entry() : cupMatch.getEntry1Entry())
                .setAgainstEntryName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Name() : cupMatch.getEntry1Name())
                .setAgainstPlayerName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2PlayerName() : cupMatch.getEntry1PlayerName())
                .setAgainstEventPoints(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Points() : cupMatch.getEntry1Points());
        if (cupMatch.getWinner() == 0) {
            if (entryEventCupResult.getEventPoints() >= entryEventCupResult.getAgainstEventPoints()) {
                entryEventCupResult.setResult("Win");
            }
        } else if (cupMatch.getWinner() == entryEventCupResult.getEntry()) {
            entryEventCupResult.setResult("Win");
        } else {
            entryEventCupResult.setResult("Lose");
        }
        this.entryEventCupResultService.save(entryEventCupResult);
    }

    @Override
    public void insertEventCupResultByEntryList(int event, List<Integer> entryList) {
        // get entry_list
        if (this.entryEventCupResultService.count(new QueryWrapper<EntryEventCupResultEntity>().lambda()
                .eq(EntryEventCupResultEntity::getEvent, event)
                .in(EntryEventCupResultEntity::getEntry, entryList)) > 0) {
            return;
        }
        // upsert entry_event_result
        List<EntryEventCupResultEntity> insertEventCupResultList = Lists.newArrayList();
        entryList.forEach(entry -> {
            EntryCupRes entryCupRes = this.queryService.getEntryCup(entry);
            if (entryCupRes == null) {
                return;
            }
            Match cupMatch = entryCupRes.getCupMatches()
                    .stream()
                    .filter(o -> o.getEvent() == event)
                    .findFirst()
                    .orElse(null);
            if (cupMatch == null) {
                return;
            }
            // entry_event_cup_result
            EntryEventCupResultEntity entryEventCupResult = new EntryEventCupResultEntity()
                    .setEvent(event)
                    .setEntry(entry)
                    .setEntryName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1Name() : cupMatch.getEntry2Name())
                    .setPlayerName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1PlayerName() : cupMatch.getEntry2PlayerName())
                    .setEventPoints(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1Points() : cupMatch.getEntry2Points())
                    .setAgainstEntry(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Entry() : cupMatch.getEntry1Entry())
                    .setAgainstEntryName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Name() : cupMatch.getEntry1Name())
                    .setAgainstPlayerName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2PlayerName() : cupMatch.getEntry1PlayerName())
                    .setAgainstEventPoints(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Points() : cupMatch.getEntry1Points());
            if (cupMatch.getWinner() == 0) {
                if (entryEventCupResult.getEventPoints() >= entryEventCupResult.getAgainstEventPoints()) {
                    entryEventCupResult.setResult("Win");
                }
            } else if (cupMatch.getWinner() == entryEventCupResult.getEntry()) {
                entryEventCupResult.setResult("Win");
            } else {
                entryEventCupResult.setResult("Lose");
            }
        });
        this.entryEventCupResultService.saveBatch(insertEventCupResultList);
        log.info("event:{}, insert tournament entry event cup result size:{}!", event, insertEventCupResultList.size());
    }

    /**
     * @implNote after matchDay
     */

    @Override
    public void updateEventLiveData(int event) {
        this.redisCacheService.insertSingleEventFixture(event);
        this.redisCacheService.insertLiveFixtureCache();
        this.redisCacheService.insertLiveBonusCache();
        this.redisCacheService.insertEventLive(event);
    }

    @Override
    public void upsertEntryEventResult(int event, int entry) {
        if (entry <= 0) {
            return;
        }
        UserPicksRes userPick = this.queryService.getUserPicks(event, entry);
        if (userPick == null) {
            return;
        }
        // get event_live
        Map<String, EventLiveEntity> eventLiveMap = this.queryService.getEventLiveByEvent(event);
        // entry_event_result
        EntryEventResultEntity entryEventResult = this.calcEntryEventPoints(event, entry, userPick, eventLiveMap);
        if (entryEventResult == null) {
            return;
        }
        // insert or update
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .eq(EntryEventResultEntity::getEntry, entry));
        if (entryEventResultEntity == null) {
            this.entryEventResultService.save(entryEventResult);
        } else {
            entryEventResult.setId(entryEventResultEntity.getId());
            this.entryEventResultService.updateById(entryEventResult);
        }
    }

    @Override
    public void upsertTournamentEntryEventResult(int event, int tournamentId) {
        // get entry_list
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        if (CollectionUtils.isEmpty(entryList)) {
            log.error("tournament:{}, tournament_info not exists!", tournamentId);
            return;
        }
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        // get event_live
        Map<String, EventLiveEntity> eventLiveMap = this.queryService.getEventLiveByEvent(event);
        // upsert entry_event_result
        RedisUtils.removeCacheByKey("get");
        List<EntryEventResultEntity> insertEventResultList = Lists.newArrayList();
        List<EntryEventResultEntity> updateEventResultList = Lists.newArrayList();
        entryList.forEach(entry -> {
            if (entry <= 0) {
                return;
            }
            UserPicksRes userPick = this.queryService.getUserPicks(event, entry);
            if (userPick == null) {
                return;
            }
            EntryEventResultEntity entryEventResultEntity = this.calcEntryEventPoints(event, entry, userPick, eventLiveMap);
            if (entryEventResultEntity == null) {
                return;
            }
            if (!entryEventResultMap.containsKey(entry)) {
                insertEventResultList.add(entryEventResultEntity);
            } else {
                entryEventResultEntity.setId(entryEventResultMap.get(entry).getId());
                updateEventResultList.add(entryEventResultEntity);
            }
        });
        this.entryEventResultService.saveBatch(insertEventResultList);
        log.info("tournament:{}, event:{}, insert tournament entry event result size:{}!", tournamentId, event, insertEventResultList.size());
        this.entryEventResultService.updateBatchById(updateEventResultList);
        log.info("tournament:{}, event:{}, update tournament entry event result size:{}!", tournamentId, event, updateEventResultList.size());
    }

    @Override
    public void upsertEventResultByEntryList(int event, List<Integer> entryList) {
        // get entry_list
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        // get event_live
        Map<String, EventLiveEntity> eventLiveMap = this.queryService.getEventLiveByEvent(event);
        // upsert entry_event_result
        RedisUtils.removeCacheByKey("get");
        List<EntryEventResultEntity> insertEventResultList = Lists.newArrayList();
        List<EntryEventResultEntity> updateEventResultList = Lists.newArrayList();
        entryList.forEach(entry -> {
            if (entry <= 0) {
                return;
            }
            UserPicksRes userPick = this.queryService.getUserPicks(event, entry);
            if (userPick == null) {
                return;
            }
            EntryEventResultEntity entryEventResultEntity = this.calcEntryEventPoints(event, entry, userPick, eventLiveMap);
            if (entryEventResultEntity == null) {
                return;
            }
            if (!entryEventResultMap.containsKey(entry)) {
                insertEventResultList.add(entryEventResultEntity);
            } else {
                entryEventResultEntity.setId(entryEventResultMap.get(entry).getId());
                updateEventResultList.add(entryEventResultEntity);
            }
        });
        this.entryEventResultService.saveBatch(insertEventResultList);
        log.info("event:{}, insert event result size:{}!", event, insertEventResultList.size());
        this.entryEventResultService.updateBatchById(updateEventResultList);
        log.info("event:{}, update event result size:{}!", event, updateEventResultList.size());
    }

    private EntryEventResultEntity calcEntryEventPoints(int event, int entry, UserPicksRes
            userPick, Map<String, EventLiveEntity> eventLiveMap) {
        Map<Integer, Integer> elementPointsMap = eventLiveMap.values()
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        int captain = this.getPlayedCaptain(userPick.getPicks(), eventLiveMap);
        EventLiveEntity captainEntity = eventLiveMap.getOrDefault(String.valueOf(captain), new EventLiveEntity());
        return new EntryEventResultEntity()
                .setEntry(entry)
                .setEvent(event)
                .setEventPoints(userPick.getEntryHistory().getPoints())
                .setEventTransfers(userPick.getEntryHistory().getEventTransfers())
                .setEventTransfersCost(userPick.getEntryHistory().getEventTransfersCost())
                .setEventNetPoints(userPick.getEntryHistory().getPoints() - userPick.getEntryHistory().getEventTransfersCost())
                .setEventBenchPoints(userPick.getEntryHistory().getPointsOnBench())
                .setEventAutoSubPoints(userPick.getAutomaticSubs().size() == 0 ? 0 : this.calcAutoSubPoints(userPick.getAutomaticSubs(), elementPointsMap))
                .setEventRank(userPick.getEntryHistory().getRank())
                .setEventChip(StringUtils.isBlank(userPick.getActiveChip()) ? Chip.NONE.getValue() : userPick.getActiveChip())
                .setEventPicks(this.setUserPicks(userPick.getPicks(), elementPointsMap))
                .setEventAutoSubs(this.setAutoSubs(userPick.getAutomaticSubs(), elementPointsMap))
                .setOverallPoints(userPick.getEntryHistory().getTotalPoints())
                .setOverallRank(userPick.getEntryHistory().getOverallRank())
                .setTeamValue(userPick.getEntryHistory().getValue())
                .setBank(userPick.getEntryHistory().getBank())
                .setPlayedCaptain(captain)
                .setCaptainPoints(captainEntity.getTotalPoints());
    }

    private int getPlayedCaptain(List<Pick> picks, Map<String, EventLiveEntity> eventLiveMap) {
        Pick captain = picks
                .stream()
                .filter(Pick::isCaptain)
                .findFirst()
                .orElse(null);
        Pick viceCaptain = picks
                .stream()
                .filter(Pick::isViceCaptain)
                .findFirst()
                .orElse(null);
        if (captain == null || viceCaptain == null) {
            return 0;
        }
        if (eventLiveMap.get(String.valueOf(captain.getElement())).getMinutes() == 0 && eventLiveMap.get(String.valueOf(viceCaptain.getElement())).getMinutes() > 0) {
            return viceCaptain.getElement();
        }
        return captain.getElement();
    }

    private int calcAutoSubPoints(List<AutoSubs> automaticSubs, Map<Integer, Integer> elementPointsMap) {
        return automaticSubs
                .stream()
                .mapToInt(o -> elementPointsMap.getOrDefault(o.getElementIn(), 0))
                .sum();
    }

    private String setUserPicks(List<Pick> picks, Map<Integer, Integer> elementPointsMap) {
        List<EntryPickData> pickList = Lists.newArrayList();
        picks.forEach(o -> pickList.add(
                new EntryPickData()
                        .setElement(o.getElement())
                        .setPosition(o.getPosition())
                        .setMultiplier(o.getMultiplier())
                        .setCaptain(o.isCaptain())
                        .setViceCaptain(o.isViceCaptain())
                        .setPoints(elementPointsMap.getOrDefault(o.getElement(), 0))
                )
        );
        return JsonUtils.obj2json(pickList);
    }

    private String setAutoSubs(List<AutoSubs> autoSubs, Map<Integer, Integer> elementPointsMap) {
        if (CollectionUtils.isEmpty(autoSubs)) {
            return "";
        }
        List<EntryEventAutoSubsData> autoSubList = Lists.newArrayList();
        autoSubs.forEach(o -> autoSubList.add(
                new EntryEventAutoSubsData()
                        .setElementIn(o.getElementIn())
                        .setElementInPoints(elementPointsMap.getOrDefault(o.getElementIn(), 0))
                        .setElementOut(o.getElementOut())
                        .setElementOutPoints(elementPointsMap.getOrDefault(o.getElementOut(), 0))
                )
        );
        return JsonUtils.obj2json(autoSubList);
    }

    @Override
    public void updateEntryEventTransfers(int event, int entry) {
        if (entry <= 0) {
            return;
        }
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .eq(EntryEventResultEntity::getEntry, entry));
        if (entryEventResultEntity == null) {
            return;
        }
        List<Integer> pickElementList = this.queryService.qryPickListFromPicks(entryEventResultEntity.getEventPicks())
                .stream()
                .map(EntryPickData::getElement)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(pickElementList)) {
            return;
        }
        List<EntryEventTransfersEntity> entryEventTransferEntityList = this.entryEventTransferService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                .eq(EntryEventTransfersEntity::getEvent, event)
                .eq(EntryEventTransfersEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventTransferEntityList)) {
            return;
        }
        Map<Integer, Integer> pointsMap = this.queryService.getEventLiveByEvent(event).values()
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        if (CollectionUtils.isEmpty(pointsMap)) {
            return;
        }
        List<EntryEventTransfersEntity> list = Lists.newArrayList();
        entryEventTransferEntityList.forEach(o -> {
            o
                    .setElementInPoints(pointsMap.getOrDefault(o.getElementIn(), 0))
                    .setElementInPlayed(StringUtils.equals(Chip.BB.getValue(), entryEventResultEntity.getEventChip()) ?
                            pickElementList.contains(o.getElementIn()) : pickElementList.subList(0, 11).contains(o.getElementIn()))
                    .setElementOutPoints(pointsMap.getOrDefault(o.getElementOut(), 0));
            list.add(o);
        });
        this.entryEventTransferService.updateBatchById(list);
    }

    private List<EntryEventTransfersEntity> getEntryEventTransfer(List<TransferRes> transferResList) {
        List<EntryEventTransfersEntity> list = Lists.newArrayList();
        transferResList.forEach(o ->
                list.add(
                        new EntryEventTransfersEntity()
                                .setEntry(o.getEntry())
                                .setEvent(o.getEvent())
                                .setElementIn(o.getElementIn())
                                .setElementInPlayed(false)
                                .setElementInPoints(0)
                                .setElementInCost(o.getElementInCost())
                                .setElementOut(o.getElementOut())
                                .setElementOutCost(o.getElementOutCost())
                                .setElementOutPoints(0)
                                .setTime(o.getTime())
                ));
        return list;
    }


    @Override
    public void updateEventTransfersByEntryList(int event, List<Integer> entryList) {
        // get entry_list
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        if (CollectionUtils.isEmpty(entryEventResultMap)) {
            return;
        }
        Map<Integer, Integer> pointsMap = this.queryService.getEventLiveByEvent(event).values()
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        if (CollectionUtils.isEmpty(pointsMap)) {
            return;
        }
        Multimap<Integer, EntryEventTransfersEntity> entryEventTransferMap = HashMultimap.create();
        this.entryEventTransferService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                .eq(EntryEventTransfersEntity::getEvent, event)
                .in(EntryEventTransfersEntity::getEntry, entryList))
                .forEach(o -> entryEventTransferMap.put(o.getEntry(), o));
        if (entryEventTransferMap.size() == 0) {
            return;
        }
        List<EntryEventTransfersEntity> list = Lists.newArrayList();
        entryList.forEach(entry -> {
            EntryEventResultEntity entryEventResultEntity = entryEventResultMap.getOrDefault(entry, null);
            if (entryEventResultEntity == null) {
                return;
            }
            List<EntryPickData> pickList = JsonUtils.json2Collection(entryEventResultEntity.getEventPicks(), List.class, EntryPickData.class);
            if (CollectionUtils.isEmpty(pickList)) {
                return;
            }
            List<Integer> pickElementList = pickList
                    .stream()
                    .map(EntryPickData::getElement)
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(pickElementList)) {
                return;
            }
            entryEventTransferMap.get(entry).forEach(o -> {
                o
                        .setElementInPoints(pointsMap.getOrDefault(o.getElementIn(), 0))
                        .setElementInPlayed(StringUtils.equals(Chip.BB.getValue(), entryEventResultEntity.getEventChip()) ?
                                pickElementList.contains(o.getElementIn()) : pickElementList.subList(0, 11).contains(o.getElementIn()))
                        .setElementOutPoints(pointsMap.getOrDefault(o.getElementOut(), 0));
                list.add(o);
            });
        });
        this.entryEventTransferService.updateBatchById(list);
        log.info("update event transfers size:{}", list.size());
    }

    @Override
    public void upsertEntryEventCupResult(int event, int entry) {
        if (entry < 0) {
            return;
        }
        EntryCupRes entryCupRes = this.queryService.getEntryCup(entry);
        if (entryCupRes == null) {
            return;
        }
        Match cupMatch = entryCupRes.getCupMatches()
                .stream()
                .filter(o -> o.getEvent() == event)
                .findFirst()
                .orElse(null);
        if (cupMatch == null) {
            return;
        }
        // entry_event_cup_result
        EntryEventCupResultEntity entryEventCupResult = this.entryEventCupResultService.getOne(new QueryWrapper<EntryEventCupResultEntity>().lambda()
                .eq(EntryEventCupResultEntity::getEvent, event)
                .eq(EntryEventCupResultEntity::getEntry, entry))
                .setEntryName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1Name() : cupMatch.getEntry2Name())
                .setPlayerName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1PlayerName() : cupMatch.getEntry2PlayerName())
                .setEventPoints(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1Points() : cupMatch.getEntry2Points())
                .setAgainstEntry(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Entry() : cupMatch.getEntry1Entry())
                .setAgainstEntryName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Name() : cupMatch.getEntry1Name())
                .setAgainstPlayerName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2PlayerName() : cupMatch.getEntry1PlayerName())
                .setAgainstEventPoints(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Points() : cupMatch.getEntry1Points());
        if (cupMatch.getWinner() == 0) {
            if (entryEventCupResult.getEventPoints() >= entryEventCupResult.getAgainstEventPoints()) {
                entryEventCupResult.setResult("Win");
            }
        } else if (cupMatch.getWinner() == entryEventCupResult.getEntry()) {
            entryEventCupResult.setResult("Win");
        } else {
            entryEventCupResult.setResult("Lose");
        }
        this.entryEventCupResultService.updateById(entryEventCupResult);
    }

    @Override
    public void upsertEventCupResultByEntryList(int event, List<Integer> entryList) {
        // get entry_list
        Map<Integer, EntryEventCupResultEntity> entryEventCupResultMap = this.entryEventCupResultService.list(new QueryWrapper<EntryEventCupResultEntity>().lambda()
                .eq(EntryEventCupResultEntity::getEvent, event)
                .in(EntryEventCupResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventCupResultEntity::getEntry, o -> o));
        // upsert entry_event_result
        List<EntryEventCupResultEntity> updateEventCupResultList = Lists.newArrayList();
        entryList.forEach(entry -> {
            EntryCupRes entryCupRes = this.queryService.getEntryCup(entry);
            if (entryCupRes == null) {
                return;
            }
            Match cupMatch = entryCupRes.getCupMatches()
                    .stream()
                    .filter(o -> o.getEvent() == event)
                    .findFirst()
                    .orElse(null);
            if (cupMatch == null) {
                return;
            }
            // entry_event_cup_result
            EntryEventCupResultEntity entryEventCupResult = entryEventCupResultMap.get(entry)
                    .setEntryName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1Name() : cupMatch.getEntry2Name())
                    .setPlayerName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1PlayerName() : cupMatch.getEntry2PlayerName())
                    .setEventPoints(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1Points() : cupMatch.getEntry2Points())
                    .setAgainstEntry(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Entry() : cupMatch.getEntry1Entry())
                    .setAgainstEntryName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Name() : cupMatch.getEntry1Name())
                    .setAgainstPlayerName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2PlayerName() : cupMatch.getEntry1PlayerName())
                    .setAgainstEventPoints(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Points() : cupMatch.getEntry1Points());
            if (cupMatch.getWinner() == 0) {
                if (entryEventCupResult.getEventPoints() >= entryEventCupResult.getAgainstEventPoints()) {
                    entryEventCupResult.setResult("Win");
                }
            } else if (cupMatch.getWinner() == entryEventCupResult.getEntry()) {
                entryEventCupResult.setResult("Win");
            } else {
                entryEventCupResult.setResult("Lose");
            }
            updateEventCupResultList.add(entryEventCupResult);
        });
        this.entryEventCupResultService.updateBatchById(updateEventCupResultList);
        log.info("event:{}, update tournament entry event cup result size:{}!", event, updateEventCupResultList.size());
    }

    @Override
    public void updatePointsRaceGroupResult(int event, int tournamentId) {
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("tournament:{}, tournament_info not exists!", tournamentId);
            return;
        }
        if (!StringUtils.equals(GroupMode.Points_race.name(), tournamentInfoEntity.getGroupMode())) {
            log.error("tournament:{}, not points group!", tournamentId);
            return;
        }
        // check gw
        int groupStartGw = tournamentInfoEntity.getGroupStartGw();
        int groupEndGw = tournamentInfoEntity.getGroupEndGw();
        if (event > groupEndGw) {
            log.error("tournament:{}, group stage passed,current event:{}!", event, tournamentId);
            return;
        }
        // entry list
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // entry_event_result
        Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("tournament:{}, event:{}, event_result not updated!", event, tournamentId);
            return;
        }
        // tournament_group
        List<TournamentGroupEntity> tournamentGroupEntityList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .in(TournamentGroupEntity::getEntry, entryList));
        if (CollectionUtils.isEmpty(tournamentGroupEntityList)) {
            log.error("tournament:{}, tournament_group not exists!", tournamentId);
            return;
        }
        Map<Integer, TournamentPointsGroupResultEntity> tournamentPointsGroupResultEntityMap = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                .eq(TournamentPointsGroupResultEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(TournamentPointsGroupResultEntity::getEntry, o -> o));
        // update tournament_group and tournament_group_result
        List<TournamentGroupEntity> updateGroupList = Lists.newArrayList();
        List<TournamentPointsGroupResultEntity> updateGroupPointsResultList = Lists.newArrayList();
        // tournament_group
        tournamentGroupEntityList.forEach(tournamentGroupEntity -> {
            int entry = tournamentGroupEntity.getEntry();
            EntryEventResultEntity entryEventResultEntity = eventResultMap.getOrDefault(entry, null);
            if (entryEventResultEntity == null) {
                log.error("tournament:{}, event:{}, entry:{}, event_result not updated!", event, tournamentId, entry);
                return;
            }
            tournamentGroupEntity
                    .setPlay(event - tournamentGroupEntity.getStartGw() + 1)
                    .setTotalPoints(this.entryEventResultService.sumEventPoints(event, groupStartGw, groupEndGw, entry))
                    .setTotalTransfersCost(this.entryEventResultService.sumEventTransferCost(event, groupStartGw, groupEndGw, entry))
                    .setTotalNetPoints(this.entryEventResultService.sumEventNetPoints(event, groupStartGw, groupEndGw, entry))
                    .setOverallRank(entryEventResultEntity.getOverallRank());
            // tournament_points_group_result
            TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity = tournamentPointsGroupResultEntityMap.get(entry);
            if (tournamentPointsGroupResultEntity == null) {
                return;
            }
            tournamentPointsGroupResultEntity
                    .setEventPoints(entryEventResultEntity.getEventPoints())
                    .setEventCost(entryEventResultEntity.getEventTransfersCost())
                    .setEventNetPoints(entryEventResultEntity.getEventPoints() - entryEventResultEntity.getEventTransfersCost())
                    .setEventRank(entryEventResultEntity.getEventRank());
        });
        // sort group rank
        Map<String, Integer> groupRankMap = this.sortPointsRaceGroupRank(tournamentGroupEntityList);  // key:overall_rank -> value:group_rank
        tournamentGroupEntityList.forEach(tournamentGroupEntity -> {
            int groupRank = groupRankMap.getOrDefault(tournamentGroupEntity.getTotalNetPoints() + "-" + tournamentGroupEntity.getOverallRank(), 0);
            tournamentGroupEntity
                    .setGroupPoints(tournamentGroupEntity.getTotalNetPoints())
                    .setGroupRank(groupRank);
            updateGroupList.add(tournamentGroupEntity);
            TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity = tournamentPointsGroupResultEntityMap.get(tournamentGroupEntity.getEntry());
            if (tournamentPointsGroupResultEntity == null) {
                return;
            }
            tournamentPointsGroupResultEntity.setEventGroupRank(groupRank);
            updateGroupPointsResultList.add(tournamentPointsGroupResultEntity);
        });
        // update
        this.tournamentGroupService.updateBatchById(updateGroupList);
        log.info("tournament:{}, event:{}, update tournament group!", tournamentId, event);
        this.tournamentPointsGroupResultService.updateBatchById(updateGroupPointsResultList);
        log.info("tournament:{}, event:{}, update tournament points group result!", tournamentId, event);
    }

    private Map<Integer, EntryEventResultEntity> getEntryEventResultByEvent(int event, List<Integer> entryList) {
        return this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
    }

    @Override
    public void updateBattleRaceGroupResult(int event, int tournamentId) {
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("tournament:{}, tournament_info not exists!", tournamentId);
            return;
        }
        if (!StringUtils.equals(GroupMode.Battle_race.name(), tournamentInfoEntity.getGroupMode())) {
            log.error("tournament:{}, not battle group!", tournamentId);
            return;
        }
        // check gw
        int groupStartGw = tournamentInfoEntity.getGroupStartGw();
        int groupEndGw = tournamentInfoEntity.getGroupEndGw();
        if (event > groupEndGw) {
            log.error("tournament:{}, event:{}, group stage passed!", tournamentId, event);
            return;
        }
        // tournament_entry
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // entry_event_result
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(entryEventResultMap)) {
            log.error("tournament:{}, event:{}, event_result not update!", tournamentId, event);
            return;
        }
        // tournament_group_battle_result
        Table<Integer, Integer, Integer> battleResultTable = this.updateGroupBattleResult(event, tournamentId, entryEventResultMap);
        // tournament_group
        this.updateTournamentGroup(event, tournamentId, tournamentInfoEntity.getGroupQualifiers(), groupStartGw, groupEndGw, battleResultTable, entryEventResultMap);
    }

    private Table<Integer, Integer, Integer> updateGroupBattleResult(int event, int tournamentId, Map<
            Integer, EntryEventResultEntity> entryEventResultMap) {
        List<TournamentBattleGroupResultEntity> tournamentBattleGroupResultList = Lists.newArrayList();
        Table<Integer, Integer, Integer> battleResultTable = HashBasedTable.create(); // groupId -> entry -> matchPoints
        this.tournamentBattleGroupResultService.list(new QueryWrapper<TournamentBattleGroupResultEntity>()
                .lambda()
                .eq(TournamentBattleGroupResultEntity::getTournamentId, tournamentId)
                .eq(TournamentBattleGroupResultEntity::getEvent, event))
                .forEach(groupBattleResult -> {
                    int homeEntry = groupBattleResult.getHomeEntry();
                    int awayEntry = groupBattleResult.getAwayEntry();
                    EntryEventResultEntity homeEventResult = entryEventResultMap.getOrDefault(homeEntry, new EntryEventResultEntity());
                    EntryEventResultEntity awayEventResult = entryEventResultMap.getOrDefault(awayEntry, new EntryEventResultEntity());
                    int homeEntryMatchPoints = this.getGroupBattleHomeEntryResult(homeEventResult, awayEventResult);
                    int awayEntryMatchPoints = this.getGroupBattleHomeEntryResult(awayEventResult, homeEventResult);
                    if (homeEntry != 0) {
                        battleResultTable.put(groupBattleResult.getGroupId(), homeEntry, homeEntryMatchPoints);
                    }
                    if (awayEntry != 0) {
                        battleResultTable.put(groupBattleResult.getGroupId(), awayEntry, awayEntryMatchPoints);
                    }
                    tournamentBattleGroupResultList.add(groupBattleResult
                            .setHomeEntryNetPoints(homeEventResult.getEventNetPoints())
                            .setHomeEntryRank(homeEventResult.getEventRank())
                            .setHomeEntryMatchPoints(homeEntryMatchPoints)
                            .setAwayEntryNetPoints(awayEventResult.getEventNetPoints())
                            .setAwayEntryRank(awayEventResult.getEventRank())
                            .setAwayEntryMatchPoints(awayEntryMatchPoints)
                    );
                });
        this.tournamentBattleGroupResultService.updateBatchById(tournamentBattleGroupResultList);
        // return
        return battleResultTable;
    }

    private int getGroupBattleHomeEntryResult(EntryEventResultEntity firstEventResult, EntryEventResultEntity
            secondEventResult) {
        if (firstEventResult.getEventNetPoints() > secondEventResult.getEventNetPoints()) {
            return 3;
        } else if (firstEventResult.getEventNetPoints() < secondEventResult.getEventNetPoints()) {
            return 0;
        } else {
            return 1;
        }
    }

    private void updateTournamentGroup(int event, int tournamentId, int qualifiers, int startGw, int endGw, Table<
            Integer, Integer, Integer> battleResultTable, Map<Integer, EntryEventResultEntity> entryEventResultMap) {
        List<TournamentGroupEntity> tournamentGroupList = Lists.newArrayList();
        int playedEvent = event - startGw + 1;
        battleResultTable.rowKeySet().forEach(groupId -> {
            // prepare
            Map<Integer, Integer> matchResultMap = battleResultTable.row(groupId); // entry -> matchPoints
            List<TournamentGroupEntity> groupList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                    .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                    .eq(TournamentGroupEntity::getGroupId, groupId)
                    .orderByAsc(TournamentGroupEntity::getGroupIndex));
            groupList.forEach(tournamentGroupEntity -> {
                int entry = tournamentGroupEntity.getEntry();
                // total_points and overall_rank
                EntryEventResultEntity entryEventResult = entryEventResultMap.getOrDefault(entry, null);
                if (entryEventResult != null) {
                    tournamentGroupEntity
                            .setTotalPoints(this.entryEventResultService.sumEventPoints(event, startGw, endGw, entry))
                            .setTotalTransfersCost(this.entryEventResultService.sumEventTransferCost(event, startGw, endGw, entry))
                            .setTotalNetPoints(this.entryEventResultService.sumEventNetPoints(event, startGw, endGw, entry))
                            .setOverallRank(entryEventResult.getOverallRank());
                }
                if (tournamentGroupEntity.getPlay() == playedEvent) {
                    return;
                }
                // group points
                int matchPoints = matchResultMap.getOrDefault(entry, 0);
                tournamentGroupEntity
                        .setGroupPoints(tournamentGroupEntity.getGroupPoints() + matchPoints)
                        .setPlay(tournamentGroupEntity.getPlay() + 1);
                if (matchPoints == 3) {
                    tournamentGroupEntity.setWin(tournamentGroupEntity.getWin() + 1);
                } else if (matchPoints == 0) {
                    tournamentGroupEntity.setLose(tournamentGroupEntity.getLose() + 1);
                } else {
                    tournamentGroupEntity.setDraw(tournamentGroupEntity.getDraw() + 1);
                }
            });
            // sort group list by group points
            Map<String, Integer> groupRankMap = this.sortBattleGroupRank(groupList); // entry -> groupRank
            groupList.forEach(tournamentGroupEntity ->
                    tournamentGroupList.add(tournamentGroupEntity
                            .setGroupRank(groupRankMap.getOrDefault(tournamentGroupEntity.getGroupPoints() + "-" + tournamentGroupEntity.getOverallRank(), 0))
                            .setQualified(tournamentGroupEntity.getGroupRank() <= qualifiers)
                    )
            );
        });
        this.tournamentGroupService.updateBatchById(tournamentGroupList);
    }

    private Map<Integer, Map<String, Integer>> sortZjTournamentPhaseOneGroupRank
            (List<TournamentGroupEntity> tournamentGroupEntityList) {
        // groupIdList
        Map<Integer, List<TournamentGroupEntity>> groupEntityMap = Maps.newHashMap();
        tournamentGroupEntityList.forEach(o -> {
            int groupId = o.getGroupId();
            List<TournamentGroupEntity> list = Lists.newArrayList();
            if (groupEntityMap.containsKey(groupId)) {
                list = groupEntityMap.get(groupId);
            }
            list.add(o);
            groupEntityMap.put(groupId, list);
        });
        Map<Integer, Map<String, Integer>> map = Maps.newHashMap();
        groupEntityMap.keySet().forEach(groupId -> {
            Map<String, Integer> groupRankMap = this.sortPointsRaceEachGroupRank(groupEntityMap.get(groupId));
            map.put(groupId, groupRankMap);
        });
        return map;
    }

    private Map<String, Integer> sortPointsRaceGroupRank(List<TournamentGroupEntity> tournamentGroupEntityList) {
        Map<Integer, List<TournamentGroupEntity>> groupEntityMap = Maps.newHashMap();
        tournamentGroupEntityList.forEach(o -> {
            int groupId = o.getGroupId();
            List<TournamentGroupEntity> list = Lists.newArrayList();
            if (groupEntityMap.containsKey(groupId)) {
                list = groupEntityMap.get(groupId);
            }
            list.add(o);
            groupEntityMap.put(groupId, list);
        });
        Map<String, Integer> map = Maps.newHashMap();
        groupEntityMap.keySet().forEach(groupId -> {
            Map<String, Integer> groupRankMap = this.sortPointsRaceEachGroupRank(groupEntityMap.get(groupId));
            map.putAll(groupRankMap);
        });
        return map;
    }

    private Map<String, Integer> sortPointsRaceEachGroupRank
            (List<TournamentGroupEntity> tournamentGroupEntityList) {
        Map<String, Integer> groupRankMap = Maps.newHashMap(); // entry -> groupRank
        Map<String, Integer> groupRankCountMap = Maps.newLinkedHashMap();
        tournamentGroupEntityList
                .stream()
                .filter(o -> o.getTotalNetPoints() != 0)
                .sorted(Comparator.comparing(TournamentGroupEntity::getTotalNetPoints).reversed()
                        .thenComparing(TournamentGroupEntity::getOverallRank))
                .forEachOrdered(o -> this.setGroupRankMapValue(o.getTotalNetPoints() + "-" + o.getOverallRank(), groupRankCountMap));
        tournamentGroupEntityList
                .stream()
                .filter(o -> o.getTotalNetPoints() == 0)
                .sorted(Comparator.comparingInt(TournamentGroupEntity::getEntry))
                .forEachOrdered(o -> this.setGroupRankMapValue(o.getTotalNetPoints() + "-" + o.getOverallRank(), groupRankCountMap));
        int index = 1;
        for (String key :
                groupRankCountMap.keySet()) {
            groupRankMap.put(key, index);
            index += groupRankCountMap.get(key);
        }
        return groupRankMap;
    }

    private Map<String, Integer> sortBattleGroupRank(List<TournamentGroupEntity> tournamentGroupEntityList) {
        Map<String, Integer> groupRankMap = Maps.newLinkedHashMap();
        Map<String, Integer> groupRankCountMap = Maps.newLinkedHashMap();
        tournamentGroupEntityList.stream()
                .filter(o -> o.getTotalPoints() != 0)
                .sorted(Comparator.comparing(TournamentGroupEntity::getGroupPoints).reversed()
                        .thenComparing(TournamentGroupEntity::getOverallRank))
                .forEachOrdered(o -> this.setGroupRankMapValue(o.getGroupPoints() + "-" + o.getOverallRank(), groupRankCountMap));
        tournamentGroupEntityList.stream()
                .filter(o -> o.getTotalPoints() == 0)
                .sorted(Comparator.comparingInt(TournamentGroupEntity::getEntry))
                .forEachOrdered(o -> this.setGroupRankMapValue(o.getGroupPoints() + "-" + o.getOverallRank(), groupRankCountMap));
        int index = 1;
        for (String key :
                groupRankCountMap.keySet()) {
            groupRankMap.put(key, index);
            index += groupRankCountMap.get(key);
        }
        return groupRankMap;
    }

    private void setGroupRankMapValue(String key, Map<String, Integer> groupRankCountMap) {
        if (groupRankCountMap.containsKey(key)) {
            groupRankCountMap.put(key, groupRankCountMap.get(key) + 1);
        } else {
            groupRankCountMap.put(key, 1);
        }
    }

    @Override
    public void updateKnockoutResult(int event, int tournamentId) {
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("tournament:{}, tournament_info not exists!", tournamentId);
            return;
        }
        if (StringUtils.equals(KnockoutMode.No_knockout.name(), tournamentInfoEntity.getKnockoutMode())) {
            log.error("tournament:{}, no knockout!", tournamentId);
            return;
        }
        // check gw
        int knockoutEndGw = tournamentInfoEntity.getKnockoutEndGw();
        if (event > knockoutEndGw) {
            log.error("tournament:{}, event:{}, knockout stage passed!", tournamentId, event);
            return;
        }
        // get entry_list by tournament
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // get event_result list
        Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("tournament:{}, event:{}, event_result not update!", tournamentId, event);
            return;
        }
        // tournament_knockout_result
        Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap = this.updateKnockoutResult(tournamentId, event, eventResultMap);
        if (CollectionUtils.isEmpty(knockoutResultDataMap.values())) {
            return;
        }
        // tournament_knockout
        Map<Integer, TournamentKnockoutNextRoundData> nextKnockoutMap = this.updateKnockoutInfo(tournamentId, event, knockoutResultDataMap);
        if (CollectionUtils.isEmpty(nextKnockoutMap)) {
            return;
        }
        // next round entry
        this.updateNextKnockout(tournamentId, nextKnockoutMap);
    }

    private Multimap<Integer, TournamentKnockoutResultData> updateKnockoutResult(int tournamentId, int event, Map<
            Integer, EntryEventResultEntity> eventResultMap) {
        List<TournamentKnockoutResultEntity> tournamentKnockoutResultList = Lists.newArrayList();
        // matchId -> tournament_knockout_result data
        Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap = HashMultimap.create();
        // tournament_knockout_result
        this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                .eq(TournamentKnockoutResultEntity::getEvent, event)
                .orderByAsc(TournamentKnockoutResultEntity::getMatchId))
                .forEach(knockoutResult -> {
                    int homeEntry = knockoutResult.getHomeEntry();
                    int awayEntry = knockoutResult.getAwayEntry();
                    EntryEventResultEntity homeEventResult = eventResultMap.getOrDefault(homeEntry, new EntryEventResultEntity());
                    EntryEventResultEntity awayEventResult = eventResultMap.getOrDefault(awayEntry, new EntryEventResultEntity());
                    int matchWinner = this.getMatchWinner(homeEntry, awayEntry, homeEventResult, awayEventResult);
                    knockoutResultDataMap.put(knockoutResult.getMatchId(), new TournamentKnockoutResultData()
                            .setEvent(event)
                            .setPlayAgainstId(knockoutResult.getPlayAgainstId())
                            .setMatchId(knockoutResult.getMatchId())
                            .setMatchWinner(matchWinner)
                            .setWinnerRank(matchWinner == homeEntry ? homeEventResult.getOverallRank() : awayEventResult.getOverallRank())
                    );
                    tournamentKnockoutResultList.add(knockoutResult
                            .setHomeEntryNetPoints(homeEntry > 0 ? homeEventResult.getEventNetPoints() : 0)
                            .setHomeEntryRank(homeEntry > 0 ? homeEventResult.getEventRank() : 0)
                            .setAwayEntryNetPoints(awayEntry > 0 ? awayEventResult.getEventNetPoints() : 0)
                            .setAwayEntryRank(awayEntry > 0 ? awayEventResult.getEventRank() : 0)
                            .setMatchWinner(matchWinner));
                });
        this.tournamentKnockoutResultService.updateBatchById(tournamentKnockoutResultList);
        log.info("tournament:{}, event:{}, update tournament knockout result!", tournamentId, event);
        return knockoutResultDataMap;
    }

    private Map<Integer, TournamentKnockoutNextRoundData> updateKnockoutInfo(int tournamentId, int event, Multimap<
            Integer, TournamentKnockoutResultData> knockoutResultDataMap) {
        List<TournamentKnockoutEntity> tournamentKnockoutList = Lists.newArrayList();
        // next_match_id -> tournament_knockout_result data
        Map<Integer, TournamentKnockoutNextRoundData> nextKnockoutMap = Maps.newHashMap();
        Map<Integer, TournamentKnockoutEntity> knockoutMap = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                .eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
                .eq(TournamentKnockoutEntity::getEndGw, event)
                .orderByAsc(TournamentKnockoutEntity::getMatchId))
                .stream()
                .collect(Collectors.toMap(TournamentKnockoutEntity::getMatchId, v -> v));
        // update by match id
        knockoutResultDataMap.keySet().forEach(matchId ->
                knockoutResultDataMap.get(matchId).forEach(resultData -> {
                    // update tournament_knockout
                    if (!knockoutMap.containsKey(matchId)) {
                        return;
                    }
                    TournamentKnockoutEntity knockoutEntity = knockoutMap.get(matchId);
                    if (resultData.getEvent() != knockoutEntity.getEndGw()) { // round not finished
                        return;
                    }
                    knockoutEntity.setRoundWinner(this.getRoundWinner(knockoutResultDataMap.get(matchId)));
                    tournamentKnockoutList.add(knockoutEntity);
                    // set next round data
                    this.setNextRoundData(nextKnockoutMap, knockoutEntity);
                })
        );
        this.tournamentKnockoutService.updateBatchById(tournamentKnockoutList);
        log.info("tournament:{}, event:{}, update tournament knockout info!", tournamentId, event);
        return nextKnockoutMap;
    }

    private void updateNextKnockout(int tournamentId, Map<
            Integer, TournamentKnockoutNextRoundData> nextKnockoutMap) {
        // get round
        int nextRound = nextKnockoutMap.values()
                .stream()
                .map(TournamentKnockoutNextRoundData::getNextRound)
                .findFirst()
                .orElse(0);
        if (nextRound == 0) {
            return;
        }
        // tournament_knockout
        List<TournamentKnockoutEntity> tournamentKnockoutEntityList = Lists.newArrayList();
        this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                .eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
                .eq(TournamentKnockoutEntity::getRound, nextRound))
                .forEach(knockoutEntity -> tournamentKnockoutEntityList.add(knockoutEntity
                        .setHomeEntry(nextKnockoutMap.get(knockoutEntity.getMatchId()).getNextRoundHomeEntry())
                        .setAwayEntry(nextKnockoutMap.get(knockoutEntity.getMatchId()).getNextRoundAwayEntry())));
        // tournament_knockout_result
        List<TournamentKnockoutResultEntity> tournamentKnockoutResultList = Lists.newArrayList();
        this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                .in(TournamentKnockoutResultEntity::getMatchId, nextKnockoutMap.keySet()))
                .forEach(knockoutResultEntity -> tournamentKnockoutResultList.add(knockoutResultEntity
                        .setHomeEntry(nextKnockoutMap.get(knockoutResultEntity.getMatchId()).getNextRoundHomeEntry())
                        .setAwayEntry(nextKnockoutMap.get(knockoutResultEntity.getMatchId()).getNextRoundAwayEntry())));
        this.tournamentKnockoutService.updateBatchById(tournamentKnockoutEntityList);
        this.tournamentKnockoutResultService.updateBatchById(tournamentKnockoutResultList);
        log.info("tournament:{}, update tournament next knockout info!", tournamentId);
    }

    private int getMatchWinner(int homeEntry, int awayEntry, EntryEventResultEntity
            homeEntryResult, EntryEventResultEntity awayEntryResult) {
        // if blank
        if (homeEntry <= 0) {
            return awayEntry;
        } else if (awayEntry <= 0) {
            return homeEntry;
        }
        // compare order: net points; overall rank; random
        int winner = this.compareNetPoint(homeEntry, homeEntryResult.getEventNetPoints(), awayEntry, awayEntryResult.getEventNetPoints());
        if (winner != 0) {
            return winner;
        }
        winner = this.compareRank(homeEntry, homeEntryResult.getOverallRank(), awayEntry, awayEntryResult.getOverallRank());
        if (winner != 0) {
            return winner;
        }
        return this.randomWinner(homeEntry, awayEntry);
    }

    private int compareNetPoint(int homeEntry, int homeNetPoint, int awayEntry, int awayNetPoint) {
        if (homeNetPoint > awayNetPoint) {
            return homeEntry;
        } else if (homeNetPoint < awayNetPoint) {
            return awayEntry;
        }
        return 0;
    }

    private int compareRank(int homeEntry, int homeRank, int awayEntry, int awayRank) {
        if (homeRank > awayRank) {
            return homeEntry;
        } else if (homeRank < awayRank) {
            return awayEntry;
        }
        return 0;
    }

    private int randomWinner(int homeEntry, int awayEntry) {
        if (new Random().nextInt(10) % 2 == 0) {
            return homeEntry;
        } else {
            return awayEntry;
        }
    }

    private int getRoundWinner(Collection<TournamentKnockoutResultData> collection) {
        if (collection.size() == 1) {
            return collection.stream().map(TournamentKnockoutResultData::getMatchWinner).findFirst().orElse(0);
        }
        List<TournamentKnockoutResultData> winners = new ArrayList<>(collection);
        int firstWinner = winners.get(0).getMatchWinner();
        Map<Integer, Integer> secondWinnerMap = collection.stream()
                .filter(tournamentKnockoutResultData -> tournamentKnockoutResultData.getMatchWinner() != firstWinner)
                .limit(1)
                .collect(Collectors.toMap(TournamentKnockoutResultData::getMatchWinner, TournamentKnockoutResultData::getWinnerRank)); // find the other match winner
        int secondWinner = secondWinnerMap.keySet().stream().findFirst().orElse(0);
        if (secondWinner == 0) { // all matches won by the first winner
            return firstWinner;
        }
        // tie, compare rank, then random
        int roundWinner = this.compareRank(firstWinner, winners.get(0).getWinnerRank(), secondWinner, secondWinnerMap.get(secondWinner));
        if (roundWinner != 0) {
            return roundWinner;
        }
        return this.randomWinner(firstWinner, secondWinner);
    }

    private void setNextRoundData
            (Map<Integer, TournamentKnockoutNextRoundData> nextKnockoutMap, TournamentKnockoutEntity knockoutEntity) {
        TournamentKnockoutNextRoundData nextRoundData = nextKnockoutMap.getOrDefault(knockoutEntity.getNextMatchId(), new TournamentKnockoutNextRoundData());
        nextRoundData.setNextMatchId(knockoutEntity.getNextMatchId());
        nextRoundData.setNextRound(knockoutEntity.getRound() + 1);
        if (knockoutEntity.getMatchId() % 2 == 1) {
            nextRoundData.setNextRoundHomeEntry(knockoutEntity.getRoundWinner());
        } else {
            nextRoundData.setNextRoundAwayEntry(knockoutEntity.getRoundWinner());
        }
        nextKnockoutMap.put(nextRoundData.getNextMatchId(), nextRoundData);
    }

    @Override
    public void updateZjPhaseOneResult(int event, int tournamentId) {
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("tournament:{}, tournament_info not exists!", tournamentId);
            return;
        }
        int groupNum = tournamentInfoEntity.getGroupNum();
        // get group_id
        List<Integer> groupIdList = Lists.newArrayList();
        IntStream.rangeClosed(1, groupNum).forEach(groupIdList::add);
        // get entry_list by tournament
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // entry_event_result list
        Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("tournament:{}, event:{}, event_result not updated!", tournamentId, event);
            return;
        }
        // tournament_group
        List<TournamentGroupEntity> tournamentGroupEntityList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .in(TournamentGroupEntity::getGroupId, groupIdList));
        if (CollectionUtils.isEmpty(tournamentGroupEntityList)) {
            log.error("tournament:{}, tournament_group not exists!", tournamentId);
            return;
        }
        Map<Integer, TournamentPointsGroupResultEntity> tournamentPointsGroupResultEntityMap = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                .eq(TournamentPointsGroupResultEntity::getEvent, event)
                .in(TournamentPointsGroupResultEntity::getGroupId, groupIdList))
                .stream()
                .collect(Collectors.toMap(TournamentPointsGroupResultEntity::getEntry, o -> o));
        // phase one params
        int phaseOneStartGw = tournamentGroupEntityList.get(0).getStartGw();
        if (event < phaseOneStartGw) {
            log.error("tournament:{}, event:{}, phase one not start!", tournamentId, event);
            return;
        }
        int phaseOneEndGw = tournamentGroupEntityList.get(0).getEndGw();
        if (event > phaseOneEndGw) {
            log.error("tournament:{}, event:{}, phase one passed!", tournamentId, event);
            return;
        }
        // update phase one result
        List<TournamentGroupEntity> updateGroupList = Lists.newArrayList();
        List<TournamentPointsGroupResultEntity> updateGroupPointsResultList = Lists.newArrayList();
        // tournament_group
        tournamentGroupEntityList.forEach(tournamentGroupEntity -> {
            int entry = tournamentGroupEntity.getEntry();
            EntryEventResultEntity entryEventResultEntity = eventResultMap.getOrDefault(tournamentGroupEntity.getEntry(), null);
            if (entryEventResultEntity == null) {
                log.error("tournament:{}, event:{}, entry:{}, event_result not updated!", tournamentId, event, entry);
                return;
            }
            tournamentGroupEntity
                    .setPlay(event - phaseOneStartGw + 1)
                    .setTotalPoints(this.entryEventResultService.sumEventPoints(event, phaseOneStartGw, phaseOneEndGw, entry))
                    .setTotalTransfersCost(this.entryEventResultService.sumEventTransferCost(event, phaseOneStartGw, phaseOneEndGw, entry))
                    .setTotalNetPoints(this.entryEventResultService.sumEventNetPoints(event, phaseOneStartGw, phaseOneEndGw, entry))
                    .setOverallRank(entryEventResultEntity.getOverallRank());
            // tournament_points_group_result
            TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity = tournamentPointsGroupResultEntityMap.get(entry);
            if (tournamentPointsGroupResultEntity == null) {
                return;
            }
            tournamentPointsGroupResultEntity
                    .setEventPoints(entryEventResultEntity.getEventPoints())
                    .setEventCost(entryEventResultEntity.getEventTransfersCost())
                    .setEventNetPoints(entryEventResultEntity.getEventPoints() - entryEventResultEntity.getEventTransfersCost())
                    .setEventRank(entryEventResultEntity.getEventRank());
        });
        // sort group rank
        Map<Integer, Map<String, Integer>> tournamentGroupRankMap = this.sortZjTournamentPhaseOneGroupRank(tournamentGroupEntityList); // key:groupId -> value(key:overall_rank -> value:group_rank）
        tournamentGroupEntityList.forEach(tournamentGroupEntity -> {
            Map<String, Integer> groupRankMap = tournamentGroupRankMap.get(tournamentGroupEntity.getGroupId());  // key:overall_rank -> value:group_rank
            int groupRank = groupRankMap.getOrDefault(tournamentGroupEntity.getTotalPoints() + "-" + tournamentGroupEntity.getOverallRank(), 0);
            tournamentGroupEntity
                    .setGroupPoints(tournamentGroupEntity.getTotalPoints())
                    .setGroupRank(groupRank);
            updateGroupList.add(tournamentGroupEntity);
            TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity = tournamentPointsGroupResultEntityMap.get(tournamentGroupEntity.getEntry());
            if (tournamentPointsGroupResultEntity == null) {
                return;
            }
            tournamentPointsGroupResultEntity.setEventGroupRank(groupRank);
            updateGroupPointsResultList.add(tournamentPointsGroupResultEntity);
        });

        // update
        this.tournamentGroupService.updateBatchById(updateGroupList);
        log.info("tournament:{}, event:{}, update tournament group!", tournamentId, event);
        this.tournamentPointsGroupResultService.updateBatchById(updateGroupPointsResultList);
        log.info("tournament:{}, event:{}, update tournament points group result!", tournamentId, event);
    }

    @Override
    public void updateZjPhaseTwoResult(int event, int tournamentId) {
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("tournament:{}, tournament_info not exists!", tournamentId);
            return;
        }
        int groupNum = tournamentInfoEntity.getGroupNum();
        int teamPerGroup = tournamentInfoEntity.getTeamPerGroup();
        // get group_id
        List<Integer> groupIdList = Lists.newArrayList();
        IntStream.rangeClosed(groupNum + 1, groupNum + teamPerGroup).forEach(groupIdList::add);
        // get entry_list by tournament
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // entry_event_result list
        Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("tournament:{}, event:{}, event_result not updated!", tournamentId, event);
            return;
        }
        // tournament_group
        List<TournamentGroupEntity> tournamentGroupEntityList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .in(TournamentGroupEntity::getGroupId, groupIdList));
        if (CollectionUtils.isEmpty(tournamentGroupEntityList)) {
            log.error("tournament:{}, tournament_group not exists!", tournamentId);
            return;
        }
        Map<Integer, TournamentPointsGroupResultEntity> tournamentPointsGroupResultEntityMap = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                .eq(TournamentPointsGroupResultEntity::getEvent, event)
                .in(TournamentPointsGroupResultEntity::getGroupId, groupIdList))
                .stream()
                .collect(Collectors.toMap(TournamentPointsGroupResultEntity::getEntry, o -> o));
        // phase two params
        int phaseTwoStartGw = tournamentGroupEntityList.get(0).getStartGw();
        if (event < phaseTwoStartGw) {
            log.error("tournament:{}, event:{}, phase two not start!", tournamentId, event);
            return;
        }
        int phaseTwoEndGw = tournamentGroupEntityList.get(0).getEndGw();
        if (event > phaseTwoEndGw) {
            log.error("tournament:{}, event:{}, phase two passed!", tournamentId, event);
            return;
        }
        // update phase one result
        List<TournamentGroupEntity> updateGroupList = Lists.newArrayList();
        List<TournamentPointsGroupResultEntity> updateGroupPointsResultList = Lists.newArrayList();
        // tournament_group
        tournamentGroupEntityList.forEach(tournamentGroupEntity -> {
            int entry = tournamentGroupEntity.getEntry();
            EntryEventResultEntity entryEventResultEntity = eventResultMap.getOrDefault(tournamentGroupEntity.getEntry(), null);
            if (entryEventResultEntity == null) {
                log.error("tournament:{}, event:{}, entry:{}, event_result not updated!", tournamentId, event, entry);
                return;
            }
            tournamentGroupEntity
                    .setPlay(event - phaseTwoStartGw + 1)
                    .setTotalPoints(this.entryEventResultService.sumEventPoints(event, phaseTwoStartGw, phaseTwoEndGw, entry))
                    .setTotalTransfersCost(this.entryEventResultService.sumEventTransferCost(event, phaseTwoStartGw, phaseTwoEndGw, entry))
                    .setTotalNetPoints(this.entryEventResultService.sumEventNetPoints(event, phaseTwoStartGw, phaseTwoEndGw, entry))
                    .setOverallRank(entryEventResultEntity.getOverallRank());
            // tournament_points_group_result
            TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity = tournamentPointsGroupResultEntityMap.get(entry);
            if (tournamentPointsGroupResultEntity == null) {
                return;
            }
            tournamentPointsGroupResultEntity
                    .setEventPoints(entryEventResultEntity.getEventPoints())
                    .setEventCost(entryEventResultEntity.getEventTransfersCost())
                    .setEventNetPoints(entryEventResultEntity.getEventPoints() - entryEventResultEntity.getEventTransfersCost())
                    .setEventRank(entryEventResultEntity.getEventRank());
            tournamentPointsGroupResultEntityMap.put(entry, tournamentPointsGroupResultEntity);
        });
        // sort group rank
        Map<String, Map<Integer, Integer>> tournamentGroupRankMap = this.qryZjTournamentPhaseTwoGroupRankMapByGroupList(tournamentGroupEntityList);
        tournamentGroupEntityList.forEach(tournamentGroupEntity -> {
            Map<Integer, Integer> groupRankMap = tournamentGroupRankMap.get(String.valueOf(tournamentGroupEntity.getGroupId()));
            int groupRank = groupRankMap.getOrDefault(tournamentGroupEntity.getEntry(), 0);
            int groupPoints = this.getZjTournamentGroupPoints(groupRank);
            tournamentGroupEntity
                    .setGroupPoints(groupPoints)
                    .setGroupRank(groupRank);
            updateGroupList.add(tournamentGroupEntity);
            TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity = tournamentPointsGroupResultEntityMap.get(tournamentGroupEntity.getEntry());
            if (tournamentPointsGroupResultEntity == null) {
                return;
            }
            tournamentPointsGroupResultEntity.setEventGroupRank(groupRank);
            updateGroupPointsResultList.add(tournamentPointsGroupResultEntity);
        });
        // update
        this.tournamentGroupService.updateBatchById(updateGroupList);
        log.info("tournament:{}, event:{}, update tournament group success!", tournamentId, event);
        this.tournamentPointsGroupResultService.updateBatchById(updateGroupPointsResultList);
        log.info("tournament:{}, event:{}, update tournament points group result success!", tournamentId, event);
    }

    private Map<String, Map<Integer, Integer>> qryZjTournamentPhaseTwoGroupRankMapByGroupList
            (List<TournamentGroupEntity> tournamentGroupEntityList) {
        Map<String, Map<Integer, Integer>> map = Maps.newHashMap(); // entry -> groupRank
        // sort by group
        Multimap<Integer, TournamentGroupEntity> groupEntityMap = HashMultimap.create();
        tournamentGroupEntityList.forEach(o -> groupEntityMap.put(o.getGroupId(), o));
        // sort every group
        groupEntityMap.keySet().forEach(groupId -> {
            Map<Integer, Integer> groupRankMap = Maps.newHashMap();
            List<TournamentGroupEntity> groupEntityList = groupEntityMap.get(groupId)
                    .stream()
                    .sorted(Comparator.comparing(TournamentGroupEntity::getTotalNetPoints).reversed()
                            .thenComparing(TournamentGroupEntity::getTotalTransfersCost))
                    .collect(Collectors.toList());
            int rank = 1;
            int levelCount = 0;
            for (int i = 0; i < groupEntityList.size(); i++) {
                TournamentGroupEntity tournamentGroupEntity = groupEntityList.get(i);
                int totalNetPoints = tournamentGroupEntity.getTotalNetPoints();
                int totalTransfersCost = tournamentGroupEntity.getTotalTransfersCost();
                if (i > 0) {
                    if (totalNetPoints != groupEntityList.get(i - 1).getTotalNetPoints()) {
                        rank = rank + 1 + levelCount;
                        levelCount = 0;
                    } else if (totalTransfersCost != groupEntityList.get(i - 1).getTotalTransfersCost()) {
                        rank = rank + 1 + levelCount;
                        levelCount = 0;
                    } else {
                        levelCount++;
                    }
                }
                groupRankMap.put(tournamentGroupEntity.getEntry(), rank);
            }
            map.put(String.valueOf(groupId), groupRankMap);
        });
        return map;
    }

    private int getZjTournamentGroupPoints(int groupRank) {
        switch (groupRank) {
            case 1:
                return 5;
            case 2:
                return 3;
            case 3:
                return 2;
            case 4:
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public void updateZjPkResult(int event, int tournamentId) {
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("tournament:{}, tournament_info not exists!", tournamentId);
            return;
        }
        // check gw
        int pkStartGw = tournamentInfoEntity.getKnockoutStartGw();
        if (event < pkStartGw) {
            log.error("tournament:{}, event:{}, pk not start!", tournamentId, event);
            return;
        }
        int pkEndGw = tournamentInfoEntity.getKnockoutEndGw();
        if (event > pkEndGw) {
            log.error("tournament:{}, event:{}, pk passed!", tournamentId, event);
            return;
        }
        // get entry_list by tournament
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // get event_result list
        Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("tournament:{}, event:{}, event_result not update!", tournamentId, event);
            return;
        }
        // tournament_knockout_result
        Map<Integer, Integer> matchWinnerMap = Maps.newHashMap();
        List<TournamentKnockoutResultEntity> tournamentKnockoutResultList = Lists.newArrayList();
        this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                .eq(TournamentKnockoutResultEntity::getEvent, event))
                .forEach(tournamentKnockoutResultEntity -> {
                    int homeEntry = tournamentKnockoutResultEntity.getHomeEntry();
                    int awayEntry = tournamentKnockoutResultEntity.getAwayEntry();
                    EntryEventResultEntity homeEventResult = eventResultMap.getOrDefault(homeEntry, new EntryEventResultEntity());
                    EntryEventResultEntity awayEventResult = eventResultMap.getOrDefault(awayEntry, new EntryEventResultEntity());
                    int matchWinner = this.getMatchWinner(homeEntry, awayEntry, homeEventResult, awayEventResult);
                    matchWinnerMap.put(tournamentKnockoutResultEntity.getMatchId(), matchWinner);
                    tournamentKnockoutResultEntity
                            .setHomeEntryNetPoints(homeEntry > 0 ? homeEventResult.getEventNetPoints() : 0)
                            .setHomeEntryRank(homeEntry > 0 ? homeEventResult.getEventRank() : 0)
                            .setAwayEntryNetPoints(awayEntry > 0 ? awayEventResult.getEventNetPoints() : 0)
                            .setAwayEntryRank(awayEntry > 0 ? awayEventResult.getEventRank() : 0)
                            .setMatchWinner(matchWinner);
                    tournamentKnockoutResultList.add(tournamentKnockoutResultEntity);
                });
        this.tournamentKnockoutResultService.updateBatchById(tournamentKnockoutResultList);
        log.info("tournament:{}, event:{}, update zj tournament pk knockout success!", tournamentId, event);
        // tournament_knockout
        List<TournamentKnockoutEntity> tournamentKnockoutList = Lists.newArrayList();
        matchWinnerMap.keySet().forEach(matchId -> {
            TournamentKnockoutEntity tournamentKnockoutEntity = this.tournamentKnockoutService.getOne(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                    .eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
                    .eq(TournamentKnockoutEntity::getMatchId, matchId));
            tournamentKnockoutEntity.setRoundWinner(matchWinnerMap.get(matchId));
            tournamentKnockoutList.add(tournamentKnockoutEntity);
        });
        this.tournamentKnockoutService.updateBatchById(tournamentKnockoutList);
        log.info("tournament:{}, event:{}, update zj tournament pk knockout result success!", tournamentId, event);
    }

    @Override
    public void updateZjTournamentResult(int tournamentId) {
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("tournament:{}, tournament_info not exists!", tournamentId);
            return;
        }
        // tournament_entry
        List<Integer> entryList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                .eq(TournamentEntryEntity::getTournamentId, tournamentId))
                .stream()
                .map(TournamentEntryEntity::getEntry)
                .collect(Collectors.toList());
        // phase one result
        int groupNum = tournamentInfoEntity.getGroupNum();
        List<Integer> phaseOneGroupList = Lists.newArrayList();
        IntStream.rangeClosed(1, groupNum).forEach(phaseOneGroupList::add);
        List<TournamentGroupEntity> tournamentGroupEntityList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .gt(TournamentGroupEntity::getEntry, 0)
                .in(TournamentGroupEntity::getGroupId, phaseOneGroupList));
        Map<Integer, Integer> phaseOneResultMap = tournamentGroupEntityList
                .stream()
                .collect(Collectors.toMap(TournamentGroupEntity::getEntry, TournamentGroupEntity::getTotalPoints));
        // phase two result
        int teamPerGroup = tournamentInfoEntity.getTeamPerGroup();
        List<Integer> phaseTwoGroupList = Lists.newArrayList();
        IntStream.rangeClosed(groupNum + 1, groupNum + teamPerGroup).forEach(phaseTwoGroupList::add);
        Map<Integer, Integer> phaseTwoResultMap = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .gt(TournamentGroupEntity::getEntry, 0)
                .in(TournamentGroupEntity::getGroupId, phaseTwoGroupList))
                .stream()
                .collect(Collectors.toMap(TournamentGroupEntity::getEntry, TournamentGroupEntity::getTotalNetPoints));
        // pk result
        Map<Integer, Integer> pkResultMap = Maps.newHashMap();
        this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                .gt(TournamentKnockoutResultEntity::getHomeEntry, 0)
                .gt(TournamentKnockoutResultEntity::getAwayEntry, 0)
                .gt(TournamentKnockoutResultEntity::getMatchWinner, 0))
                .forEach(o -> {
                    pkResultMap.put(o.getHomeEntry(), o.getHomeEntryNetPoints());
                    pkResultMap.put(o.getAwayEntry(), o.getAwayEntryNetPoints());
                });
        // entry_result
        Table<Integer, Integer, Integer> entryResultTable = HashBasedTable.create(); // entry -> phase -> points
        entryList.forEach(entry -> {
            entryResultTable.put(entry, 1, phaseOneResultMap.getOrDefault(entry, 0));
            entryResultTable.put(entry, 2, phaseTwoResultMap.getOrDefault(entry, 0));
            entryResultTable.put(entry, 3, pkResultMap.getOrDefault(entry, 0));
        });
        // group points
        Map<String, Integer> phaseTwoGroupPointsMap = this.queryService.qryZjTournamentPhaseTwoGroupPointsMap(tournamentId);
        Map<String, Integer> pkGroupPointsMap = this.queryService.qryZjTournamentPkGroupPointsMap(tournamentId);
        // group rank
        Map<String, Integer> phaseOneRankMap = this.queryService.qryZjTournamentPhaseOneRankMap(tournamentId);
        Map<String, Integer> phaseTwoRankMap = this.queryService.qryZjTournamentPhaseTwoRankMap(tournamentId);
        Map<String, Integer> pkRankMap = this.queryService.qryZjTournamentPkRankMap(tournamentId);
        // total group points
        Map<String, Integer> phaseOneTotalGroupPointMap = this.setPhaseOneTotalGroupPoints(phaseOneRankMap);
        Map<String, Integer> phaseTwoTotalGroupPointMap = this.setPhaseTwoTotalGroupPoints(phaseTwoRankMap);
        Map<String, Integer> pkTotalGroupPointMap = this.setPkTotalGroupPoints(pkRankMap);
        // tournament_result
        List<ZjTournamentResultEntity> zjTournamentResultEntityList = Lists.newArrayList();
        this.zjTournamentResultService.list(new QueryWrapper<ZjTournamentResultEntity>().lambda()
                .eq(ZjTournamentResultEntity::getTournamentId, tournamentId))
                .forEach(zjTournamentResultEntity -> {
                    int groupId = zjTournamentResultEntity.getGroupId();
                    Multimap<Integer, Integer> groupEntryMap = HashMultimap.create();
                    tournamentGroupEntityList.forEach(o -> groupEntryMap.put(o.getGroupId(), o.getEntry()));
                    // total_points
                    int phaseOneTotalPoints = this.sumGroupTotalPoints(groupEntryMap.get(groupId), entryResultTable.column(1));
                    int phaseTwoTotalPoints = this.sumGroupTotalPoints(groupEntryMap.get(groupId), entryResultTable.column(2));
                    int phasePkTotalPoints = this.sumGroupTotalPoints(groupEntryMap.get(groupId), entryResultTable.column(3));
                    zjTournamentResultEntity
                            .setPhaseOneTotalPoints(phaseOneTotalPoints)
                            .setPhaseOneGroupPoints(phaseOneTotalPoints)
                            .setPhaseOneTotalGroupPoints(phaseOneTotalGroupPointMap.getOrDefault(String.valueOf(groupId), 0))
                            .setPhaseTwoTotalPoints(phaseTwoTotalPoints)
                            .setPhaseTwoGroupPoints(phaseTwoGroupPointsMap.getOrDefault(String.valueOf(groupId), 0))
                            .setPhaseTwoTotalGroupPoints(phaseTwoTotalGroupPointMap.getOrDefault(String.valueOf(groupId), 0))
                            .setPkTotalPoints(phasePkTotalPoints)
                            .setPkGroupPoints(pkGroupPointsMap.getOrDefault(String.valueOf(groupId), 0))
                            .setPkTotalGroupPoints(pkTotalGroupPointMap.getOrDefault(String.valueOf(groupId), 0));
                    zjTournamentResultEntity
                            .setTournamentTotalPoints(zjTournamentResultEntity.getPhaseOneTotalPoints() + zjTournamentResultEntity.getPhaseTwoTotalPoints() + zjTournamentResultEntity.getPkTotalPoints())
                            .setTournamentTotalGroupPoints(zjTournamentResultEntity.getPhaseOneTotalGroupPoints() + zjTournamentResultEntity.getPhaseTwoTotalGroupPoints() + zjTournamentResultEntity.getPkTotalGroupPoints());
                    zjTournamentResultEntityList.add(zjTournamentResultEntity);
                });
        // group tournament rank
        Map<String, Integer> tournamentRankMap = this.qryZjTournamentRankMap(zjTournamentResultEntityList);
        zjTournamentResultEntityList.forEach(o -> o.setTournamentRank(tournamentRankMap.getOrDefault(String.valueOf(o.getGroupId()), 0)));
        // update
        this.zjTournamentResultService.updateBatchById(zjTournamentResultEntityList);
        log.info("tournament:{}, update zj tournament result!", tournamentId);
    }

    private Map<String, Integer> setPhaseOneTotalGroupPoints(Map<String, Integer> phaseOneRankMap) {
        Map<String, Integer> map = Maps.newHashMap();
        phaseOneRankMap.forEach((groupId, rank) -> map.put(groupId, this.setPhaseOneRankPoints(rank)));
        return map;
    }

    private Map<String, Integer> setPhaseTwoTotalGroupPoints(Map<String, Integer> phaseTwoRankMap) {
        Map<String, Integer> map = Maps.newHashMap();
        phaseTwoRankMap.forEach((groupId, rank) -> map.put(groupId, this.setPhaseTwoRankPoints(rank)));
        return map;
    }

    private Map<String, Integer> setPkTotalGroupPoints(Map<String, Integer> pkRankMap) {
        Map<String, Integer> map = Maps.newHashMap();
        pkRankMap.forEach((groupId, rank) -> map.put(groupId, this.setPkRankPoints(rank)));
        return map;
    }

    private int setPhaseOneRankPoints(int rank) {
        switch (rank) {
            case 1:
                return 5;
            case 2:
                return 3;
            case 3:
                return 2;
            case 4:
                return 1;
            default:
                return 0;
        }
    }

    private int setPhaseTwoRankPoints(int rank) {
        switch (rank) {
            case 1:
                return 5;
            case 2:
                return 3;
            case 3:
                return 2;
            case 4:
                return 1;
            default:
                return 0;
        }
    }

    private int setPkRankPoints(int rank) {
        switch (rank) {
            case 1:
                return 7;
            case 2:
                return 5;
            case 3:
                return 3;
            case 4:
                return 2;
            default:
                return 0;
        }
    }

    private int sumGroupTotalPoints(Collection<Integer> groupEntryList, Map<Integer, Integer> entryResultMap) {
        return groupEntryList
                .stream()
                .mapToInt(o -> entryResultMap.getOrDefault(o, 0))
                .sum();
    }

    private Map<String, Integer> qryZjTournamentRankMap(List<ZjTournamentResultEntity> list) {
        Map<String, Integer> map = Maps.newHashMap();
        List<ZjTournamentResultEntity> zjTournamentResultEntityList = list
                .stream()
                .sorted(Comparator.comparing(ZjTournamentResultEntity::getTournamentTotalGroupPoints)
                        .thenComparing(ZjTournamentResultEntity::getTournamentTotalPoints)
                        .reversed())
                .collect(Collectors.toList());
        int rank = 1;
        int levelCount = 0;
        for (int i = 0; i < zjTournamentResultEntityList.size(); i++) {
            ZjTournamentResultEntity zjTournamentResultEntity = zjTournamentResultEntityList.get(i);
            int totalGroupPoints = zjTournamentResultEntity.getTournamentTotalGroupPoints();
            int totalPoints = zjTournamentResultEntity.getTournamentTotalPoints();
            if (i > 0) {
                if (totalGroupPoints != zjTournamentResultEntityList.get(i - 1).getTournamentTotalGroupPoints()) {
                    rank = rank + 1 + levelCount;
                    levelCount = 0;
                } else if (totalPoints != zjTournamentResultEntityList.get(i - 1).getTournamentTotalPoints()) {
                    rank = rank + 1 + levelCount;
                    levelCount = 0;
                } else {
                    levelCount++;
                }
            }
            map.put(String.valueOf(zjTournamentResultEntity.getGroupId()), rank);
        }
        return map;
    }

    /**
     * @implNote refresh
     */
    @Override
    public void refreshEventLiveCache(int event) {
        if (event <= 0 || event > 38) {
            return;
        }
        this.redisCacheService.insertSingleEventFixtureCache(event);
        this.redisCacheService.insertLiveFixtureCache();
        this.redisCacheService.insertLiveBonusCache();
        this.redisCacheService.insertEventLiveCache(event);
    }

    @Override
    public void refreshPlayerValue() {
        this.redisCacheService.insertPlayer();
        this.redisCacheService.insertPlayerValue();
        RedisUtils.removeCacheByKey("api::qryPlayerValue");
    }

    @Override
    public void refreshEntryInfo(int entry) {
        this.updateEntryInfoByEntry(entry);
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "qryEntryInfo", CommonUtils.getCurrentSeason(), entry));
    }

    @Override
    public void refreshEntryEventResult(int event, int entry) {
        this.upsertEntryEventResult(event, entry);
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntryEventResult", event, entry));
    }

    @Override
    public void refreshEntryEventTransfers(int event, int entry) {
        this.updateEntryEventTransfers(event, entry);
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntryEventTransfers", event, entry));
    }

    @Override
    public void refreshCurrentEventScoutResult(int entry) {
        int nextEvent = this.queryService.getNextEvent();
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEventScoutPickResult", nextEvent, entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEventScoutResult", nextEvent));
    }

    @Override
    public void refreshTournamentEventResult(int event, int tournamentId) {
        // tournament_result
        log.info("event:{}, tornament:{}, start upsert tournament entry event result", event, tournamentId);
        this.upsertTournamentEntryEventResult(event, tournamentId);
        // tournament_points_race_group_result
        log.info("event:{}, tornament:{}, start upsert tournament points race group result", event, tournamentId);
        this.updatePointsRaceGroupResult(event, tournamentId);
        // cache
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryTournamentEventResult", event, tournamentId));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryTournamentEventChampion", tournamentId));
    }

    @Override
    public void refreshEntrySummary(int event, int entry) {
        // entry_event_result
        this.upsertEntryEventResult(event, entry);
        // entry_event_transfers
        this.updateEntryEventTransfers(event, entry);
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntrySeasonInfo", entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntrySeasonSummary", entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntrySeasonCaptain", entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntrySeasonTransfers", entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEntrySeasonScore", entry));
    }

    @Override
    public void refreshLeagueSummary(int event, String leagueName, int entry) {
        LeagueEventReportEntity leagueEventReportEntity = this.queryService.qryLeagueInfoByName(leagueName);
        if (leagueEventReportEntity == null) {
            return;
        }
        this.reportService.updateLeagueEventResult(event, leagueEventReportEntity.getLeagueId(), leagueEventReportEntity.getLeagueType());
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryLeagueSeasonInfo", leagueName));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryLeagueSeasonSummary", leagueName, entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryLeagueSeasonCaptain", leagueName, entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryLeagueSeasonScore", leagueName, entry));
    }

    @Override
    public void refreshLeagueSelect(int event, String leagueName) {
        LeagueEventReportEntity leagueEventReportEntity = this.queryService.qryLeagueInfoByName(leagueName);
        if (leagueEventReportEntity == null) {
            return;
        }
        int leagueId = leagueEventReportEntity.getLeagueId();
        String leagueType = leagueEventReportEntity.getLeagueType();
        this.reportService.updateLeagueEventResult(event, leagueId, leagueType);
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryLeagueSelectByName", CommonUtils.getCurrentSeason(), event, leagueName));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryLeagueEventEoWebNameMap", CommonUtils.getCurrentSeason(), event, leagueId, leagueType));
    }

    @Override
    public void refreshPlayerSummary(String season, int code) {
        if (StringUtils.equals(season, CommonUtils.getCurrentSeason())) {
            int event = this.queryService.getCurrentEvent();
            this.redisCacheService.insertPlayer();
            this.redisCacheService.insertEventLive(event);
            this.redisCacheService.insertEventFixture();
            this.redisCacheService.insertEventLiveSummary();
        }
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryPlayerInfo", season, code));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryPlayerSummary", season, code));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "qryPlayerDetailData", season));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "qryPlayerFixtureList", season));
    }

    @Override
    public void refreshTeamSummary(String season, String name) {
        if (StringUtils.equals(season, CommonUtils.getCurrentSeason())) {
            this.redisCacheService.insertPlayer();
            this.redisCacheService.insertPlayerStat();
        }
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryTeamSummary", season, name));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "qryPlayerFixtureList", season));
    }

}

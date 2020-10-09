package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.UserHistoryRes;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userHistory.Current;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutNextRoundData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IUpdateEventResultService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Calculate gw points
 * Create by tong on 2020/3/10
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateEventResultServiceImpl implements IUpdateEventResultService {

    private final EventLiveService eventLiveService;
    private final EntryEventResultService entryEventResultService;
    private final EntryInfoService entryInfoService;
    private final TournamentInfoService tournamentInfoService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
    private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
    private final TournamentKnockoutService tournamentKnockoutService;
    private final TournamentKnockoutResultService tournamentKnockoutResultService;
    private final IQuerySerivce querySerivce;

    @Override
    public void updateEntryInfo() {
        List<EntryInfoEntity> updateEntryInfoList = Lists.newArrayList();
        // get entry info list
        List<EntryInfoEntity> entryInfoList = this.entryInfoService.list();
        entryInfoList.parallelStream().forEach(entryInfoEntity -> {
            int entry = entryInfoEntity.getEntry();
            // entry
            EntryRes entryRes = this.querySerivce.getEntry(entry);
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
                    .setTotalTransfers(entryRes.getLastDeadlineTotalTransfers());
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
    }

    private Current getUserLastCurrent(int entry) {
        UserHistoryRes userHistoryRes = this.querySerivce.getUserHistory(entry);
        if (userHistoryRes == null) {
            return null;
        }
        int lastEvent = this.querySerivce.getLastEvent();
        return userHistoryRes.getCurrent()
                .stream()
                .filter(o -> o.getEvent() == lastEvent)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void updateEntryEventResult(int event, int entry) {
        if (entry <= 0) {
            return;
        }
        UserPicksRes userPick = this.querySerivce.getUserPicks(event, entry);
        if (userPick == null) {
            return;
        }
        // get event_live
        Map<Integer, Integer> elementPointsMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        // entry_event_result
        EntryEventResultEntity entryEventResult = this.calcEntryEventPoints(event, entry, userPick, elementPointsMap);
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
    public void updateTournamentEntryEventResult(int event, int tournamentId) {
        // get entry_list
        List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        // get event_live
        Map<Integer, Integer> elementPointsMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        // upsert entry_event_result
        List<EntryEventResultEntity> insertEventResultList = Lists.newArrayList();
        List<EntryEventResultEntity> updateEventResultList = Lists.newArrayList();
        entryList.parallelStream().forEach(entry -> {
            if (entry <= 0) {
                return;
            }
            UserPicksRes userPick = this.querySerivce.getUserPicks(event, entry);
            if (userPick == null) {
                return;
            }
            EntryEventResultEntity entryEventResultEntity = this.calcEntryEventPoints(event, entry, userPick, elementPointsMap);
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
        this.entryEventResultService.updateBatchById(updateEventResultList);
    }

    private EntryEventResultEntity calcEntryEventPoints(int event, int entry, UserPicksRes userPick, Map<Integer, Integer> elementPointsMap) {
        return new EntryEventResultEntity()
                .setEntry(entry)
                .setEvent(event)
                .setEventPoints(userPick.getEntryHistory().getPoints())
                .setEventTransfers(userPick.getEntryHistory().getEventTransfers())
                .setEventTransfersCost(userPick.getEntryHistory().getEventTransfersCost())
                .setEventNetPoints(userPick.getEntryHistory().getPoints() - userPick.getEntryHistory().getEventTransfersCost())
                .setEventBenchPoints(userPick.getEntryHistory().getPointsOnBench())
                .setEventRank(userPick.getEntryHistory().getRank())
                .setOverallRank(userPick.getEntryHistory().getOverallRank())
                .setEventChip(StringUtils.isBlank(userPick.getActiveChip()) ? Chip.NONE.getValue() : userPick.getActiveChip())
                .setEventPicks(this.setUserPicks(userPick.getPicks(), elementPointsMap));
    }

    @Override
    public void updatePointsRaceGroupResult(int event, int tournamentId) {
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("tournament_info not exists, tournament:{}!", tournamentId);
            return;
        }
        // check gw
        int groupStartGw = tournamentInfoEntity.getGroupStartGw();
        int groupEndGw = tournamentInfoEntity.getGroupEndGw();
        if (event > groupEndGw) {
            log.error("group stage passed,current event:{}, tournament:{}!", event, tournamentId);
            return;
        }
        // entry list
        List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
        // entry_event_result
        Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("event_result not updated, event:{}, tournament:{}!", event, tournamentId);
            return;
        }
        // tournament_group
        List<TournamentGroupEntity> tournamentGroupEntityList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .in(TournamentGroupEntity::getEntry, entryList));
        if (CollectionUtils.isEmpty(tournamentGroupEntityList)) {
            log.error("tournament_group not exists, tournament:{}!", tournamentId);
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
                log.error("event_result not updated, event:{}, tournament:{}, entry:{}!", event, tournamentId, entry);
                return;
            }
            tournamentGroupEntity
                    .setPlay(event - tournamentGroupEntity.getStartGw() + 1)
                    .setTotalPoints(this.entryEventResultService.sumEventNetPoints(event, groupStartGw, groupEndGw, entry))
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
        log.info("event:{}, tournament:{}, update tournament group success!", event, tournamentId);
        this.tournamentPointsGroupResultService.updateBatchById(updateGroupPointsResultList);
        log.info("event:{}, tournament:{}, update tournament points group result success!", event, tournamentId);
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
        TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
                .eq(TournamentInfoEntity::getId, tournamentId)
                .eq(TournamentInfoEntity::getState, 1));
        if (tournamentInfoEntity == null) {
            log.error("tournament_info not exists, tournament:{}!", tournamentId);
            return;
        }
        // check gw
        int groupStartGw = tournamentInfoEntity.getGroupStartGw();
        int groupEndGw = tournamentInfoEntity.getGroupEndGw();
        if (event > groupEndGw) {
            log.error("group stage passed,current event:{}, tournament:{}!", event, tournamentId);
            return;
        }
        // tournamet_entry
        List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
        // entry_event_result
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(entryEventResultMap)) {
            log.error("event_result not update, event:{}, tournament:{}!", event, tournamentId);
            return;
        }
        // tournament_group_battle_result
        Table<Integer, Integer, Integer> battleResultTable = this.updateGroupBattleResult(event, tournamentId, entryEventResultMap);
        // tournament_group
        this.updateTournamentGroup(event, tournamentId, tournamentInfoEntity.getGroupQualifiers(), groupStartGw, groupEndGw, battleResultTable, entryEventResultMap);
    }

    private Table<Integer, Integer, Integer> updateGroupBattleResult(int event, int tournamentId, Map<Integer, EntryEventResultEntity> entryEventResultMap) {
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

    private int getGroupBattleHomeEntryResult(EntryEventResultEntity firstEventResult, EntryEventResultEntity secondEventResult) {
        if (firstEventResult.getEventNetPoints() > secondEventResult.getEventNetPoints()) {
            return 3;
        } else if (firstEventResult.getEventNetPoints() < secondEventResult.getEventNetPoints()) {
            return 0;
        } else {
            return 1;
        }
    }

    private void updateTournamentGroup(int event, int tournamentId, int qualifiers, int startGw, int endGw, Table<Integer, Integer, Integer> battleResultTable, Map<Integer, EntryEventResultEntity> entryEventResultMap) {
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
                            .setTotalPoints(this.entryEventResultService.sumEventNetPoints(event, startGw, endGw, entry))
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

    private Map<Integer, Map<String, Integer>> sortPointsRaceTournamentGroupRank(List<TournamentGroupEntity> tournamentGroupEntityList) {
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
            Map<String, Integer> groupRankMap = this.sortPointsRaceGroupRank(groupEntityMap.get(groupId));
            map.put(groupId, groupRankMap);
        });
        return map;
    }

    private Map<String, Integer> sortPointsRaceGroupRank(List<TournamentGroupEntity> tournamentGroupEntityList) {
        Map<String, Integer> groupRankMap = Maps.newHashMap(); // entry-> groupRank
        Map<String, Integer> groupRankCountMap = Maps.newLinkedHashMap();
        tournamentGroupEntityList
                .stream()
                .filter(o -> o.getTotalPoints() != 0)
                .sorted(Comparator.comparing(TournamentGroupEntity::getTotalPoints).reversed()
                        .thenComparing(TournamentGroupEntity::getOverallRank))
                .forEachOrdered(o -> this.setGroupRankMapValue(o.getTotalPoints() + "-" + o.getOverallRank(), groupRankCountMap));
        tournamentGroupEntityList
                .stream()
                .filter(o -> o.getTotalPoints() == 0)
                .sorted(Comparator.comparingInt(TournamentGroupEntity::getEntry))
                .forEachOrdered(o -> this.setGroupRankMapValue(o.getTotalPoints() + "-" + o.getOverallRank(), groupRankCountMap));
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
        // get entry_list by tournament
        List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
        // get event_result list
        Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("event_result not update, event:{}, tournament:{}!", event, tournamentId);
            return;
        }
        // tournament_knockout_result
        Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap = this.updateKnockoutResult(tournamentId, event, eventResultMap);
        if (CollectionUtils.isEmpty(knockoutResultDataMap.values())) {
            return;
        }
        // tournament_knockout
        Map<Integer, TournamentKnockoutNextRoundData> nextKouckoutMap = this.updateKnockoutInfo(tournamentId, event, knockoutResultDataMap);
        if (CollectionUtils.isEmpty(nextKouckoutMap)) {
            return;
        }
        // next round entry
        this.updateNextKnockout(tournamentId, nextKouckoutMap);
    }

    private Multimap<Integer, TournamentKnockoutResultData> updateKnockoutResult(int tournamentId, int event, Map<Integer, EntryEventResultEntity> eventResultMap) {
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
                            .setPlayAgainstId(knockoutResult.getPlayAginstId())
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
        return knockoutResultDataMap;
    }

    private Map<Integer, TournamentKnockoutNextRoundData> updateKnockoutInfo(int tournamentId, int event, Multimap<Integer, TournamentKnockoutResultData> knockoutResultDataMap) {
        List<TournamentKnockoutEntity> tournametKnockoutList = Lists.newArrayList();
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
                    tournametKnockoutList.add(knockoutEntity);
                    // set next round data
                    this.setNextRoundData(nextKnockoutMap, knockoutEntity);
                })
        );
        this.tournamentKnockoutService.updateBatchById(tournametKnockoutList);
        return nextKnockoutMap;
    }

    private void updateNextKnockout(int tournamentId, Map<Integer, TournamentKnockoutNextRoundData> nextKouckoutMap) {
        // get round
        int nextRound = nextKouckoutMap.values()
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
                        .setHomeEntry(nextKouckoutMap.get(knockoutEntity.getMatchId()).getNextRoundHomeEntry())
                        .setAwayEntry(nextKouckoutMap.get(knockoutEntity.getMatchId()).getNextRoundAwayEntry())));
        // tournament_knockout_result
        List<TournamentKnockoutResultEntity> tournamentKnockoutResultList = Lists.newArrayList();
        this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                .in(TournamentKnockoutResultEntity::getMatchId, nextKouckoutMap.keySet()))
                .forEach(knockoutResultEntity -> tournamentKnockoutResultList.add(knockoutResultEntity
                        .setHomeEntry(nextKouckoutMap.get(knockoutResultEntity.getMatchId()).getNextRoundHomeEntry())
                        .setAwayEntry(nextKouckoutMap.get(knockoutResultEntity.getMatchId()).getNextRoundAwayEntry())));
        this.tournamentKnockoutService.updateBatchById(tournamentKnockoutEntityList);
        this.tournamentKnockoutResultService.updateBatchById(tournamentKnockoutResultList);
        log.info("1");
    }

    private String setUserPicks(List<Pick> picks, Map<Integer, Integer> elementPointsMap) {
        List<EntryPickData> pickList = Lists.newArrayList();
        picks.forEach(o -> pickList.add(new EntryPickData()
                .setElement(o.getElement())
                .setPosition(o.getPosition())
                .setMultiplier(o.getMultiplier())
                .setCaptain(o.isCaptain())
                .setViceCaptain(o.isViceCaptain())
                .setPoints(elementPointsMap.getOrDefault(o.getElement(), 0))
        ));
        return JsonUtils.obj2json(pickList);
    }

    private int getMatchWinner(int homeEntry, int awayEntry, EntryEventResultEntity homeEntryResult, EntryEventResultEntity awayEntryResult) {
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

    private int getRoundWinner(Collection<TournamentKnockoutResultData> collention) {
        if (collention.size() == 1) {
            return collention.stream().map(TournamentKnockoutResultData::getMatchWinner).findFirst().orElse(0);
        }
        List<TournamentKnockoutResultData> winners = new ArrayList<>(collention);
        int firstWinner = winners.get(0).getMatchWinner();
        Map<Integer, Integer> secondWinnerMap = collention.stream()
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

    private void setNextRoundData(Map<Integer, TournamentKnockoutNextRoundData> nextKnockoutMap, TournamentKnockoutEntity knockoutEntity) {
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
        TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("tournament_info not exists, tournament:{}!", tournamentId);
            return;
        }
        int groupNum = tournamentInfoEntity.getGroupNum();
        // get group_id
        List<Integer> groupIdList = Lists.newArrayList();
        IntStream.range(1, groupNum + 1).forEach(groupIdList::add);
        // get entry_list by tournament
        List<Integer> entryList = this.querySerivce.qryEntryListByTournament(tournamentId);
        // entry_event_result list
        Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("event_result not updated, event:{}, tournament:{}!", event, tournamentId);
            return;
        }
        // tournament_group
        List<TournamentGroupEntity> tournamentGroupEntityList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .in(TournamentGroupEntity::getGroupId, groupIdList));
        if (CollectionUtils.isEmpty(tournamentGroupEntityList)) {
            log.error("tournament_group not exists, tournament:{}!", tournamentId);
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
        int phaseOneEndGw = tournamentGroupEntityList.get(0).getEndGw();
        if (event > phaseOneEndGw) {
            log.error("phaseOneEndGw passed,current event:{}, tournament:{}!", event, tournamentId);
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
                log.error("event_result not updated, event:{}, tournament:{}, entry:{}!", event, tournamentId, entry);
                return;
            }
            tournamentGroupEntity
                    .setPlay(event - phaseOneStartGw + 1)
                    .setTotalPoints(this.entryEventResultService.sumEventPoints(event, phaseOneStartGw, phaseOneEndGw, entry))
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
        Map<Integer, Map<String, Integer>> tournamentGroupRankMap = this.sortPointsRaceTournamentGroupRank(tournamentGroupEntityList); // key:groupId -> value(key:overall_rank -> value:group_rank）
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
        log.info("event:{}, tournament:{}, update tournament group success!", event, tournamentId);
        this.tournamentPointsGroupResultService.updateBatchById(updateGroupPointsResultList);
        log.info("event:{}, tournament:{}, update tournament points group result success!", event, tournamentId);
    }

    @Override
    public void updateZjPhaseTwoResult(int event, int tournamentId) {

    }

    @Override
    public void updateZjPkResult(int event, int tournamentId) {

    }

}

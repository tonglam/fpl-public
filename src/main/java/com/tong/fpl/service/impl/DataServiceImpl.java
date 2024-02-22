package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.entry.Match;
import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.domain.data.userpick.AutoSubs;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventAutoSubsData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.global.MapData;
import com.tong.fpl.domain.letletme.league.LeagueEventInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import com.tong.fpl.service.IDataService;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * Create by tong on 2021/9/30
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataServiceImpl implements IDataService {

    private final IInterfaceService interfaceService;
    private final IRedisCacheService redisCacheService;
    private final IQueryService queryService;

    private final PlayerStatService playerStatService;
    private final EntryInfoService entryInfoService;
    private final EntryLeagueInfoService entryLeagueInfoService;
    private final EntryHistoryInfoService entryHistoryInfoService;
    private final EntryEventPickService entryEventPickService;
    private final EntryEventTransfersService entryEventTransfersService;
    private final EntryEventCupResultService entryEventCupResultService;
    private final EventLiveService eventLiveService;
    private final EntryEventResultService entryEventResultService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
    private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
    private final TournamentKnockoutService tournamentKnockoutService;
    private final TournamentKnockoutResultService tournamentKnockoutResultService;
    private final LeagueEventReportService leagueEventReportService;
    private final PopularScoutResultService popularScoutResultService;

    private final ForkJoinPool forkJoinPool = new ForkJoinPool(4);

    /**
     * @implNote daily
     */
    @Override
    public void updatePlayerValue() {
        int event = this.queryService.getCurrentEvent();
        if (event < 0 || event > 38) {
            return;
        }
        this.interfaceService.getBootstrapStatic().ifPresent(staticRes -> {
            this.redisCacheService.insertPlayer(staticRes);
            log.info("event:{}, insert player success", event);
            this.redisCacheService.insertPlayerValue(staticRes);
            log.info("event:{}, insert player_value success", event);
        });
    }

    @Override
    public void updatePlayerStat() {
        int event = this.queryService.getCurrentEvent();
        if (event < 0 || event > 38) {
            return;
        }
        this.interfaceService.getBootstrapStatic().ifPresent(this.redisCacheService::insertPlayerStat);
        log.info("event:{}, insert player_stat success", event);
    }

    /**
     * @implNote matchDay
     */
    @Override
    public void updateEventLiveCache(int event) {
        if (event < 1 || event > 38) {
            return;
        }
        this.interfaceService.getEventFixture(event).ifPresent(res -> this.redisCacheService.insertSingleEventFixtureCache(event, res));
        log.info("event:{}, insert single event_fixture_cache success", event);
        this.redisCacheService.insertLiveFixtureCache();
        log.info("event:{}, insert live_event_fixture_cache success", event);
        this.redisCacheService.insertLiveBonusCache();
        log.info("event:{}, insert live_bonus_cache success", event);
        this.interfaceService.getEventLive(event).ifPresent(res -> this.redisCacheService.insertEventLiveCache(event, res));
        log.info("event:{}, insert event_live_cache success", event);
    }

    @Override
    public void updateEventLive(int event) {
        if (event < 0 || event > 38) {
            return;
        }
        this.interfaceService.getEventFixture(event).ifPresent(res -> this.redisCacheService.insertSingleEventFixture(event, res));
        log.info("event:{}, insert single event_fixture success", event);
        this.redisCacheService.insertLiveFixtureCache();
        log.info("event:{}, insert live_event_fixture_cache success", event);
        this.redisCacheService.insertLiveBonusCache();
        log.info("event:{}, insert live_bonus_cache success", event);
        this.interfaceService.getEventLive(event).ifPresent(res -> this.redisCacheService.insertEventLive(event, res));
        log.info("event:{}, insert event_live success", event);
    }

    @Override
    public void upsertEventOverallResult() {
        int event = this.queryService.getCurrentEvent();
        if (event < 1 || event > 38) {
            return;
        }
        this.interfaceService.getBootstrapStatic().ifPresent(this.redisCacheService::insertEventOverallResult);
        log.info("event:{}, insert event overall result success", event);
    }

    /**
     * @implNote entry
     */
    @Override
    public void upsertEntryInfo(int entry) {
        if (entry <= 0) {
            return;
        }
        EntryInfoEntity entryInfo = this.entryInfoService.getById(entry);
        // init data
        EntryRes entryRes = this.interfaceService.getEntry(entry).orElse(null);
        if (entryRes == null) {
            log.error("entry:{}, get fpl server entry empty", entry);
            return;
        }
        // entry_info
        EntryEventResultEntity lastEventEntryResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, this.queryService.getLastEvent()).eq(EntryEventResultEntity::getEntry, entry));
        EntryInfoEntity entryInfoEntity = this.initEntryInfo(entryRes, entryInfo, lastEventEntryResultEntity);
        if (entryInfo == null) {
            this.entryInfoService.save(entryInfoEntity);
            log.info("entry:{}, insert entry_info success", entry);
        } else {
            this.entryInfoService.updateById(entryInfoEntity);
            log.info("entry:{}, update entry_info success", entry);
        }
        // entry_league_info
        List<EntryLeagueInfoEntity> entryLeagueInfoEntityList = this.initEntryLeagueInfo(entry, entryRes);
        if (CollectionUtils.isEmpty(entryLeagueInfoEntityList)) {
            return;
        }
        Map<String, EntryLeagueInfoEntity> entryLeagueInfoMap = this.entryLeagueInfoService.list(new QueryWrapper<EntryLeagueInfoEntity>().lambda().eq(EntryLeagueInfoEntity::getEntry, entry)).stream().collect(Collectors.toMap(k -> StringUtils.joinWith("-", k.getLeagueId(), k.getLeagueType()), v -> v));
        List<EntryLeagueInfoEntity> insertList = Lists.newArrayList();
        List<EntryLeagueInfoEntity> updateList = Lists.newArrayList();
        entryLeagueInfoEntityList.forEach(o -> {
            String key = StringUtils.joinWith("-", o.getLeagueId(), o.getLeagueType());
            if (!entryLeagueInfoMap.containsKey(key)) {
                insertList.add(o);
            } else {
                updateList.add(o);
            }
        });
        // save and update
        this.entryLeagueInfoService.saveBatch(insertList);
        log.info("entry:{}, insert entry_league_info size:{}", entry, insertList.size());
        this.entryLeagueInfoService.updateBatchById(updateList);
        log.info("entry:{}, update entry_league_info size:{}", entry, updateList.size());
    }

    private EntryInfoEntity initEntryInfo(EntryRes entryRes, EntryInfoEntity entryInfo, EntryEventResultEntity lastEntryEventResultEntity) {
        EntryInfoEntity entryInfoEntity = new EntryInfoEntity().setEntry(entryRes.getId()).setEntryName(entryRes.getName()).setPlayerName(entryRes.getPlayerFirstName() + " " + entryRes.getPlayerLastName()).setRegion(entryRes.getPlayerRegionName()).setStartedEvent(entryRes.getStartedEvent()).setOverallPoints(entryRes.getSummaryOverallPoints()).setOverallRank(entryRes.getSummaryOverallRank()).setBank(entryRes.getLastDeadlineBank()).setTeamValue(entryRes.getLastDeadlineValue()).setTotalTransfers(entryRes.getLastDeadlineTotalTransfers());
        if (lastEntryEventResultEntity == null) {
            entryInfoEntity.setLastOverallPoints(0).setLastOverallRank(0).setLastTeamValue(0);
        } else {
            entryInfoEntity.setLastOverallPoints(lastEntryEventResultEntity.getOverallPoints()).setLastOverallRank(lastEntryEventResultEntity.getOverallRank()).setLastTeamValue(lastEntryEventResultEntity.getTeamValue());
        }
        if (entryInfo == null) {
            List<String> usedNameList = Lists.newArrayList(entryRes.getName());
            entryInfoEntity.setLastEntryName("").setUsedEntryName(JsonUtils.obj2json(usedNameList));
        } else {
            if (!StringUtils.equals(entryRes.getName(), entryInfo.getEntryName())) {
                entryInfoEntity.setLastEntryName(entryInfo.getEntryName());
                List<String> usedNameList = JsonUtils.json2Collection(entryInfo.getUsedEntryName(), List.class, String.class);
                if (CollectionUtils.isEmpty(usedNameList)) {
                    usedNameList = Lists.newArrayList();
                }
                usedNameList.add(entryRes.getName());
                entryInfoEntity.setUsedEntryName(JsonUtils.obj2json(usedNameList));
            }
        }
        return entryInfoEntity;
    }

    private List<EntryLeagueInfoEntity> initEntryLeagueInfo(int entry, EntryRes entryRes) {
        if (entryRes == null || entryRes.getLeagues() == null) {
            log.error("entry:{}, get fpl server entry empty", entry);
            return null;
        }
        List<EntryLeagueInfoEntity> entryLeagueInfoEntityList = Lists.newArrayList();
        // classic
        entryRes.getLeagues().getClassic().forEach(o -> {
            int leagueId = o.getId();
            entryLeagueInfoEntityList.add(new EntryLeagueInfoEntity().setEntry(entry).setLeagueId(leagueId).setType(StringUtils.equals("x", o.getLeagueType()) ? "private" : "public").setLeagueType(LeagueType.Classic.name()).setLeagueName(o.getName()).setEntryRank(o.getEntryRank()).setEntryLastRank(o.getEntryLastRank()).setStartEvent(o.getStartEvent()).setCreated(o.getCreated()));
        });
        // h2h
        entryRes.getLeagues().getH2h().forEach(o -> {
            int leagueId = o.getId();
            entryLeagueInfoEntityList.add(new EntryLeagueInfoEntity().setEntry(entry).setLeagueId(leagueId).setType(StringUtils.equals("x", o.getLeagueType()) ? "private" : "public").setLeagueType(LeagueType.H2h.name()).setLeagueName(o.getName()).setEntryRank(o.getEntryRank()).setEntryLastRank(o.getEntryLastRank()).setStartEvent(o.getStartEvent()).setCreated(o.getCreated()));
        });
        return entryLeagueInfoEntityList;
    }

    @Override
    public void upsertEntryHistoryInfo(int entry) {
        if (entry <= 0) {
            return;
        }
        if (this.entryHistoryInfoService.count(new QueryWrapper<EntryHistoryInfoEntity>().lambda().eq(EntryHistoryInfoEntity::getEntry, entry)) > 0) {
            log.error("entry:{}, entry_history_info exists", entry);
            return;
        }
        // calc
        List<EntryHistoryInfoEntity> list = this.initEntryHistoryInfo(entry);
        if (CollectionUtils.isEmpty(list)) {
            log.error("entry:{}, init entry_history_info empty", entry);
            return;
        }
        // insert
        this.entryHistoryInfoService.saveBatch(list);
        log.info("entry:{}, insert entry_history_info size:{}", entry, list.size());
    }

    private List<EntryHistoryInfoEntity> initEntryHistoryInfo(int entry) {
        UserHistoryRes userHistoryRes = this.interfaceService.getUserHistory(entry).orElse(null);
        if (userHistoryRes == null || CollectionUtils.isEmpty(userHistoryRes.getPast())) {
            return null;
        }
        return userHistoryRes.getPast().stream().map(o -> new EntryHistoryInfoEntity().setEntry(userHistoryRes.getEntry()).setSeason(o.getSeasonName()).setTotalPoints(o.getTotalPoints()).setOverallRank(o.getRank())).collect(Collectors.toList());
    }

    @Override
    public void insertEntryEventPick(int event, int entry) {
        if (event < 1 || event > 38 || entry <= 0) {
            log.error("event:{}, entry:{}, params error", event, entry);
            return;
        }
        if (this.entryEventPickService.count(new QueryWrapper<EntryEventPickEntity>().lambda().eq(EntryEventPickEntity::getEntry, entry).eq(EntryEventPickEntity::getEvent, event)) > 0) {
            log.error("event:{}, entry:{}, entry_event_pick exists", event, entry);
            return;
        }
        EntryEventPickEntity entryEventPickEntity = this.initEventEntryPicks(event, entry);
        if (entryEventPickEntity == null) {
            log.error("event:{}, entry:{}, init entry_event_pick data empty", event, entry);
            return;
        }
        this.entryEventPickService.save(entryEventPickEntity);
        log.info("event:{}, entry:{}, insert entry_event_pick success", event, entry);
    }

    private EntryEventPickEntity initEventEntryPicks(int event, int entry) {
        UserPicksRes userPicksRes = this.interfaceService.getUserPicks(event, entry).orElse(null);
        if (userPicksRes == null || CollectionUtils.isEmpty(userPicksRes.getPicks())) {
            log.error("event:{}, entry:{}, get fpl server user_picks empty", event, entry);
            return null;
        }
        return new EntryEventPickEntity().setEntry(entry).setEvent(event).setTransfers(userPicksRes.getEntryHistory().getEventTransfers()).setTransfersCost(userPicksRes.getEntryHistory().getEventTransfersCost()).setChip(userPicksRes.getActiveChip() == null ? Chip.NONE.getValue() : userPicksRes.getActiveChip()).setPicks(JsonUtils.obj2json(userPicksRes.getPicks()));
    }

    @Override
    public void insertEntryEventTransfers(int entry) {
        int event = this.queryService.getCurrentEvent();
        if (event <= 1 || event > 38 || entry <= 0) {
            log.error("event:{}, entry:{}, params error", event, entry);
            return;
        }
        if (this.entryEventTransfersService.count(new QueryWrapper<EntryEventTransfersEntity>().lambda().eq(EntryEventTransfersEntity::getEntry, entry).eq(EntryEventTransfersEntity::getEvent, event)) > 0) {
            log.error("event:{}, entry:{}, entry_event_transfers exists", event, entry);
            return;
        }
        // init data
        List<EntryEventTransfersEntity> list = this.initEntryEventTransfers(entry);
        if (CollectionUtils.isEmpty(list)) {
            log.error("event:{}, entry:{}, init entry_event_transfers data empty", event, entry);
            return;
        }
        // prepare
        Map<String, EntryEventTransfersEntity> entryEventTransferMap = this.entryEventTransfersService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda().eq(EntryEventTransfersEntity::getEntry, entry)).stream().collect(Collectors.toMap(k -> StringUtils.joinWith("-", k.getEvent(), k.getEntry(), k.getElementIn(), k.getElementOut(), k.getTime()), o -> o));
        // save
        List<EntryEventTransfersEntity> insertList = list.stream().filter(o -> !entryEventTransferMap.containsKey(StringUtils.joinWith("-", o.getEvent(), o.getEntry(), o.getElementIn(), o.getElementOut(), o.getTime()))).collect(Collectors.toList());
        this.entryEventTransfersService.saveBatch(insertList);
        log.info("event:{}, entry:{}, insert entry_event_transfers size:{}", event, entry, insertList.size());
    }

    private List<EntryEventTransfersEntity> initEntryEventTransfers(int entry) {
        List<UserTransfersRes> transferResList = this.interfaceService.getUserTransfers(entry).orElse(null);
        if (CollectionUtils.isEmpty(transferResList)) {
            return null;
        }
        return transferResList.stream().map(o -> new EntryEventTransfersEntity().setEntry(o.getEntry()).setEvent(o.getEvent()).setElementIn(o.getElementIn()).setElementInPlayed(false).setElementInPoints(0).setElementInCost(o.getElementInCost()).setElementOut(o.getElementOut()).setElementOutCost(o.getElementOutCost()).setElementOutPoints(0).setTime(o.getTime())).collect(Collectors.toList());
    }

    @Override
    public void updateEntryEventTransfers(int event, int entry) {
        if (event <= 1 || event > 38 || entry <= 0) {
            log.error("event:{}, entry:{}, params error", event, entry);
            return;
        }
        // prepare
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).eq(EntryEventResultEntity::getEntry, entry));
        if (entryEventResultEntity == null || StringUtils.isEmpty(entryEventResultEntity.getEventPicks())) {
            log.error("event:{}, entry:{}, entry_event_result empty", event, entry);
            return;
        }
        List<Integer> pickElementList = this.getPickElementList(entryEventResultEntity.getEventPicks());
        if (CollectionUtils.isEmpty(pickElementList)) {
            log.error("event:{}, entry:{}, pick element empty", event, entry);
            return;
        }
        List<EntryEventTransfersEntity> entryEventTransferEntityList = this.entryEventTransfersService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda().eq(EntryEventTransfersEntity::getEvent, event).eq(EntryEventTransfersEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventTransferEntityList)) {
            log.error("event:{}, entry:{}, entry_event_transfers empty", event, entry);
            return;
        }
        Map<Integer, Integer> pointsMap = this.redisCacheService.getEventLiveByEvent(event).values().stream().collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        if (CollectionUtils.isEmpty(pointsMap)) {
            log.error("event:{}, entry:{}, event_live empty", event, entry);
            return;
        }
        List<EntryEventTransfersEntity> list = entryEventTransferEntityList.stream().peek(o -> o.setElementInPoints(pointsMap.getOrDefault(o.getElementIn(), 0)).setElementInPlayed(StringUtils.equals(Chip.BB.getValue(), entryEventResultEntity.getEventChip()) ? pickElementList.contains(o.getElementIn()) : pickElementList.subList(0, 11).contains(o.getElementIn())).setElementOutPoints(pointsMap.getOrDefault(o.getElementOut(), 0))).collect(Collectors.toList());
        this.entryEventTransfersService.updateBatchById(list);
        log.info("event:{}, entry:{}, update entry_event_transfers size:{}", event, entry, list.size());
    }

    private List<Integer> getPickElementList(String picks) {
        List<EntryPickData> pickList = JsonUtils.json2Collection(picks, List.class, EntryPickData.class);
        if (CollectionUtils.isEmpty(pickList)) {
            log.error("event pick empty");
            return null;
        }
        return pickList.stream().map(EntryPickData::getElement).collect(Collectors.toList());
    }

    @Override
    public void upsertEntryEventResult(int event, int entry) {
        if (event < 1 || event > 38 || entry <= 0) {
            log.error("event:{}, entry:{}, params error", event, entry);
            return;
        }
        // prepare
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).eq(EntryEventResultEntity::getEntry, entry));
        Map<String, EventLiveEntity> eventLiveMap = this.redisCacheService.getEventLiveByEvent(event);
        // init data
        EntryEventResultEntity entryEventResult = this.initEntryEventResult(event, entry, eventLiveMap);
        if (entryEventResult == null) {
            log.error("event:{}, entry:{}, params error", event, entry);
            return;
        }
        // save or update
        if (entryEventResultEntity == null) {
            this.entryEventResultService.save(entryEventResult);
            log.info("event:{}, entry:{}, insert entry_event_result success", event, entry);
        } else {
            entryEventResult.setId(entryEventResultEntity.getId());
            this.entryEventResultService.updateById(entryEventResult);
            log.info("event:{}, entry:{}, update entry_event_result success", event, entry);
        }
    }

    private EntryEventResultEntity initEntryEventResult(int event, int entry, Map<String, EventLiveEntity> eventLiveMap) {
        UserPicksRes userPick = this.interfaceService.getUserPicks(event, entry).orElse(null);
        if (userPick == null) {
            log.error("event:{}, entry:{}, get fpl server entry_event_result empty", event, entry);
            return null;
        }
        Map<Integer, Integer> elementPointsMap = eventLiveMap.values().stream().collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        int captain = this.getPlayedCaptain(userPick.getPicks(), eventLiveMap);
        EventLiveEntity captainEntity = eventLiveMap.getOrDefault(String.valueOf(captain), new EventLiveEntity());
        return new EntryEventResultEntity().setEntry(entry).setEvent(event).setEventPoints(userPick.getEntryHistory().getPoints()).setEventTransfers(userPick.getEntryHistory().getEventTransfers()).setEventTransfersCost(userPick.getEntryHistory().getEventTransfersCost()).setEventNetPoints(userPick.getEntryHistory().getPoints() - userPick.getEntryHistory().getEventTransfersCost()).setEventBenchPoints(userPick.getEntryHistory().getPointsOnBench()).setEventAutoSubPoints(userPick.getAutomaticSubs().size() == 0 ? 0 : this.calcAutoSubPoints(userPick.getAutomaticSubs(), elementPointsMap)).setEventRank(userPick.getEntryHistory().getRank()).setEventChip(StringUtils.isBlank(userPick.getActiveChip()) ? Chip.NONE.getValue() : userPick.getActiveChip()).setEventPicks(this.setUserPicks(userPick.getPicks(), elementPointsMap)).setEventAutoSubs(this.setAutoSubs(userPick.getAutomaticSubs(), elementPointsMap)).setOverallPoints(userPick.getEntryHistory().getTotalPoints()).setOverallRank(userPick.getEntryHistory().getOverallRank()).setTeamValue(userPick.getEntryHistory().getValue()).setBank(userPick.getEntryHistory().getBank()).setPlayedCaptain(captain).setCaptainPoints(captainEntity.getTotalPoints());
    }

    private int getPlayedCaptain(List<Pick> picks, Map<String, EventLiveEntity> eventLiveMap) {
        Pick captain = picks.stream().filter(Pick::isCaptain).findFirst().orElse(null);
        Pick viceCaptain = picks.stream().filter(Pick::isViceCaptain).findFirst().orElse(null);
        if (captain == null || viceCaptain == null) {
            return 0;
        }
        if (eventLiveMap.get(String.valueOf(captain.getElement())).getMinutes() == 0 && eventLiveMap.get(String.valueOf(viceCaptain.getElement())).getMinutes() > 0) {
            return viceCaptain.getElement();
        }
        return captain.getElement();
    }

    private int calcAutoSubPoints(List<AutoSubs> automaticSubs, Map<Integer, Integer> elementPointsMap) {
        return automaticSubs.stream().mapToInt(o -> elementPointsMap.getOrDefault(o.getElementIn(), 0)).sum();
    }

    private String setUserPicks(List<Pick> picks, Map<Integer, Integer> elementPointsMap) {
        List<EntryPickData> pickList = Lists.newArrayList();
        picks.forEach(o -> pickList.add(new EntryPickData().setElement(o.getElement()).setPosition(o.getPosition()).setMultiplier(o.getMultiplier()).setCaptain(o.isCaptain()).setViceCaptain(o.isViceCaptain()).setPoints(elementPointsMap.getOrDefault(o.getElement(), 0))));
        return JsonUtils.obj2json(pickList);
    }

    private String setAutoSubs(List<AutoSubs> autoSubs, Map<Integer, Integer> elementPointsMap) {
        if (CollectionUtils.isEmpty(autoSubs)) {
            return "";
        }
        List<EntryEventAutoSubsData> autoSubList = Lists.newArrayList();
        autoSubs.forEach(o -> autoSubList.add(new EntryEventAutoSubsData().setElementIn(o.getElementIn()).setElementInPoints(elementPointsMap.getOrDefault(o.getElementIn(), 0)).setElementOut(o.getElementOut()).setElementOutPoints(elementPointsMap.getOrDefault(o.getElementOut(), 0))));
        return JsonUtils.obj2json(autoSubList);
    }

    /**
     * @implNote entry_list
     */
    @Override
    public void upsertEntryInfoByList(List<Integer> entryList) {
        if (CollectionUtils.isEmpty(entryList)) {
            return;
        }
        entryList = entryList.stream().distinct().collect(Collectors.toList());
        // prepare
        Map<Integer, EntryInfoEntity> entryInfoMap = this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda().in(EntryInfoEntity::getEntry, entryList)).stream().collect(Collectors.toMap(EntryInfoEntity::getEntry, o -> o));
        Map<Integer, EntryEventResultEntity> lastEntryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, this.queryService.getLastEvent()).in(EntryEventResultEntity::getEntry, entryList)).stream().collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        // init data
        List<CompletableFuture<EntryRes>> entryResFuture = entryList.stream().map(o -> CompletableFuture.supplyAsync(() -> this.interfaceService.getEntry(o).orElse(null), this.forkJoinPool)).toList();
        Map<Integer, EntryRes> entryResMap = entryResFuture.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toMap(EntryRes::getId, o -> o));
        // entry_info
        List<CompletableFuture<EntryInfoEntity>> entryInfoFuture = entryList.stream().map(o -> CompletableFuture.supplyAsync(() -> this.initEntryInfo(entryResMap.get(o), entryInfoMap.get(o), lastEntryEventResultMap.get(o)), this.forkJoinPool)).toList();
        List<EntryInfoEntity> entryInfoList = entryInfoFuture.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();
        // save or update
        List<EntryInfoEntity> insertEntryInfoList = Lists.newArrayList();
        List<EntryInfoEntity> updateEntryInfoList = Lists.newArrayList();
        entryInfoList.forEach(o -> {
            if (!entryInfoMap.containsKey(o.getEntry())) {
                insertEntryInfoList.add(o);
            } else {
                updateEntryInfoList.add(o);
            }
        });
        this.entryInfoService.saveBatch(insertEntryInfoList);
        log.info("insert entry_info size:{}", insertEntryInfoList.size());
        this.entryInfoService.updateBatchById(updateEntryInfoList);
        log.info("update entry_info size:{}", updateEntryInfoList.size());
        if (!this.queryService.isAfterMatchDay(this.queryService.getCurrentEvent())) {
            return;
        }
        Map<String, EntryLeagueInfoEntity> entryLeagueInfoMap = this.entryLeagueInfoService.list(new QueryWrapper<EntryLeagueInfoEntity>().lambda().in(EntryLeagueInfoEntity::getEntry, entryList)).stream().collect(Collectors.toMap(k -> StringUtils.joinWith("-", k.getEntry(), k.getLeagueId(), k.getLeagueType()), v -> v));
        // entry_league_info
        List<CompletableFuture<List<EntryLeagueInfoEntity>>> entryLeagueInfoFuture = entryList.stream().map(o -> CompletableFuture.supplyAsync(() -> this.initEntryLeagueInfo(o, entryResMap.get(o)), this.forkJoinPool)).toList();
        List<EntryLeagueInfoEntity> entryInfoLeagueList = Lists.newArrayList();
        entryLeagueInfoFuture.stream().map(CompletableFuture::join).filter(Objects::nonNull).forEach(entryInfoLeagueList::addAll);
        // save or update
        List<EntryLeagueInfoEntity> insertLeagueInfoList = Lists.newArrayList();
        List<EntryLeagueInfoEntity> updateLeagueInfoList = Lists.newArrayList();
        entryInfoLeagueList.forEach(o -> {
            String key = StringUtils.joinWith("-", o.getEntry(), o.getLeagueId(), o.getLeagueType());
            if (!entryLeagueInfoMap.containsKey(key)) {
                insertLeagueInfoList.add(o);
            } else {
                o.setId(entryLeagueInfoMap.get(key).getId());
                updateLeagueInfoList.add(o);
            }
        });
        this.entryLeagueInfoService.saveBatch(insertLeagueInfoList);
        log.info("insert entry_league_info size:{}", insertLeagueInfoList.size());
        this.entryLeagueInfoService.updateBatchById(updateLeagueInfoList);
        log.info("update entry_league_info size:{}", updateLeagueInfoList.size());
    }

    @Override
    public void upsertEntryHistoryInfoByList(List<Integer> entryList) {
        if (CollectionUtils.isEmpty(entryList)) {
            return;
        }
        // exists
        List<Integer> existsList = this.entryHistoryInfoService.list(new QueryWrapper<EntryHistoryInfoEntity>().lambda().in(EntryHistoryInfoEntity::getEntry, entryList)).stream().map(EntryHistoryInfoEntity::getEntry).toList();
        entryList = entryList.stream().filter(o -> !existsList.contains(o)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            log.error("no need to insert");
            return;
        }
        // prepare
        List<CompletableFuture<List<EntryHistoryInfoEntity>>> future = entryList.stream().map(o -> CompletableFuture.supplyAsync(() -> this.initEntryHistoryInfo(o), this.forkJoinPool)).toList();
        List<EntryHistoryInfoEntity> entryHistoryInfoEntityList = Lists.newArrayList();
        future.stream().map(CompletableFuture::join).filter(Objects::nonNull).forEach(entryHistoryInfoEntityList::addAll);
        // save
        this.entryHistoryInfoService.saveBatch(entryHistoryInfoEntityList);
        log.info("insert entry_history_info size:{}", entryHistoryInfoEntityList.size());
    }

    @Override
    public void insertEventPickByEntryList(int event, List<Integer> entryList) {
        if (event < 1 || event > 38 || CollectionUtils.isEmpty(entryList)) {
            log.error("event:{}, params error", event);
            return;
        }
        // init
        List<Integer> existsList = this.entryEventPickService.list(new QueryWrapper<EntryEventPickEntity>().lambda().eq(EntryEventPickEntity::getEvent, event).in(EntryEventPickEntity::getEntry, entryList)).stream().map(EntryEventPickEntity::getEntry).toList();
        entryList = entryList.stream().filter(o -> !existsList.contains(o)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            log.error("event:{}, no need to insert", event);
            return;
        }
        // save
        List<CompletableFuture<EntryEventPickEntity>> future = entryList.stream().map(entry -> CompletableFuture.supplyAsync(() -> this.initEventEntryPicks(event, entry))).toList();
        List<EntryEventPickEntity> list = future.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());
        this.entryEventPickService.saveBatch(list);
        log.info("event:{}, insert entry_event_pick size:{}", event, list.size());
    }

    @Override
    public void upsertEventCupResultByEntryList(int event, List<Integer> entryList) {
        if (event < 17 || event > 38 || CollectionUtils.isEmpty(entryList)) {
            log.error("event:{}, params error", event);
            return;
        }
        // prepare
        Map<Integer, EntryEventCupResultEntity> entryCupResultMap = this.entryEventCupResultService.list(new QueryWrapper<EntryEventCupResultEntity>().lambda().eq(EntryEventCupResultEntity::getEvent, event).in(EntryEventCupResultEntity::getEntry, entryList)).stream().collect(Collectors.toMap(EntryEventCupResultEntity::getEntry, o -> o));
        // init data
        List<CompletableFuture<EntryEventCupResultEntity>> future = entryList.stream().map(entry -> CompletableFuture.supplyAsync(() -> this.initEntryEventCupResult(event, entry))).toList();
        List<EntryEventCupResultEntity> list = future.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();
        // save or update
        List<EntryEventCupResultEntity> insertList = Lists.newArrayList();
        List<EntryEventCupResultEntity> updateList = Lists.newArrayList();
        list.forEach(o -> {
            int entry = o.getEntry();
            if (!entryCupResultMap.containsKey(entry)) {
                insertList.add(o);
            }
        });
        this.entryEventCupResultService.saveBatch(insertList);
        log.info("event:{}, insert entry_event_cup_result size:{}", event, insertList.size());
        this.entryEventCupResultService.updateBatchById(updateList);
        log.info("event:{}, update entry_event_cup_result size:{}", event, updateList.size());
    }

    private EntryEventCupResultEntity initEntryEventCupResult(int event, int entry) {
        EntryCupRes entryCupRes = this.interfaceService.getEntryCup(entry).orElse(null);
        if (entryCupRes == null) {
            return null;
        }
        Match cupMatch = entryCupRes.getCupMatches().stream().filter(o -> o.getEvent() == event).findFirst().orElse(null);
        if (cupMatch == null) {
            log.error("entry:{}, get fpl server entry_cup empty", entry);
            return null;
        }
        EntryEventCupResultEntity entryEventCupResultEntity = new EntryEventCupResultEntity().setEvent(event).setEntry(entry).setEntryName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1Name() : cupMatch.getEntry2Name()).setPlayerName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1PlayerName() : cupMatch.getEntry2PlayerName()).setEventPoints(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry1Points() : cupMatch.getEntry2Points()).setAgainstEntry(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Entry() : cupMatch.getEntry1Entry()).setAgainstEntryName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Name() : cupMatch.getEntry1Name()).setAgainstPlayerName(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2PlayerName() : cupMatch.getEntry1PlayerName()).setAgainstEventPoints(entry == cupMatch.getEntry1Entry() ? cupMatch.getEntry2Points() : cupMatch.getEntry1Points());
        if (cupMatch.getWinner() == 0) {
            if (entryEventCupResultEntity.getEventPoints() >= entryEventCupResultEntity.getAgainstEventPoints()) {
                entryEventCupResultEntity.setResult("Win");
            }
        } else if (cupMatch.getWinner() == entryEventCupResultEntity.getEntry()) {
            entryEventCupResultEntity.setResult("Win");
        } else {
            entryEventCupResultEntity.setResult("Lose");
        }
        return entryEventCupResultEntity;
    }

    @Override
    public void upsertEventResultByEntryList(int event, List<Integer> entryList) {
        if (event < 1 || event > 38 || CollectionUtils.isEmpty(entryList)) {
            log.error("event:{}, params error", event);
            return;
        }
        // prepare
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).in(EntryEventResultEntity::getEntry, entryList)).stream().collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        Map<String, EventLiveEntity> eventLiveMap = this.redisCacheService.getEventLiveByEvent(event);
        // init data
        List<CompletableFuture<EntryEventResultEntity>> future = entryList.stream().filter(entry -> entry > 0).map(entry -> CompletableFuture.supplyAsync(() -> this.initEntryEventResult(event, entry, eventLiveMap))).toList();
        List<EntryEventResultEntity> list = future.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();
        if (CollectionUtils.isEmpty(list)) {
            log.error("event:{}, init entry_event_result data empty", event);
            return;
        }
        // save or update
        List<EntryEventResultEntity> insertList = Lists.newArrayList();
        List<EntryEventResultEntity> updateList = Lists.newArrayList();
        list.forEach(o -> {
            int entry = o.getEntry();
            if (!entryEventResultMap.containsKey(entry)) {
                insertList.add(o);
            } else {
                o.setId(entryEventResultMap.get(entry).getId());
                updateList.add(o);

            }
        });
        this.entryEventResultService.saveBatch(insertList);
        log.info("event:{}, insert entry_event_result size:{}", event, insertList.size());
        this.entryEventResultService.updateBatchById(updateList);
        log.info("event:{}, update entry_event_result size:{}", event, insertList.size());
    }

    /**
     * @implNote tournament
     */
    @Override
    public void upsertTournamentEventResult(int event, int tournamentId) {
        if (event < 1 || event > 38 || tournamentId <= 0) {
            log.error("event:{}, tournament:{}, params error", event, tournamentId);
            return;
        }
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("event:{}, tournament:{}, tournament_info not exists", event, tournamentId);
            return;
        }
        // entry list
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // entry_event_result
        this.upsertEventResultByEntryList(event, entryList);
        log.info("event:{}, tournament:{}, update tournament entry_event_result size:{}", event, tournamentId, entryList.size());
    }

    @Override
    public void updatePointsRaceGroupResult(int event, int tournamentId) {
        if (event < 1 || event > 38 || tournamentId <= 0) {
            log.error("event:{}, tournament:{}, params error", event, tournamentId);
            return;
        }
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("event:{}, tournament:{}, tournament_info not exists", event, tournamentId);
            return;
        }
        if (!StringUtils.equals(GroupMode.Points_race.name(), tournamentInfoEntity.getGroupMode())) {
            log.info("event:{}, tournament:{}, not points group", event, tournamentId);
            return;
        }
        // check gw
        int groupStartGw = tournamentInfoEntity.getGroupStartGw();
        int groupEndGw = tournamentInfoEntity.getGroupEndGw();
        if (event > groupEndGw) {
            log.error("event:{}, tournament:{}, group stage passed", event, tournamentId);
            return;
        }
        // entry list
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // entry_event_result
        Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("event:{}, tournament:{}, event_result not updated", event, tournamentId);
            return;
        }
        // tournament_group
        List<TournamentGroupEntity> tournamentGroupEntityList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId).in(TournamentGroupEntity::getEntry, entryList));
        if (CollectionUtils.isEmpty(tournamentGroupEntityList)) {
            return;
        }
        Map<Integer, TournamentPointsGroupResultEntity> tournamentPointsGroupResultEntityMap = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda().eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId).eq(TournamentPointsGroupResultEntity::getEvent, event)).stream().collect(Collectors.toMap(TournamentPointsGroupResultEntity::getEntry, o -> o));
        // update tournament_group and tournament_group_result
        List<TournamentGroupEntity> updateGroupList = Lists.newArrayList();
        List<TournamentPointsGroupResultEntity> updateGroupPointsResultList = Lists.newArrayList();
        // tournament_group
        tournamentGroupEntityList.forEach(tournamentGroupEntity -> {
            int entry = tournamentGroupEntity.getEntry();
            EntryEventResultEntity entryEventResultEntity = eventResultMap.getOrDefault(entry, null);
            if (entry > 0 && entryEventResultEntity == null) {
                this.upsertEntryEventResult(event, entry);
                entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEntry, entry).eq(EntryEventResultEntity::getEvent, event));
                if (entryEventResultEntity == null) {
                    log.error("event:{}, entry:{}, entry_event_result not exists", event, entry);
                }
                entryEventResultEntity = new EntryEventResultEntity().setEvent(event).setEventPoints(0).setEventTransfers(0).setEventTransfersCost(0).setEventNetPoints(0).setEventBenchPoints(0).setEventAutoSubPoints(0).setEventRank(0).setEventChip(Chip.NONE.getValue()).setPlayedCaptain(0).setCaptainPoints(0).setEventPicks("").setEventAutoSubs("").setOverallPoints(0).setOverallRank(0).setTeamValue(0).setBank(0);
            }
            tournamentGroupEntity.setPlay(event - tournamentGroupEntity.getStartGw() + 1).setTotalPoints(this.entryEventResultService.sumEventPoints(event, groupStartGw, groupEndGw, entry)).setTotalTransfersCost(this.entryEventResultService.sumEventTransferCost(event, groupStartGw, groupEndGw, entry)).setTotalNetPoints(this.entryEventResultService.sumEventNetPoints(event, groupStartGw, groupEndGw, entry)).setOverallRank(entryEventResultEntity.getOverallRank());
            // tournament_points_group_result
            TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity = tournamentPointsGroupResultEntityMap.get(entry);
            if (tournamentPointsGroupResultEntity == null) {
                log.error("event:{}, tournament:{}, entry:{}, event_result not updated", event, tournamentId, entry);
                return;
            }
            tournamentPointsGroupResultEntity.setEventPoints(entryEventResultEntity.getEventPoints()).setEventCost(entryEventResultEntity.getEventTransfersCost()).setEventNetPoints(entryEventResultEntity.getEventPoints() - entryEventResultEntity.getEventTransfersCost()).setEventRank(entryEventResultEntity.getEventRank());
        });
        // sort group rank
        Map<String, Integer> groupRankMap = this.sortPointsRaceGroupRank(tournamentGroupEntityList);  // key:overall_rank -> value:group_rank
        tournamentGroupEntityList.forEach(tournamentGroupEntity -> {
            int groupRank = groupRankMap.getOrDefault(tournamentGroupEntity.getTotalNetPoints() + "-" + tournamentGroupEntity.getOverallRank(), 0);
            tournamentGroupEntity.setGroupPoints(tournamentGroupEntity.getTotalNetPoints()).setGroupRank(groupRank);
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
        log.info("event:{}, tournament:{}, update tournament_group size:{}", event, tournamentId, updateGroupList.size());
        this.tournamentPointsGroupResultService.updateBatchById(updateGroupPointsResultList);
        log.info("event:{}, tournament:{}, update tournament_points_group_result size:{}", event, tournamentId, updateGroupPointsResultList.size());
    }

    private Map<Integer, EntryEventResultEntity> getEntryEventResultByEvent(int event, List<Integer> entryList) {
        return this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).in(EntryEventResultEntity::getEntry, entryList)).stream().collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
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

    private Map<String, Integer> sortPointsRaceEachGroupRank(List<TournamentGroupEntity> tournamentGroupEntityList) {
        Map<String, Integer> groupRankMap = Maps.newHashMap(); // entry -> groupRank
        Map<String, Integer> groupRankCountMap = Maps.newLinkedHashMap();
        tournamentGroupEntityList.stream().filter(o -> o.getTotalNetPoints() != 0).sorted(Comparator.comparing(TournamentGroupEntity::getTotalNetPoints).reversed().thenComparing(TournamentGroupEntity::getOverallRank)).forEachOrdered(o -> this.setGroupRankMapValue(o.getTotalNetPoints() + "-" + o.getOverallRank(), groupRankCountMap));
        tournamentGroupEntityList.stream().filter(o -> o.getTotalNetPoints() == 0).sorted(Comparator.comparingInt(TournamentGroupEntity::getEntry)).forEachOrdered(o -> this.setGroupRankMapValue("0-" + o.getOverallRank(), groupRankCountMap));
        int index = 1;
        for (String key : groupRankCountMap.keySet()) {
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
    public void updateBattleRaceGroupResult(int event, int tournamentId) {
        if (event < 1 || event > 38 || tournamentId <= 0) {
            log.error("event:{}, tournament:{}, params error", event, tournamentId);
            return;
        }
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("event:{}, tournament:{}, tournament_info not exists", event, tournamentId);
            return;
        }
        if (!StringUtils.equals(GroupMode.Battle_race.name(), tournamentInfoEntity.getGroupMode())) {
            log.info("event:{}, tournament:{}, not battle group", event, tournamentId);
            return;
        }
        // check gw
        int groupStartGw = tournamentInfoEntity.getGroupStartGw();
        int groupEndGw = tournamentInfoEntity.getGroupEndGw();
        if (event > groupEndGw) {
            log.error("event:{}, tournament:{}, group stage passed", event, tournamentId);
            return;
        }
        // tournament_entry
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // entry_event_result
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(entryEventResultMap)) {
            log.error("event:{}, tournament:{}, event_result not update", event, tournamentId);
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
        this.tournamentBattleGroupResultService.list(new QueryWrapper<TournamentBattleGroupResultEntity>().lambda().eq(TournamentBattleGroupResultEntity::getTournamentId, tournamentId).eq(TournamentBattleGroupResultEntity::getEvent, event)).forEach(groupBattleResult -> {
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
            tournamentBattleGroupResultList.add(groupBattleResult.setHomeEntryNetPoints(homeEventResult.getEventNetPoints()).setHomeEntryRank(homeEventResult.getEventRank()).setHomeEntryMatchPoints(homeEntryMatchPoints).setAwayEntryNetPoints(awayEventResult.getEventNetPoints()).setAwayEntryRank(awayEventResult.getEventRank()).setAwayEntryMatchPoints(awayEntryMatchPoints));
        });
        this.tournamentBattleGroupResultService.updateBatchById(tournamentBattleGroupResultList);
        log.info("event:{}, tournament:{}, update tournament_battle_group_result", event, tournamentId);
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
            List<TournamentGroupEntity> groupList = this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda().eq(TournamentGroupEntity::getTournamentId, tournamentId).eq(TournamentGroupEntity::getGroupId, groupId).orderByAsc(TournamentGroupEntity::getGroupIndex));
            groupList.forEach(tournamentGroupEntity -> {
                int entry = tournamentGroupEntity.getEntry();
                // total_points and overall_rank
                EntryEventResultEntity entryEventResult = entryEventResultMap.getOrDefault(entry, null);
                if (entryEventResult != null) {
                    tournamentGroupEntity.setTotalPoints(this.entryEventResultService.sumEventPoints(event, startGw, endGw, entry)).setTotalTransfersCost(this.entryEventResultService.sumEventTransferCost(event, startGw, endGw, entry)).setTotalNetPoints(this.entryEventResultService.sumEventNetPoints(event, startGw, endGw, entry)).setOverallRank(entryEventResult.getOverallRank());
                }
                if (tournamentGroupEntity.getPlay() == playedEvent) {
                    return;
                }
                // group points
                int matchPoints = matchResultMap.getOrDefault(entry, 0);
                tournamentGroupEntity.setGroupPoints(tournamentGroupEntity.getGroupPoints() + matchPoints).setPlay(tournamentGroupEntity.getPlay() + 1);
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
            groupList.forEach(tournamentGroupEntity -> tournamentGroupList.add(tournamentGroupEntity.setGroupRank(groupRankMap.getOrDefault(tournamentGroupEntity.getGroupPoints() + "-" + tournamentGroupEntity.getOverallRank(), 0)).setQualified(tournamentGroupEntity.getGroupRank() <= qualifiers)));
        });
        this.tournamentGroupService.updateBatchById(tournamentGroupList);
        log.info("event:{}, tournament:{}, update tournament_group size:{}", event, tournamentId, tournamentGroupList.size());
    }

    private Map<String, Integer> sortBattleGroupRank(List<TournamentGroupEntity> tournamentGroupEntityList) {
        Map<String, Integer> groupRankMap = Maps.newLinkedHashMap();
        Map<String, Integer> groupRankCountMap = Maps.newLinkedHashMap();
        tournamentGroupEntityList.stream().filter(o -> o.getTotalPoints() != 0).sorted(Comparator.comparing(TournamentGroupEntity::getGroupPoints).reversed().thenComparing(TournamentGroupEntity::getOverallRank)).forEachOrdered(o -> this.setGroupRankMapValue(o.getGroupPoints() + "-" + o.getOverallRank(), groupRankCountMap));
        tournamentGroupEntityList.stream().filter(o -> o.getTotalPoints() == 0).sorted(Comparator.comparingInt(TournamentGroupEntity::getEntry)).forEachOrdered(o -> this.setGroupRankMapValue(o.getGroupPoints() + "-" + o.getOverallRank(), groupRankCountMap));
        int index = 1;
        for (String key : groupRankCountMap.keySet()) {
            groupRankMap.put(key, index);
            index += groupRankCountMap.get(key);
        }
        return groupRankMap;
    }

    @Override
    public void updateKnockoutResult(int event, int tournamentId) {
        if (event < 1 || event > 38 || tournamentId <= 0) {
            log.error("event:{}, tournament:{}, params error", event, tournamentId);
            return;
        }
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            log.error("event:{}, tournament:{}, tournament_info not exists", event, tournamentId);
            return;
        }
        if (StringUtils.equals(KnockoutMode.No_knockout.name(), tournamentInfoEntity.getKnockoutMode())) {
            log.info("event:{}, tournament:{}, no knockout", event, tournamentId);
            return;
        }
        // check gw
        int knockoutEndGw = tournamentInfoEntity.getKnockoutEndGw();
        if (event > knockoutEndGw) {
            log.error("event:{}, tournament:{}, knockout stage passed", event, tournamentId);
            return;
        }
        // get entry_list by tournament
        List<Integer> entryList = this.queryService.qryEntryListByTournament(tournamentId);
        // get event_result list
        Map<Integer, EntryEventResultEntity> eventResultMap = this.getEntryEventResultByEvent(event, entryList);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("event:{}, tournament:{}, event_result not update", event, tournamentId);
            return;
        }
        // get event_live
        Map<String, EventLiveEntity> eventLiveMap = this.redisCacheService.getEventLiveByEvent(event);
        if (CollectionUtils.isEmpty(eventResultMap)) {
            log.error("event:{}, tournament:{}, event_live not update", event, tournamentId);
            return;
        }
        Random random = new Random();
        // tournament_knockout_result
        this.updateKnockoutResult(tournamentId, event, eventResultMap, eventLiveMap, random);
        // tournament_knockout
        this.updateKnockoutInfo(tournamentId, event, random);
        // next round entry if necessary
        this.updateNextKnockout(tournamentId, event);
    }

    private void updateKnockoutResult(int tournamentId, int event, Map<Integer, EntryEventResultEntity> eventResultMap, Map<String, EventLiveEntity> eventLiveMap, Random random) {
        List<TournamentKnockoutResultEntity> updateList = Lists.newArrayList();
        // tournament_knockout_result
        List<TournamentKnockoutResultEntity> tournamentKnockoutResultList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                .eq(TournamentKnockoutResultEntity::getEvent, event));
        if (CollectionUtils.isEmpty(tournamentKnockoutResultList)) {
            return;
        }
        tournamentKnockoutResultList.forEach(o -> {
            int homeEntry = o.getHomeEntry();
            int awayEntry = o.getAwayEntry();
            EntryEventResultEntity homeEventResult = eventResultMap.getOrDefault(homeEntry, this.initKnockoutResultEntity(homeEntry));
            EntryEventResultEntity awayEventResult = eventResultMap.getOrDefault(awayEntry, this.initKnockoutResultEntity(awayEntry));
            // entry_pick
            List<EntryPickData> homePickList = Lists.newArrayList();
            if (homeEventResult.getEntry() > 0) {
                homePickList = JsonUtils.json2Collection(homeEventResult.getEventPicks(), List.class, EntryPickData.class);
            }
            List<EntryPickData> awayPickList = Lists.newArrayList();
            if (awayEventResult.getEntry() > 0) {
                awayPickList = JsonUtils.json2Collection(awayEventResult.getEventPicks(), List.class, EntryPickData.class);
            }
            String homeChip = homeEventResult.getEventChip();
            String awayChip = awayEventResult.getEventChip();
            // net_points
            int homeNetPoints = homeEventResult.getEventNetPoints();
            int awayNetPoints = awayEventResult.getEventNetPoints();
            // goals_scored
            int homeGoalsScored = this.calcTotalGoalsScored(homePickList, homeChip, eventLiveMap);
            int awayGoalsScored = this.calcTotalGoalsScored(awayPickList, awayChip, eventLiveMap);
            // goals_conceded
            int homeGoalsConceded = this.calcTotalGoalsConceded(homePickList, homeChip, eventLiveMap);
            int awayGoalsConceded = this.calcTotalGoalsConceded(awayPickList, awayChip, eventLiveMap);
            // match winner
            int matchWinner = this.getMatchWinner(homeEntry, awayEntry, homeNetPoints, awayNetPoints, homeGoalsScored, awayGoalsScored, homeGoalsConceded, awayGoalsConceded, random);
            // update
            o
                    .setHomeEntryNetPoints(homeEntry > 0 ? homeEventResult.getEventPoints() : 0)
                    .setHomeEntryRank(homeEntry > 0 ? homeEventResult.getEventRank() : 0)
                    .setHomeEntryGoalsScored(homeGoalsScored)
                    .setHomeEntryGoalsConceded(homeGoalsConceded)
                    .setAwayEntryNetPoints(awayEntry > 0 ? awayEventResult.getEventPoints() : 0)
                    .setAwayEntryRank(awayEntry > 0 ? awayEventResult.getEventRank() : 0)
                    .setAwayEntryGoalsScored(awayGoalsScored)
                    .setAwayEntryGoalsConceded(awayGoalsConceded)
                    .setMatchWinner(matchWinner);
            updateList.add(o);
        });
        this.tournamentKnockoutResultService.updateBatchById(updateList);
        log.info("event:{}, tournament:{}, update tournament_knockout_result size:{}", event, tournamentId, updateList.size());
    }

    private EntryEventResultEntity initKnockoutResultEntity(int entry) {
        return new EntryEventResultEntity()
                .setEntry(entry)
                .setEventChip("")
                .setEventNetPoints(0)
                .setEventRank(0)
                .setEventPicks("");
    }

    private int calcTotalGoalsScored(List<EntryPickData> pickList, String chip, Map<String, EventLiveEntity> eventLiveMap) {
        if (CollectionUtils.isEmpty(pickList)) {
            return 0;
        }
        if (StringUtils.equals(Chip.BB.name(), chip)) {
            return pickList
                    .stream()
                    .map(EntryPickData::getElement)
                    .mapToInt(o -> {
                        EventLiveEntity eventLiveEntity = eventLiveMap.getOrDefault(String.valueOf(o), null);
                        if (eventLiveEntity == null) {
                            return 0;
                        }
                        return eventLiveEntity.getGoalsScored();
                    })
                    .sum();
        }
        return pickList
                .stream()
                .filter(o -> o.getPosition() < 12)
                .map(EntryPickData::getElement)
                .mapToInt(o -> {
                    EventLiveEntity eventLiveEntity = eventLiveMap.getOrDefault(String.valueOf(o), null);
                    if (eventLiveEntity == null) {
                        return 0;
                    }
                    return eventLiveEntity.getGoalsScored();
                })
                .sum();
    }

    private int calcTotalGoalsConceded(List<EntryPickData> pickList, String chip, Map<String, EventLiveEntity> eventLiveMap) {
        if (CollectionUtils.isEmpty(pickList)) {
            return 0;
        }
        if (StringUtils.equals(Chip.BB.name(), chip)) {
            return pickList
                    .stream()
                    .map(EntryPickData::getElement)
                    .mapToInt(o -> {
                        EventLiveEntity eventLiveEntity = eventLiveMap.getOrDefault(String.valueOf(o), null);
                        if (eventLiveEntity == null) {
                            return 0;
                        }
                        return eventLiveEntity.getGoalsConceded();
                    })
                    .sum();
        }
        return pickList
                .stream()
                .filter(o -> o.getPosition() < 12)
                .map(EntryPickData::getElement)
                .mapToInt(o -> {
                    EventLiveEntity eventLiveEntity = eventLiveMap.getOrDefault(String.valueOf(o), null);
                    if (eventLiveEntity == null) {
                        return 0;
                    }
                    return eventLiveEntity.getGoalsConceded();
                })
                .sum();
    }

    private void updateKnockoutInfo(int tournamentId, int event, Random random) {
        List<TournamentKnockoutEntity> updateList = Lists.newArrayList();
        // get current round
        int roundMatchId = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                        .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                        .eq(TournamentKnockoutResultEntity::getEvent, event))
                .stream()
                .map(TournamentKnockoutResultEntity::getMatchId)
                .findFirst()
                .orElse(0);
        if (roundMatchId <= 0) {
            return;
        }
        int round = this.tournamentKnockoutService.getOne(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                        .eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
                        .eq(TournamentKnockoutEntity::getMatchId, roundMatchId))
                .getRound();
        if (round <= 0) {
            return;
        }
        List<TournamentKnockoutEntity> tournamentKnockoutList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                .eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
                .eq(TournamentKnockoutEntity::getRound, round));
        if (CollectionUtils.isEmpty(tournamentKnockoutList)) {
            return;
        }
        List<Integer> roundMatchIdList = tournamentKnockoutList
                .stream()
                .map(TournamentKnockoutEntity::getMatchId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(roundMatchIdList)) {
            return;
        }
        Multimap<Integer, TournamentKnockoutResultData> knockoutRoundResultDataMap = HashMultimap.create();
        this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                        .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                        .in(TournamentKnockoutResultEntity::getMatchId, roundMatchIdList))
                .forEach(o -> {
                    TournamentKnockoutResultData data = BeanUtil.copyProperties(o, TournamentKnockoutResultData.class);
                    data.setWinnerRank(o.getMatchWinner().equals(o.getHomeEntry()) ? o.getHomeEntryRank() : o.getAwayEntryRank());
                    knockoutRoundResultDataMap.put(o.getMatchId(), data);
                });
        // update
        tournamentKnockoutList.forEach(o -> {
            int homeEntry = o.getHomeEntry();
            int awayEntry = o.getAwayEntry();
            Collection<TournamentKnockoutResultData> tournamentKnockoutResultDataCollection = knockoutRoundResultDataMap.get(o.getMatchId());
            int homeEntryRoundNetPoints = tournamentKnockoutResultDataCollection
                    .stream()
                    .filter(i -> i.getHomeEntry() == homeEntry)
                    .mapToInt(TournamentKnockoutResultData::getHomeEntryNetPoints)
                    .sum() + tournamentKnockoutResultDataCollection
                    .stream()
                    .filter(i -> i.getAwayEntry() == homeEntry)
                    .mapToInt(TournamentKnockoutResultData::getAwayEntryNetPoints)
                    .sum();
            int homeEntryRoundGoalsScored = tournamentKnockoutResultDataCollection
                    .stream()
                    .filter(i -> i.getHomeEntry() == homeEntry)
                    .mapToInt(TournamentKnockoutResultData::getHomeEntryGoalsScored)
                    .sum() + tournamentKnockoutResultDataCollection
                    .stream()
                    .filter(i -> i.getAwayEntry() == homeEntry)
                    .mapToInt(TournamentKnockoutResultData::getAwayEntryGoalsScored)
                    .sum();
            int homeEntryRoundGoalsConceded = tournamentKnockoutResultDataCollection
                    .stream()
                    .filter(i -> i.getHomeEntry() == homeEntry)
                    .mapToInt(TournamentKnockoutResultData::getHomeEntryGoalsConceded)
                    .sum() + tournamentKnockoutResultDataCollection
                    .stream()
                    .filter(i -> i.getAwayEntry() == homeEntry)
                    .mapToInt(TournamentKnockoutResultData::getAwayEntryGoalsConceded)
                    .sum();
            int awayEntryRoundNetPoints = tournamentKnockoutResultDataCollection
                    .stream()
                    .filter(i -> i.getHomeEntry() == awayEntry)
                    .mapToInt(TournamentKnockoutResultData::getHomeEntryNetPoints)
                    .sum() + tournamentKnockoutResultDataCollection
                    .stream()
                    .filter(i -> i.getAwayEntry() == awayEntry)
                    .mapToInt(TournamentKnockoutResultData::getAwayEntryNetPoints)
                    .sum();
            int awayEntryRoundGoalsScored = tournamentKnockoutResultDataCollection
                    .stream()
                    .filter(i -> i.getHomeEntry() == awayEntry)
                    .mapToInt(TournamentKnockoutResultData::getHomeEntryNetPoints)
                    .sum() + tournamentKnockoutResultDataCollection
                    .stream()
                    .filter(i -> i.getAwayEntry() == awayEntry)
                    .mapToInt(TournamentKnockoutResultData::getAwayEntryNetPoints)
                    .sum();
            int awayEntryRoundGoalsConceded = tournamentKnockoutResultDataCollection
                    .stream()
                    .filter(i -> i.getHomeEntry() == awayEntry)
                    .mapToInt(TournamentKnockoutResultData::getHomeEntryNetPoints)
                    .sum() + tournamentKnockoutResultDataCollection
                    .stream()
                    .filter(i -> i.getAwayEntry() == awayEntry)
                    .mapToInt(TournamentKnockoutResultData::getAwayEntryNetPoints)
                    .sum();
            o
                    .setHomeEntryNetPoints(homeEntryRoundNetPoints)
                    .setHomeEntryGoalsScored(homeEntryRoundGoalsScored)
                    .setHomeEntryGoalsConceded(homeEntryRoundGoalsConceded)
                    .setAwayEntryNetPoints(awayEntryRoundNetPoints)
                    .setAwayEntryGoalsScored(awayEntryRoundGoalsScored)
                    .setAwayEntryGoalsConceded(awayEntryRoundGoalsConceded)
                    .setRoundWinner(this.getRoundWinner(tournamentKnockoutResultDataCollection, random, homeEntry, awayEntry, homeEntryRoundNetPoints, homeEntryRoundGoalsScored, homeEntryRoundGoalsConceded, awayEntryRoundNetPoints, awayEntryRoundGoalsScored, awayEntryRoundGoalsConceded));
            updateList.add(o);
        });
        this.tournamentKnockoutService.updateBatchById(updateList);
        log.info("event:{}, tournament:{}, update tournament_knockout_info size:{}", tournamentId, event, updateList.size());
    }

    private void updateNextKnockout(int tournamentId, int event) {
        // check
        List<TournamentKnockoutEntity> nextKnockoutInfoList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                .eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
                .eq(TournamentKnockoutEntity::getEndGw, event));
        if (CollectionUtils.isEmpty(nextKnockoutInfoList)) {
            return;
        }
        Multimap<Integer, TournamentKnockoutEntity> nextKnockoutMultiMap = HashMultimap.create(); // match_id -> tournament_knockout_info
        nextKnockoutInfoList.forEach(o -> nextKnockoutMultiMap.put(o.getNextMatchId(), o));
        // update next round tournament_knockout
        List<TournamentKnockoutEntity> updateKnockoutList = Lists.newArrayList();
        Table<Integer, Integer, Integer> matchIdHomeAwayEntryTable = HashBasedTable.create(); // match -> home_entry -> away_entry
        List<Integer> nextMatchIdList = new ArrayList<>(nextKnockoutMultiMap.keySet());
        List<TournamentKnockoutEntity> nextRoundKnockoutList = this.tournamentKnockoutService.list(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                .eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
                .in(TournamentKnockoutEntity::getMatchId, nextMatchIdList));
        if (CollectionUtils.isEmpty(nextRoundKnockoutList)) {
            return;
        }
        nextRoundKnockoutList.forEach(o -> {
            int matchId = o.getMatchId();
            List<TournamentKnockoutEntity> nextMatchInfoList = nextKnockoutMultiMap.get(matchId)
                    .stream()
                    .sorted(Comparator.comparing(TournamentKnockoutEntity::getMatchId))
                    .toList();
            int homeEntry = nextMatchInfoList.get(0).getRoundWinner();
            int awayEntry = nextMatchInfoList.get(1).getRoundWinner();
            matchIdHomeAwayEntryTable.put(matchId, homeEntry, awayEntry);
            o
                    .setHomeEntry(homeEntry)
                    .setAwayEntry(awayEntry);
            updateKnockoutList.add(o);
        });
        this.tournamentKnockoutService.updateBatchById(updateKnockoutList);
        log.info("event:{}, tournament:{}, update tournament_knockout_info size:{}", tournamentId, event, updateKnockoutList.size());
        // update next round tournament_knockout_result
        List<TournamentKnockoutResultEntity> updateKnockoutResultList = Lists.newArrayList();
        List<TournamentKnockoutResultEntity> nextRoundKnockoutResultList = this.tournamentKnockoutResultService.list(new QueryWrapper<TournamentKnockoutResultEntity>().lambda()
                .eq(TournamentKnockoutResultEntity::getTournamentId, tournamentId)
                .in(TournamentKnockoutResultEntity::getMatchId, nextMatchIdList));
        if (CollectionUtils.isEmpty(nextRoundKnockoutResultList)) {
            return;
        }
        nextRoundKnockoutResultList.forEach(o -> {
            int matchId = o.getMatchId();
            int playAgainstId = o.getPlayAgainstId();
            Map<Integer, Integer> entryMap = matchIdHomeAwayEntryTable.row(matchId);
            if (CollectionUtils.isEmpty(entryMap)) {
                return;
            }
            int homeEntry = entryMap.keySet().stream().findFirst().orElse(0);
            int awayEntry = entryMap.values().stream().findFirst().orElse(0);
            if (playAgainstId % 2 != 0) {
                o
                        .setHomeEntry(homeEntry)
                        .setAwayEntry(awayEntry);
            } else {
                o
                        .setHomeEntry(awayEntry)
                        .setAwayEntry(homeEntry);
            }
            updateKnockoutResultList.add(o);
        });
        this.tournamentKnockoutResultService.updateBatchById(updateKnockoutResultList);
        log.info("event:{}, tournament:{}, update tournament_knockout_result size:{}", tournamentId, event, updateKnockoutResultList.size());
    }

    private int getMatchWinner(int homeEntry, int awayEntry, int homeNetPoint, int awayNetPoints, int homeGoalsScored, int awayGoalsScored, int homeGoalsConceded, int awayGoalsConceded, Random random) {
        // if blank
        if (homeEntry <= 0) {
            return awayEntry;
        }
        if (awayEntry <= 0) {
            return homeEntry;
        }
        // compare order: net points; most goals scored; fewest goals conceded; random
        int winner = this.compareNetPoint(homeEntry, awayEntry, homeNetPoint, awayNetPoints);
        if (winner != 0) {
            return winner;
        }
        winner = this.compareGoalsScored(homeEntry, awayEntry, homeGoalsScored, awayGoalsScored);
        if (winner != 0) {
            return winner;
        }
        winner = this.compareGoalsConceded(homeEntry, awayEntry, homeGoalsConceded, awayGoalsConceded);
        if (winner != 0) {
            return winner;
        }
        return this.randomWinner(homeEntry, awayEntry, random);
    }

    private int compareNetPoint(int homeEntry, int awayEntry, int homeNetPoint, int awayNetPoint) {
        if (homeNetPoint > awayNetPoint) {
            return homeEntry;
        } else if (homeNetPoint < awayNetPoint) {
            return awayEntry;
        }
        return 0;
    }

    private int compareGoalsScored(int homeEntry, int awayEntry, int homeGoalsScored, int awayGoalsScored) {
        if (homeGoalsScored > awayGoalsScored) {
            return homeEntry;
        } else if (homeGoalsScored < awayGoalsScored) {
            return awayEntry;
        }
        return 0;
    }

    private int compareGoalsConceded(int homeEntry, int awayEntry, int homeGoalsConceded, int awayGoalsConceded) {
        if (homeGoalsConceded < awayGoalsConceded) {
            return homeEntry;
        } else if (homeGoalsConceded > awayGoalsConceded) {
            return awayEntry;
        }
        return 0;
    }

    private int randomWinner(int homeEntry, int awayEntry, Random random) {
        if (random.nextInt(10) % 2 == 0) {
            return homeEntry;
        }
        return awayEntry;
    }

    private int getRoundWinner(Collection<TournamentKnockoutResultData> collection, Random random,
                               int homeEntry, int awayEntry,
                               int homeEntryRoundNetPoints, int homeEntryRoundGoalsScored, int homeEntryRoundGoalsConceded,
                               int awayEntryRoundNetPoints, int awayEntryRoundGoalsScored, int awayEntryRoundGoalsConceded) {
        if (collection.size() == 1) {
            return collection.stream().map(TournamentKnockoutResultData::getMatchWinner).findFirst().orElse(0);
        }
        List<TournamentKnockoutResultData> winners = new ArrayList<>(collection);
        int firstWinner = winners.get(0).getMatchWinner();
        Map<Integer, Integer> secondWinnerMap = collection.stream().filter(tournamentKnockoutResultData -> tournamentKnockoutResultData.getMatchWinner() != firstWinner).limit(1).collect(Collectors.toMap(TournamentKnockoutResultData::getMatchWinner, TournamentKnockoutResultData::getWinnerRank)); // find the other match winner
        int secondWinner = secondWinnerMap.keySet().stream().findFirst().orElse(0);
        if (secondWinner == 0) { // all matches won by the first winner
            return firstWinner;
        }
        // compare order: net points; most goals scored; fewest goals conceded; random
        int roundWinner = this.compareNetPoint(homeEntry, awayEntry, homeEntryRoundNetPoints, awayEntryRoundNetPoints);
        if (roundWinner != 0) {
            return roundWinner;
        }
        roundWinner = this.compareGoalsScored(homeEntry, awayEntry, homeEntryRoundGoalsScored, awayEntryRoundGoalsScored);
        if (roundWinner != 0) {
            return roundWinner;
        }
        roundWinner = this.compareGoalsConceded(homeEntry, awayEntry, homeEntryRoundGoalsConceded, awayEntryRoundGoalsConceded);
        if (roundWinner != 0) {
            return roundWinner;
        }
        return this.randomWinner(homeEntry, awayEntry, random);
    }

    /**
     * @implNote league
     */
    @Override
    public void updateEntryLeagueEventResult(int event, int leagueId, int entry) {
        if (event < 1 || event > 38) {
            log.error("event:{}, params error", event);
            return;
        }
        LeagueEventInfoData data = this.getLeagueInfoId(leagueId);
        if (data == null) {
            log.error("event:{}, league_id:{}, tournament_info not exists", event, leagueId);
            return;
        }
        String leagueType = data.getLeagueType();
        // league_event_stat
        LeagueEventReportEntity leagueEventReportEntity = this.leagueEventReportService.getOne(new QueryWrapper<LeagueEventReportEntity>().lambda().eq(LeagueEventReportEntity::getLeagueId, leagueId).eq(LeagueEventReportEntity::getLeagueType, leagueType).eq(LeagueEventReportEntity::getEvent, event).eq(LeagueEventReportEntity::getEntry, entry));
        if (leagueEventReportEntity == null) {
            log.error("event:{}, entry:{}, leagueId:{}, leagueType:{}, league_event_report record not exists", event, entry, leagueId, leagueType);
            return;
        }
        // prepare
        Map<Integer, PlayerStatEntity> playerStatMap = Maps.newHashMap();
        this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda().eq(PlayerStatEntity::getEvent, event)).forEach(o -> {
            int element = o.getElement();
            if (playerStatMap.containsKey(element)) {
                PlayerStatEntity playerStatEntity = playerStatMap.get(element);
                if (LocalDateTime.parse(playerStatEntity.getUpdateTime().replace(" ", "T")).isAfter(LocalDateTime.parse(o.getUpdateTime().replace(" ", "T")))) {
                    playerStatMap.put(element, playerStatEntity);
                }
            } else {
                playerStatMap.put(element, o);
            }
        });
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event)).stream().collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).eq(EntryEventResultEntity::getEntry, entry)).stream().collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        // collect
        LeagueEventReportEntity leagueEventReport = this.updateEntryEventResultStat(event, leagueEventReportEntity, playerStatMap, eventLiveMap, entryEventResultMap);
        // update
        this.leagueEventReportService.updateById(leagueEventReport);
        log.info("event:{}, entry:{}, leagueId:{}, leagueType:{}, update league_event_report!", event, entry, leagueId, leagueType);
    }

    @Override
    public void insertLeagueEventPick(int event, int leagueId) {
        if (event < 1 || event > 38 || leagueId <= 0) {
            log.error("event:{}, params error", event);
            return;
        }
        LeagueEventInfoData data = this.getLeagueInfoId(leagueId);
        if (data == null) {
            log.error("event:{}, league_id:{}, tournament_info not exists", event, leagueId);
            return;
        }
        int tournamentId = data.getId();
        String leagueType = data.getLeagueType();
        int limit = data.getLimit();
        String name = limit == 0 ? data.getLeagueName() : data.getLeagueName() + "(top " + (int) NumberUtil.div(limit, 1000, 0, RoundingMode.FLOOR) + "k)";
        // get entry list
        List<Integer> entryList = this.getReportEntryList(tournamentId, data);
        if (CollectionUtils.isEmpty(entryList)) {
            log.error("event:{}, leagueId:{}, leagueType:{}, no need to insert", event, leagueId, leagueType);
            return;
        }
        // init
        List<Integer> existsList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda().eq(LeagueEventReportEntity::getLeagueId, leagueId).eq(LeagueEventReportEntity::getEvent, event).in(LeagueEventReportEntity::getEntry, entryList)).stream().map(LeagueEventReportEntity::getEntry).toList();
        entryList = entryList.stream().filter(o -> !existsList.contains(o)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            log.info("event:{}, leagueId:{}, no need to insert", event, leagueId);
            return;
        }
        // get user picks
        List<CompletableFuture<LeagueEventReportEntity>> future = entryList.stream().map(o -> CompletableFuture.supplyAsync(() -> this.initEntryEventSelectPick(event, o, leagueId, leagueType, name))).toList();
        List<LeagueEventReportEntity> insertList = future.stream().map(CompletableFuture::join).filter(Objects::nonNull).collect(Collectors.toList());
        // save
        this.leagueEventReportService.saveBatch(insertList);
        log.info("event:{}, leagueId:{}, leagueType:{}, insert league_event_report size:{}", event, leagueId, leagueType, insertList.size());
    }

    private LeagueEventInfoData getLeagueInfoId(int leagueId) {
        if (leagueId == 65) {
            return new LeagueEventInfoData().setLeagueId(65).setLeagueName("China").setLeagueType("Classic").setLimit(0);
        } else if (leagueId == 314) {
            return new LeagueEventInfoData().setLeagueId(314).setLeagueName("Overall").setLeagueType("Classic").setLimit(10000);
        } else {
            return this.queryService.qryLeagueEventReportDataByLeagueId(leagueId);
        }
    }

    private List<Integer> getReportEntryList(int tournamentId, LeagueEventInfoData data) {
        int leagueId = data.getLeagueId();
        if (tournamentId == 0) {
            String leagueType = data.getLeagueType();
            int limit = data.getLimit();
            LeagueInfoData leagueInfoData = new LeagueInfoData();
            if (LeagueType.valueOf(leagueType).equals(LeagueType.Classic)) {
                leagueInfoData = this.interfaceService.getEntryInfoListFromClassicByLimit(leagueId, limit);
            } else if (LeagueType.valueOf(leagueType).equals(LeagueType.H2h)) {
                leagueInfoData = this.interfaceService.getEntryInfoListFromH2hByLimit(leagueId, limit);
            }
            if (CollectionUtils.isEmpty(leagueInfoData.getEntryInfoList())) {
                return null;
            }
            return leagueInfoData.getEntryInfoList().stream().map(EntryInfoData::getEntry).collect(Collectors.toList());
        } else {
            return this.queryService.qryEntryListByTournament(tournamentId);
        }
    }

    private LeagueEventReportEntity initEntryEventSelectPick(int event, int entry, int leagueId, String leagueType, String leagueName) {
        List<Pick> picks = this.interfaceService.getUserPicks(event, entry).orElse(new UserPicksRes()).getPicks();
        if (CollectionUtils.isEmpty(picks)) {
            log.error("event:{}, entry:{}, get fpl server user_picks empty", event, entry);
            return null;
        }
        LeagueEventReportEntity leagueEventReportEntity = new LeagueEventReportEntity().setLeagueId(leagueId).setLeagueType(leagueType).setLeagueName(leagueName).setEntry(entry).setEntryName("").setPlayerName("").setOverallPoints(0).setOverallRank(0).setTeamValue(0).setBank(0).setEvent(event).setEventPoints(0).setEventTransfers(0).setEventTransfersCost(0).setEventNetPoints(0).setEventBenchPoints(0).setEventAutoSubPoints(0).setEventRank(0).setEventChip("").setPosition1(picks.get(0).getElement()).setPosition2(picks.get(1).getElement()).setPosition3(picks.get(2).getElement()).setPosition4(picks.get(3).getElement()).setPosition5(picks.get(4).getElement()).setPosition6(picks.get(5).getElement()).setPosition7(picks.get(6).getElement()).setPosition8(picks.get(7).getElement()).setPosition9(picks.get(8).getElement()).setPosition10(picks.get(9).getElement()).setPosition11(picks.get(10).getElement()).setPosition12(picks.get(11).getElement()).setPosition13(picks.get(12).getElement()).setPosition14(picks.get(13).getElement()).setPosition15(picks.get(14).getElement());
        // captain
        leagueEventReportEntity.setCaptain(picks.stream().filter(Pick::isCaptain).map(Pick::getElement).findFirst().orElse(0)).setCaptainPoints(0).setCaptainBlank(true).setCaptainSelected("");
        // vice captain
        leagueEventReportEntity.setViceCaptain(picks.stream().filter(Pick::isViceCaptain).map(Pick::getElement).findFirst().orElse(0)).setViceCaptainPoints(0).setViceCaptainBlank(true).setViceCaptainSelected("");
        // highest score
        leagueEventReportEntity.setHighestScore(0).setHighestScorePoints(0).setHighestScoreBlank(true).setHighestScoreSelected("");
        // played captain
        leagueEventReportEntity.setPlayedCaptain(0);
        return leagueEventReportEntity;
    }

    @Override
    public void updateLeagueEventResult(int event, int leagueId) {
        if (event < 1 || event > 38 || leagueId <= 0) {
            log.error("event:{}, params error", event);
            return;
        }
        LeagueEventInfoData data = this.getLeagueInfoId(leagueId);
        if (data == null) {
            log.error("event:{}, tournament:{}, tournament_info not exists", event, leagueId);
            return;
        }
        String leagueType = data.getLeagueType();
        // league_event_stat
        List<LeagueEventReportEntity> leagueEventStatList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda().eq(LeagueEventReportEntity::getLeagueId, leagueId).eq(LeagueEventReportEntity::getLeagueType, leagueType).eq(LeagueEventReportEntity::getEvent, event));
        if (CollectionUtils.isEmpty(leagueEventStatList)) {
            log.error("event:{}, leagueId:{}, leagueType:{}, league_event_report record not exists", event, leagueId, leagueType);
            return;
        }
        List<Integer> entryList = leagueEventStatList.stream().map(LeagueEventReportEntity::getEntry).collect(Collectors.toList());
        // prepare
        Map<Integer, PlayerStatEntity> playerStatMap = Maps.newHashMap();
        this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda().eq(PlayerStatEntity::getEvent, event)).forEach(o -> {
            int element = o.getElement();
            if (playerStatMap.containsKey(element)) {
                PlayerStatEntity playerStatEntity = playerStatMap.get(element);
                if (LocalDateTime.parse(playerStatEntity.getUpdateTime().replace(" ", "T")).isAfter(LocalDateTime.parse(o.getUpdateTime().replace(" ", "T")))) {
                    playerStatMap.put(element, playerStatEntity);
                }
            } else {
                playerStatMap.put(element, o);
            }
        });
        if (CollectionUtils.isEmpty(playerStatMap)) {
            log.error("event:{}, leagueId:{}, leagueType:{}, player_stat not exists", event, leagueId, leagueType);
            return;
        }
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getEvent, event)).stream().collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        if (CollectionUtils.isEmpty(eventLiveMap)) {
            log.error("event:{}, leagueId:{}, leagueType:{}, event_live not exists", event, leagueId, leagueType);
            return;
        }
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda().eq(EntryEventResultEntity::getEvent, event).in(EntryEventResultEntity::getEntry, entryList)).stream().collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        if (CollectionUtils.isEmpty(entryEventResultMap)) {
            log.error("event:{}, leagueId:{}, leagueType:{}, entry_event_result not exists", event, leagueId, leagueType);
            return;
        }
        // collect
        List<CompletableFuture<LeagueEventReportEntity>> future = leagueEventStatList.stream().map(o -> CompletableFuture.supplyAsync(() -> this.updateEntryEventResultStat(event, o, playerStatMap, eventLiveMap, entryEventResultMap))).toList();
        List<LeagueEventReportEntity> leagueEventStatEntityList = future.stream().map(CompletableFuture::join).collect(Collectors.toList());
        // update
        this.leagueEventReportService.updateBatchById(leagueEventStatEntityList);
        log.info("leagueId:{}, leagueType:{}, event:{}, update league_event_report size:{}!", leagueId, leagueType, event, leagueEventStatEntityList.size());
    }

    private LeagueEventReportEntity updateEntryEventResultStat(int event, LeagueEventReportEntity leagueEventStatEntity, Map<Integer, PlayerStatEntity> playerStatMap, Map<Integer, EventLiveEntity> eventLiveMap, Map<Integer, EntryEventResultEntity> entryEventResultMap) {
        int entry = leagueEventStatEntity.getEntry();
        // entry_info
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(entry);
        if (entryInfoData != null) {
            leagueEventStatEntity.setEntryName(entryInfoData.getEntryName()).setPlayerName(entryInfoData.getPlayerName());
        }
        // entry_event_result
        if (entryEventResultMap.containsKey(entry)) {
            EntryEventResultEntity entryEventResultEntity = entryEventResultMap.get(entry);
            leagueEventStatEntity.setEventPoints(entryEventResultEntity.getEventPoints()).setEventTransfers(entryEventResultEntity.getEventTransfers()).setEventTransfersCost(entryEventResultEntity.getEventTransfersCost()).setEventNetPoints(entryEventResultEntity.getEventNetPoints()).setEventBenchPoints(entryEventResultEntity.getEventBenchPoints()).setEventAutoSubPoints(entryEventResultEntity.getEventAutoSubPoints()).setEventRank(entryEventResultEntity.getEventRank()).setEventChip(entryEventResultEntity.getEventChip()).setOverallPoints(entryEventResultEntity.getOverallPoints()).setOverallRank(entryEventResultEntity.getOverallRank()).setTeamValue(entryEventResultEntity.getTeamValue()).setBank(entryEventResultEntity.getBank());
        } else {
            Map<Integer, Integer> elementPointsMap = eventLiveMap.values().stream().collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
            this.interfaceService.getUserPicks(event, entry).ifPresent(userPicksRes -> leagueEventStatEntity.setEventPoints(userPicksRes.getEntryHistory().getPoints()).setEventTransfers(userPicksRes.getEntryHistory().getEventTransfers()).setEventTransfersCost(userPicksRes.getEntryHistory().getEventTransfersCost()).setEventNetPoints(userPicksRes.getEntryHistory().getPoints() - userPicksRes.getEntryHistory().getEventTransfersCost()).setEventBenchPoints(userPicksRes.getEntryHistory().getPointsOnBench()).setEventAutoSubPoints(userPicksRes.getAutomaticSubs().size() == 0 ? 0 : this.calcAutoSubPoints(userPicksRes.getAutomaticSubs(), elementPointsMap)).setEventRank(userPicksRes.getEntryHistory().getRank()).setEventChip(StringUtils.isBlank(userPicksRes.getActiveChip()) ? Chip.NONE.getValue() : userPicksRes.getActiveChip()).setOverallPoints(userPicksRes.getEntryHistory().getTotalPoints()).setOverallRank(userPicksRes.getEntryHistory().getOverallRank()).setTeamValue(userPicksRes.getEntryHistory().getValue()).setBank(userPicksRes.getEntryHistory().getBank()));
        }
        // captain
        int captain = leagueEventStatEntity.getCaptain();
        EventLiveEntity captainEventLiveEntity;
        if (eventLiveMap.containsKey(captain)) {
            captainEventLiveEntity = eventLiveMap.get(captain);
            leagueEventStatEntity.setCaptainPoints(captainEventLiveEntity.getTotalPoints()).setCaptainBlank(this.setElementBlank(captainEventLiveEntity)).setCaptainSelected(playerStatMap.containsKey(captain) ? playerStatMap.get(captain).getSelectedByPercent() + "%" : "");
        } else {
            captainEventLiveEntity = new EventLiveEntity().setMinutes(0).setTotalPoints(0);
        }
        // vice captain
        int viceCaptain = leagueEventStatEntity.getViceCaptain();
        EventLiveEntity viceCaptainEventLiveEntity;
        if (eventLiveMap.containsKey(viceCaptain)) {
            viceCaptainEventLiveEntity = eventLiveMap.get(viceCaptain);
            leagueEventStatEntity.setViceCaptainPoints(viceCaptainEventLiveEntity.getTotalPoints()).setViceCaptainBlank(this.setElementBlank(viceCaptainEventLiveEntity)).setViceCaptainSelected(playerStatMap.containsKey(viceCaptain) ? playerStatMap.get(viceCaptain).getSelectedByPercent() + "%" : "");
        } else {
            viceCaptainEventLiveEntity = new EventLiveEntity().setMinutes(0).setTotalPoints(0);
        }
        leagueEventStatEntity.setPlayedCaptain(this.selectPlayedCaptain(captainEventLiveEntity, viceCaptainEventLiveEntity));
        // highest score
        int highestElement = this.getHighestScoreElement(leagueEventStatEntity, eventLiveMap);
        if (eventLiveMap.containsKey(highestElement)) {
            EventLiveEntity highestEventLiveEntity = eventLiveMap.get(highestElement);
            leagueEventStatEntity.setHighestScore(highestElement).setHighestScorePoints(highestEventLiveEntity.getTotalPoints()).setHighestScoreBlank(this.setElementBlank(highestEventLiveEntity)).setHighestScoreSelected(playerStatMap.containsKey(highestElement) ? playerStatMap.get(highestElement).getSelectedByPercent() + "%" : "");
        }
        // played captain
        if (leagueEventStatEntity.getPlayedCaptain() == 0) {
            leagueEventStatEntity.setPlayedCaptain(leagueEventStatEntity.getCaptain());
        }
        return leagueEventStatEntity;
    }

    private int selectPlayedCaptain(EventLiveEntity captainEventLiveEntity, EventLiveEntity viceCaptainEventLiveEntity) {
        if (captainEventLiveEntity.getMinutes() == 0 && viceCaptainEventLiveEntity.getMinutes() > 0) {
            return viceCaptainEventLiveEntity.getElement();
        }
        return captainEventLiveEntity.getElement();
    }

    private int getHighestScoreElement(LeagueEventReportEntity leagueEventStatEntity, Map<Integer, EventLiveEntity> eventLiveMap) {
        Map<Integer, Integer> elementPointsMap = Maps.newHashMap();
        elementPointsMap.put(leagueEventStatEntity.getPosition1(), this.getElementEventPoints(leagueEventStatEntity.getPosition1(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition2(), this.getElementEventPoints(leagueEventStatEntity.getPosition2(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition3(), this.getElementEventPoints(leagueEventStatEntity.getPosition3(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition4(), this.getElementEventPoints(leagueEventStatEntity.getPosition4(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition5(), this.getElementEventPoints(leagueEventStatEntity.getPosition5(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition6(), this.getElementEventPoints(leagueEventStatEntity.getPosition6(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition7(), this.getElementEventPoints(leagueEventStatEntity.getPosition7(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition8(), this.getElementEventPoints(leagueEventStatEntity.getPosition8(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition9(), this.getElementEventPoints(leagueEventStatEntity.getPosition9(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition10(), this.getElementEventPoints(leagueEventStatEntity.getPosition10(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition11(), this.getElementEventPoints(leagueEventStatEntity.getPosition11(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition12(), this.getElementEventPoints(leagueEventStatEntity.getPosition12(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition13(), this.getElementEventPoints(leagueEventStatEntity.getPosition13(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition14(), this.getElementEventPoints(leagueEventStatEntity.getPosition14(), eventLiveMap));
        elementPointsMap.put(leagueEventStatEntity.getPosition15(), this.getElementEventPoints(leagueEventStatEntity.getPosition15(), eventLiveMap));
        // sort
        return elementPointsMap.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(0);
    }

    private int getElementEventPoints(int element, Map<Integer, EventLiveEntity> eventLiveMap) {
        if (eventLiveMap.containsKey(element)) {
            return eventLiveMap.get(element).getTotalPoints();
        }
        return 0;
    }

    private boolean setElementBlank(EventLiveEntity eventLiveEntity) {
        return eventLiveEntity.getGoalsScored() <= 0 && eventLiveEntity.getAssists() <= 0 && eventLiveEntity.getBonus() <= 0 && eventLiveEntity.getPenaltiesSaved() <= 0 && eventLiveEntity.getSaves() <= 3 && ((eventLiveEntity.getElementType() != 1 && eventLiveEntity.getElementType() != 2) || eventLiveEntity.getCleanSheets() <= 0);
    }

    /**
     * @implNote scout
     */
    @Override
    public void insertEventSourceScout(int event, String source, List<MapData<Integer>> scoutDataList) {
        if (event <= 0 || event > 38 || StringUtils.isEmpty(source) || CollectionUtils.isEmpty(scoutDataList)) {
            return;
        }
        Map<String, Integer> scoutDataMap = Maps.newHashMap(); // position -> element
        scoutDataList.forEach(o -> scoutDataMap.put(o.getKey(), o.getValue()));
        // check exist
        if (this.popularScoutResultService.count(new QueryWrapper<PopularScoutResultEntity>().lambda().eq(PopularScoutResultEntity::getEvent, event).eq(PopularScoutResultEntity::getSource, source)) > 0) {
            return;
        }
        // insert
        PopularScoutResultEntity popularScoutResultEntity = new PopularScoutResultEntity()
                .setEvent(event)
                .setSource(source)
                .setPosition1(scoutDataMap.get("1"))
                .setPosition1Points(0)
                .setPosition2(scoutDataMap.get("2"))
                .setPosition2Points(0)
                .setPosition3(scoutDataMap.get("3"))
                .setPosition3Points(0)
                .setPosition4(scoutDataMap.get("4"))
                .setPosition4Points(0)
                .setPosition5(scoutDataMap.get("5"))
                .setPosition5Points(0)
                .setPosition6(scoutDataMap.get("6"))
                .setPosition6Points(0)
                .setPosition7(scoutDataMap.get("7"))
                .setPosition7Points(0)
                .setPosition8(scoutDataMap.get("8"))
                .setPosition8Points(0)
                .setPosition9(scoutDataMap.get("9"))
                .setPosition9Points(0)
                .setPosition10(scoutDataMap.get("10"))
                .setPosition10Points(0)
                .setPosition11(scoutDataMap.get("11"))
                .setPosition11Points(0)
                .setPosition12(scoutDataMap.get("12"))
                .setPosition12Points(0)
                .setPosition13(scoutDataMap.get("13"))
                .setPosition13Points(0)
                .setPosition14(scoutDataMap.get("14"))
                .setPosition14Points(0)
                .setPosition15(scoutDataMap.get("15"))
                .setPosition15Points(0)
                .setCaptain(scoutDataMap.get("captain"))
                .setCaptainPoints(0)
                .setViceCaptain(scoutDataMap.get("vice_captain"))
                .setViceCaptainPoints(0)
                .setPlayedCaptain(0)
                .setPlayedCaptainPoints(0)
                .setRawTotalPoints(0)
                .setTotalPoints(0)
                .setAveragePoints(0)
                .setChip("");
        this.popularScoutResultService.save(popularScoutResultEntity);
    }

    @Override
    public void updateEventSourceScoutResult(int event) {
        List<PopularScoutResultEntity> list = Lists.newArrayList();
        // prepare
        String season = CommonUtils.getCurrentSeason();
        Map<String, EventLiveEntity> eventLiveMap = this.redisCacheService.getEventLiveByEvent(event);
        Map<Integer, Integer> pointsMap = eventLiveMap.values().stream().collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        EventOverallResultData eventOverallResultData = this.redisCacheService.getEventOverallResultByEvent(season, event);
        if (CollectionUtils.isEmpty(eventLiveMap) || eventOverallResultData == null) {
            return;
        }
        this.popularScoutResultService.list(new QueryWrapper<PopularScoutResultEntity>().lambda().eq(PopularScoutResultEntity::getEvent, event)).forEach(o -> {
            // update
            o.setEvent(event)
                    .setSource(o.getSource())
                    .setPosition1Points(pointsMap.getOrDefault(o.getPosition1(), 0))
                    .setPosition2Points(pointsMap.getOrDefault(o.getPosition2(), 0))
                    .setPosition3Points(pointsMap.getOrDefault(o.getPosition3(), 0))
                    .setPosition4Points(pointsMap.getOrDefault(o.getPosition4(), 0))
                    .setPosition5Points(pointsMap.getOrDefault(o.getPosition5(), 0))
                    .setPosition6Points(pointsMap.getOrDefault(o.getPosition6(), 0))
                    .setPosition7Points(pointsMap.getOrDefault(o.getPosition7(), 0))
                    .setPosition8Points(pointsMap.getOrDefault(o.getPosition8(), 0))
                    .setPosition9Points(pointsMap.getOrDefault(o.getPosition9(), 0))
                    .setPosition10Points(pointsMap.getOrDefault(o.getPosition10(), 0))
                    .setPosition11Points(pointsMap.getOrDefault(o.getPosition11(), 0))
                    .setPosition12Points(pointsMap.getOrDefault(o.getPosition12(), 0))
                    .setPosition13Points(pointsMap.getOrDefault(o.getPosition13(), 0))
                    .setPosition14Points(pointsMap.getOrDefault(o.getPosition14(), 0))
                    .setPosition15Points(pointsMap.getOrDefault(o.getPosition15(), 0))
                    .setCaptainPoints(pointsMap.getOrDefault(o.getCaptain(), 0))
                    .setViceCaptainPoints(pointsMap.getOrDefault(o.getViceCaptain(), 0))
                    .setPlayedCaptain(this.getScoutPlayedCaptain(o.getCaptain(), o.getViceCaptain(), eventLiveMap))
                    .setAveragePoints(eventOverallResultData.getAverageEntryScore());
            o.setPlayedCaptainPoints(pointsMap.getOrDefault(o.getPlayedCaptain(), 0));
            o.setRawTotalPoints(this.calcTotalPoints(o));
            o.setTotalPoints(o.getRawTotalPoints() + o.getPlayedCaptainPoints());
            list.add(o);
        });
        this.popularScoutResultService.updateBatchById(list);
        log.info("event:{}, update popular_scout_result size:{}!", event, list.size());
    }

    private int getScoutPlayedCaptain(int captain, int viceCaptain, Map<String, EventLiveEntity> eventLiveMap) {
        if (captain == 0 && viceCaptain == 0) {
            return 0;
        }
        EventLiveEntity captainEventLive = eventLiveMap.get(String.valueOf(captain));
        if (captainEventLive == null) {
            return 0;
        }
        return captainEventLive.getMinutes() != 0 ? captain : viceCaptain;
    }

    private int calcTotalPoints(PopularScoutResultEntity popularScoutResultEntity) {
        return popularScoutResultEntity.getPosition1Points() +
                popularScoutResultEntity.getPosition2Points() +
                popularScoutResultEntity.getPosition3Points() +
                popularScoutResultEntity.getPosition4Points() +
                popularScoutResultEntity.getPosition5Points() +
                popularScoutResultEntity.getPosition6Points() +
                popularScoutResultEntity.getPosition7Points() +
                popularScoutResultEntity.getPosition8Points() + popularScoutResultEntity.getPosition9Points() +
                popularScoutResultEntity.getPosition10Points() +
                popularScoutResultEntity.getPosition11Points();
    }

}

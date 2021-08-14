package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userpick.AutoSubs;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.IReportService;
import com.tong.fpl.service.IStaticService;
import com.tong.fpl.service.db.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 并发任务，不要走redis
 * <p>
 * Create by tong on 2020/9/2
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReportServiceImpl implements IReportService {

    private final IStaticService staticService;
    private final IQueryService queryService;
    private final IRedisCacheService redisCacheService;
    private final PlayerStatService playerStatService;
    private final EntryInfoService entryInfoService;
    private final EventLiveService eventLiveService;
    private final EntryEventResultService entryEventResultService;
    private final TournamentEntryService tournamentEntryService;
    private final LeagueEventReportService leagueEventReportService;

    @Override
    public boolean entryEvenLeagueEventExists(int event, int leagueId, String leagueType, int entry) {
        long count = this.leagueEventReportService.count(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .eq(LeagueEventReportEntity::getEvent, event)
                .eq(LeagueEventReportEntity::getEntry, entry));
        return count > 0;
    }

    @Override
    public boolean eventLeagueEventExists(int event, int leagueId, String leagueType) {
        long count = this.leagueEventReportService.count(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .eq(LeagueEventReportEntity::getEvent, event));
        return count > 0;
    }

    @Override
    public void insertEntryLeagueEventSelect(int event, int leagueId, String leagueType, int entry) {
        LeagueInfoData leagueInfoData = this.getLeagueDataByTypeAndId(leagueId, leagueType, 0);
        if (leagueInfoData == null) {
            return;
        }
        LeagueEventReportEntity leagueEventReportEntity = this.initEntryEventSelectStat(event, entry, leagueId, leagueType, leagueInfoData.getName());
        if (leagueEventReportEntity == null) {
            return;
        }
        // save
        this.leagueEventReportService.save(leagueEventReportEntity);
        log.info("leagueId:{}, leagueType:{}, event:{}, entry:{}, insert entry league_event_report!", leagueId, leagueType, event, entry);
    }

    @Override
    public void insertLeagueEventSelect(int event, int leagueId, String leagueType, int limit) {
        // get league Entry
        LeagueInfoData leagueInfoData = this.getLeagueDataByTypeAndId(leagueId, leagueType, limit);
        if (leagueInfoData == null) {
            return;
        }
        String leagueName = limit == 0 ? leagueInfoData.getName() : leagueInfoData.getName() + "(top " + (int) NumberUtil.div(limit, 1000, 0, RoundingMode.FLOOR) + "k)";
        // init league result stat
        List<EntryInfoData> entryInfoDataList = leagueInfoData.getEntryInfoList();
        if (CollectionUtils.isEmpty(entryInfoDataList)) {
            return;
        }
        // get user picks
        List<CompletableFuture<LeagueEventReportEntity>> future = entryInfoDataList
                .stream()
                .map(o ->
                        CompletableFuture.supplyAsync(() ->
                                this.initEntryEventSelectStat(event, o.getEntry(), leagueId, leagueType, leagueName)))
                .collect(Collectors.toList());
        List<LeagueEventReportEntity> leagueEventStatEntityList = future
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // save
        this.leagueEventReportService.saveBatch(leagueEventStatEntityList);
        log.info("leagueId:{}, leagueType:{}, event:{}, insert league_event_report size:{}!", leagueId, leagueType, event, leagueEventStatEntityList.size());
    }

    @Override
    public void insertEntryLeagueEventSelectByTournament(int event, int tournamentId) {
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return;
        }
        String leagueType = "Tournament";
        String leagueName = tournamentInfoEntity.getName();
        List<Integer> entryList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                        .eq(TournamentEntryEntity::getTournamentId, tournamentId))
                .stream()
                .map(TournamentEntryEntity::getEntry)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryList)) {
            return;
        }
        List<EntryInfoData> entryInfoDataList = Lists.newArrayList();
        this.entryInfoService.list(new QueryWrapper<EntryInfoEntity>().lambda()
                        .in(EntryInfoEntity::getEntry, entryList))
                .forEach(o -> entryInfoDataList.add(BeanUtil.copyProperties(o, EntryInfoData.class)));
        List<CompletableFuture<LeagueEventReportEntity>> future = entryInfoDataList
                .stream()
                .map(o ->
                        CompletableFuture.supplyAsync(() ->
                                this.initEntryEventSelectStat(event, o.getEntry(), tournamentId, leagueType, leagueName)))
                .collect(Collectors.toList());
        List<LeagueEventReportEntity> leagueEventStatEntityList = future
                .stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        // save
        this.leagueEventReportService.saveBatch(leagueEventStatEntityList);
        log.info("tournamentId:{}, event:{}, insert league_event_report size:{}!", tournamentId, event, leagueEventStatEntityList.size());
    }

    private LeagueInfoData getLeagueDataByTypeAndId(int leagueId, String leagueType, int limit) {
        if (LeagueType.valueOf(leagueType).equals(LeagueType.Classic)) {
            return this.staticService.getEntryInfoListFromClassicByLimit(leagueId, limit);
        } else if (LeagueType.valueOf(leagueType).equals(LeagueType.H2h)) {
            return this.staticService.getEntryInfoListFromH2hByLimit(leagueId, limit);
        }
        return null;
    }

    private LeagueEventReportEntity initEntryEventSelectStat(int event, int entry, int leagueId, String leagueType, String leagueName) {
        UserPicksRes userPicksRes = this.staticService.getUserPicks(event, entry).orElse(null);
        if (userPicksRes == null) {
            return null;
        }
        List<Pick> picks = userPicksRes.getPicks();
        LeagueEventReportEntity leagueEventReportEntity = new LeagueEventReportEntity()
                .setLeagueId(leagueId)
                .setLeagueType(leagueType)
                .setLeagueName(leagueName)
                .setEntry(entry)
                .setEntryName("")
                .setPlayerName("")
                .setOverallPoints(0)
                .setOverallRank(0)
                .setTeamValue(0)
                .setBank(0)
                .setEvent(event)
                .setEventPoints(0)
                .setEventTransfers(0)
                .setEventTransfersCost(0)
                .setEventNetPoints(0)
                .setEventBenchPoints(0)
                .setEventAutoSubPoints(0)
                .setEventRank(0)
                .setEventChip("")
                .setPosition1(picks.get(0).getElement())
                .setPosition2(picks.get(1).getElement())
                .setPosition3(picks.get(2).getElement())
                .setPosition4(picks.get(3).getElement())
                .setPosition5(picks.get(4).getElement())
                .setPosition6(picks.get(5).getElement())
                .setPosition7(picks.get(6).getElement())
                .setPosition8(picks.get(7).getElement())
                .setPosition9(picks.get(8).getElement())
                .setPosition10(picks.get(9).getElement())
                .setPosition11(picks.get(10).getElement())
                .setPosition12(picks.get(11).getElement())
                .setPosition13(picks.get(12).getElement())
                .setPosition14(picks.get(13).getElement())
                .setPosition15(picks.get(14).getElement());
        // captain
        leagueEventReportEntity
                .setCaptain(picks
                        .stream()
                        .filter(Pick::isCaptain)
                        .map(Pick::getElement)
                        .findFirst()
                        .orElse(0)
                )
                .setCaptainPoints(0)
                .setCaptainBlank(true)
                .setCaptainSelected("");
        // vice captain
        leagueEventReportEntity
                .setViceCaptain(picks
                        .stream()
                        .filter(Pick::isViceCaptain)
                        .map(Pick::getElement)
                        .findFirst()
                        .orElse(0)
                )
                .setViceCaptainPoints(0)
                .setViceCaptainBlank(true)
                .setViceCaptainSelected("");
        // highest score
        leagueEventReportEntity
                .setHighestScore(0)
                .setHighestScorePoints(0)
                .setHighestScoreBlank(true)
                .setHighestScoreSelected("");
        // played captain
        leagueEventReportEntity.setPlayedCaptain(0);
        return leagueEventReportEntity;
    }

    @Override
    public void updateEntryLeagueEventResult(int event, int leagueId, String leagueType, int entry) {
        // league_event_stat
        LeagueEventReportEntity leagueEventReportEntity = this.leagueEventReportService.getOne(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .eq(LeagueEventReportEntity::getEvent, event)
                .eq(LeagueEventReportEntity::getEntry, entry));
        if (leagueEventReportEntity == null) {
            return;
        }
        // prepare
        Map<Integer, PlayerStatEntity> playerStatMap = Maps.newHashMap();
        this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
                        .eq(PlayerStatEntity::getEvent, event))
                .forEach(o -> {
                    int element = o.getElement();
                    if (playerStatMap.containsKey(element)) {
                        PlayerStatEntity playerStatEntity = playerStatMap.get(element);
                        if (LocalDateTime.parse(playerStatEntity.getUpdateTime().replace(" ", "T"))
                                .isAfter(LocalDateTime.parse(o.getUpdateTime().replace(" ", "T")))) {
                            playerStatMap.put(element, playerStatEntity);
                        }
                    } else {
                        playerStatMap.put(element, o);
                    }
                });
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                        .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                        .eq(EntryEventResultEntity::getEvent, event)
                        .eq(EntryEventResultEntity::getEntry, entry))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        // collect
        LeagueEventReportEntity data = this.updateEntryEventResultStat(event, leagueEventReportEntity, playerStatMap, eventLiveMap, entryEventResultMap);
        // update
        this.leagueEventReportService.updateById(data);
        log.info("leagueId:{}, leagueType:{}, event:{}, entry:{}, update league_event_report!", leagueId, leagueType, event, entry);
    }

    @Override
    public void updateLeagueEventResult(int event, int leagueId, String leagueType) {
        // league_event_stat
        List<LeagueEventReportEntity> leagueEventStatList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .eq(LeagueEventReportEntity::getEvent, event));
        if (CollectionUtils.isEmpty(leagueEventStatList)) {
            return;
        }
        List<Integer> entryList = leagueEventStatList
                .stream()
                .map(LeagueEventReportEntity::getEntry)
                .collect(Collectors.toList());
        // prepare
        Map<Integer, PlayerStatEntity> playerStatMap = Maps.newHashMap();
        this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
                        .eq(PlayerStatEntity::getEvent, event))
                .forEach(o -> {
                    int element = o.getElement();
                    if (playerStatMap.containsKey(element)) {
                        PlayerStatEntity playerStatEntity = playerStatMap.get(element);
                        if (LocalDateTime.parse(playerStatEntity.getUpdateTime().replace(" ", "T"))
                                .isAfter(LocalDateTime.parse(o.getUpdateTime().replace(" ", "T")))) {
                            playerStatMap.put(element, playerStatEntity);
                        }
                    } else {
                        playerStatMap.put(element, o);
                    }
                });
        if (CollectionUtils.isEmpty(playerStatMap)) {
            return;
        }
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                        .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        if (CollectionUtils.isEmpty(eventLiveMap)) {
            this.redisCacheService.insertEventLive(event);
            this.updateLeagueEventResult(event, leagueId, leagueType);
        }
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                        .eq(EntryEventResultEntity::getEvent, event)
                        .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        if (CollectionUtils.isEmpty(entryEventResultMap)) {
            return;
        }
        // collect
        List<CompletableFuture<LeagueEventReportEntity>> future = leagueEventStatList
                .stream()
                .map(o ->
                        CompletableFuture.supplyAsync(() ->
                                this.updateEntryEventResultStat(event, o, playerStatMap, eventLiveMap, entryEventResultMap)))
                .collect(Collectors.toList());
        List<LeagueEventReportEntity> leagueEventStatEntityList = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        // update
        this.leagueEventReportService.updateBatchById(leagueEventStatEntityList);
        log.info("leagueId:{}, leagueType:{}, event:{}, update league_event_report size:{}!", leagueId, leagueType, event, leagueEventStatEntityList.size());
    }

    private LeagueEventReportEntity updateEntryEventResultStat(int event, LeagueEventReportEntity leagueEventStatEntity, Map<Integer, PlayerStatEntity> playerStatMap, Map<Integer, EventLiveEntity> eventLiveMap, Map<Integer, EntryEventResultEntity> entryEventResultMap) {
        int entry = leagueEventStatEntity.getEntry();
        // entry_info
        EntryInfoEntity entryInfoEntity = this.queryService.qryEntryInfo(entry);
        if (entryInfoEntity != null) {
            leagueEventStatEntity
                    .setEntryName(entryInfoEntity.getEntryName())
                    .setPlayerName(entryInfoEntity.getPlayerName());
        }
        // entry_event_result
        if (entryEventResultMap.containsKey(entry)) {
            EntryEventResultEntity entryEventResultEntity = entryEventResultMap.get(entry);
            leagueEventStatEntity
                    .setEventPoints(entryEventResultEntity.getEventPoints())
                    .setEventTransfers(entryEventResultEntity.getEventTransfers())
                    .setEventTransfersCost(entryEventResultEntity.getEventTransfersCost())
                    .setEventNetPoints(entryEventResultEntity.getEventNetPoints())
                    .setEventBenchPoints(entryEventResultEntity.getEventBenchPoints())
                    .setEventAutoSubPoints(entryEventResultEntity.getEventAutoSubPoints())
                    .setEventRank(entryEventResultEntity.getEventRank())
                    .setEventChip(entryEventResultEntity.getEventChip())
                    .setOverallPoints(entryEventResultEntity.getOverallPoints())
                    .setOverallRank(entryEventResultEntity.getOverallRank())
                    .setTeamValue(entryEventResultEntity.getTeamValue())
                    .setBank(entryEventResultEntity.getBank());
        } else {
            this.staticService.getUserPicks(event, entry)
                    .ifPresent(userPick ->
                            leagueEventStatEntity
                                    .setEventPoints(userPick.getEntryHistory().getPoints())
                                    .setEventTransfers(userPick.getEntryHistory().getEventTransfers())
                                    .setEventTransfersCost(userPick.getEntryHistory().getEventTransfersCost())
                                    .setEventNetPoints(userPick.getEntryHistory().getPoints() - userPick.getEntryHistory().getEventTransfersCost())
                                    .setEventBenchPoints(userPick.getEntryHistory().getPointsOnBench())
                                    .setEventAutoSubPoints(userPick.getAutomaticSubs().size() == 0 ? 0 : this.calcAutoSubPoints(userPick.getAutomaticSubs(), eventLiveMap))
                                    .setEventRank(userPick.getEntryHistory().getRank())
                                    .setEventChip(StringUtils.isBlank(userPick.getActiveChip()) ? Chip.NONE.getValue() : userPick.getActiveChip())
                                    .setOverallPoints(userPick.getEntryHistory().getTotalPoints())
                                    .setOverallRank(userPick.getEntryHistory().getOverallRank())
                                    .setTeamValue(userPick.getEntryHistory().getValue())
                                    .setBank(userPick.getEntryHistory().getBank())
                    );
        }
        // captain
        int captain = leagueEventStatEntity.getCaptain();
        EventLiveEntity captainEventLiveEntity;
        if (eventLiveMap.containsKey(captain)) {
            captainEventLiveEntity = eventLiveMap.get(captain);
            leagueEventStatEntity
                    .setCaptainPoints(captainEventLiveEntity.getTotalPoints())
                    .setCaptainBlank(this.setElementBlank(captainEventLiveEntity))
                    .setCaptainSelected(playerStatMap.containsKey(captain) ? playerStatMap.get(captain).getSelectedByPercent() + "%" : "");
        } else {
            captainEventLiveEntity = new EventLiveEntity()
                    .setMinutes(0)
                    .setTotalPoints(0);
        }
        // vice captain
        int viceCaptain = leagueEventStatEntity.getViceCaptain();
        EventLiveEntity viceCaptainEventLiveEntity;
        if (eventLiveMap.containsKey(viceCaptain)) {
            viceCaptainEventLiveEntity = eventLiveMap.get(viceCaptain);
            leagueEventStatEntity
                    .setViceCaptainPoints(viceCaptainEventLiveEntity.getTotalPoints())
                    .setViceCaptainBlank(this.setElementBlank(viceCaptainEventLiveEntity))
                    .setViceCaptainSelected(playerStatMap.containsKey(viceCaptain) ? playerStatMap.get(viceCaptain).getSelectedByPercent() + "%" : "");
        } else {
            viceCaptainEventLiveEntity = new EventLiveEntity()
                    .setMinutes(0)
                    .setTotalPoints(0);
        }
        leagueEventStatEntity.setPlayedCaptain(this.selectPlayedCaptain(captainEventLiveEntity, viceCaptainEventLiveEntity));
        // highest score
        int highestElement = this.getHighestScoreElement(leagueEventStatEntity, eventLiveMap);
        if (eventLiveMap.containsKey(highestElement)) {
            EventLiveEntity highestEventLiveEntity = eventLiveMap.get(highestElement);
            leagueEventStatEntity
                    .setHighestScore(highestElement)
                    .setHighestScorePoints(highestEventLiveEntity.getTotalPoints())
                    .setHighestScoreBlank(this.setElementBlank(highestEventLiveEntity))
                    .setHighestScoreSelected(playerStatMap.containsKey(highestElement) ? playerStatMap.get(highestElement).getSelectedByPercent() + "%" : "");
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

    private int calcAutoSubPoints(List<AutoSubs> automaticSubs, Map<Integer, EventLiveEntity> eventLiveMap) {
        return automaticSubs
                .stream()
                .mapToInt(o -> eventLiveMap.containsKey(o.getElementIn()) ? eventLiveMap.get(o.getElementIn()).getTotalPoints() : 0)
                .sum();
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
        return elementPointsMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
    }

    private int getElementEventPoints(int element, Map<Integer, EventLiveEntity> eventLiveMap) {
        if (eventLiveMap.containsKey(element)) {
            return eventLiveMap.get(element).getTotalPoints();
        }
        return 0;
    }

    private boolean setElementBlank(EventLiveEntity eventLiveEntity) {
        return eventLiveEntity.getGoalsScored() <= 0 &&
                eventLiveEntity.getAssists() <= 0 &&
                eventLiveEntity.getBonus() <= 0 &&
                eventLiveEntity.getPenaltiesSaved() <= 0 &&
                eventLiveEntity.getSaves() <= 3 &&
                ((eventLiveEntity.getElementType() != 1 && eventLiveEntity.getElementType() != 2) || eventLiveEntity.getCleanSheets() <= 0);
    }

    @Override
    public Map<String, Object> calcEventStat(int event, int leagueId, String leagueType, int topNum) {
        Map<String, Object> map = Maps.newHashMap();
        List<LeagueEventReportEntity> list = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getEvent, event)
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType));
        // 平均分
        double average = list
                .stream()
                .mapToInt(LeagueEventReportEntity::getEventPoints)
                .average()
                .orElse(0);
        map.put("average", average);
        // top 平均分
        double eliteAverage = list
                .stream()
                .sorted(Comparator.comparingInt(LeagueEventReportEntity::getOverallPoints).reversed())
                .limit(topNum)
                .mapToInt(LeagueEventReportEntity::getEventPoints)
                .average()
                .orElse(0);
        map.put("eliteAverage", eliteAverage);
        // 破百
        long above100Num = list
                .stream()
                .filter(o -> o.getEventPoints() >= 100)
                .count();
        map.put("above100Num", above100Num);
        // max
        LeagueEventReportEntity maxEntity = list
                .stream()
                .max(Comparator.comparingInt(LeagueEventReportEntity::getEventPoints))
                .orElse(new LeagueEventReportEntity());
        map.put("max", maxEntity.getEventPoints());
        map.put("maxEntry", maxEntity.getEntry());
        // min
        LeagueEventReportEntity minEntity = list
                .stream()
                .min(Comparator.comparingInt(LeagueEventReportEntity::getEventPoints))
                .orElse(new LeagueEventReportEntity());
        map.put("min", minEntity.getEventPoints());
        map.put("minEntry", minEntity.getEntry());
        return map;
    }

}

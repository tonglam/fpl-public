package com.tong.fpl.service.impl;

import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.LeagueEventReportEntity;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.service.IReportService;
import com.tong.fpl.service.IStaticSerive;
import com.tong.fpl.service.db.EntryEventResultService;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.service.db.EventLiveService;
import com.tong.fpl.service.db.LeagueEventReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.RoundingMode;
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

    private final IStaticSerive staticSerive;
    private final EntryInfoService entryInfoService;
    private final EventLiveService eventLiveService;
    private final EntryEventResultService entryEventResultService;
    private final LeagueEventReportService leagueEventReportService;

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
        log.info("insert league_event_stat size:{}!", leagueEventStatEntityList.size());
    }

    private LeagueInfoData getLeagueDataByTypeAndId(int leagueId, String leagueType, int limit) {
        if (LeagueType.valueOf(leagueType).equals(LeagueType.Classic)) {
            return this.staticSerive.getEntryInfoListFromClassicByLimit(leagueId, limit);
        } else if (LeagueType.valueOf(leagueType).equals(LeagueType.H2h)) {
            return this.staticSerive.getEntryInfoListFromH2hByLimit(leagueId, limit);
        }
        return null;
    }

    private LeagueEventReportEntity initEntryEventSelectStat(int event, int entry, int leagueId, String leagueType, String leagueName) {
        UserPicksRes userPicksRes = this.staticSerive.getUserPicks(event, entry).orElse(null);
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
                .setCaptainBlank(true);
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
                .setViceCaptainBlank(true);
        // highest score
        leagueEventReportEntity
                .setHighestScore(0)
                .setHighestScorePoints(0)
                .setHighestScoreBlank(true);
        return leagueEventReportEntity;
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
        // preapre
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        Map<Integer, EntryEventResultEntity> entryEventResultMap = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .in(EntryEventResultEntity::getEntry, entryList))
                .stream()
                .collect(Collectors.toMap(EntryEventResultEntity::getEntry, o -> o));
        // collect
        List<CompletableFuture<LeagueEventReportEntity>> future = leagueEventStatList
                .stream()
                .map(o ->
                        CompletableFuture.supplyAsync(() ->
                                this.updateEntryEventResultStat(event, o, eventLiveMap, entryEventResultMap)))
                .collect(Collectors.toList());
        List<LeagueEventReportEntity> leagueEventStatEntityList = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        // update
        this.leagueEventReportService.updateBatchById(leagueEventStatEntityList);
        log.info("update league_event_stat size:{}!", leagueEventStatEntityList.size());
    }

    private LeagueEventReportEntity updateEntryEventResultStat(int event, LeagueEventReportEntity leagueEventStatEntity, Map<Integer, EventLiveEntity> eventLiveMap, Map<Integer, EntryEventResultEntity> entryEventResultMap) {
        int entry = leagueEventStatEntity.getEntry();
        // entry_info
        EntryInfoEntity entryInfoEntity = this.qryEntryInfo(entry);
        if (entryInfoEntity != null) {
            leagueEventStatEntity
                    .setEntryName(entryInfoEntity.getEntryName())
                    .setPlayerName(entryInfoEntity.getPlayerName())
                    .setOverallPoints(entryInfoEntity.getOverallPoints())
                    .setOverallRank(entryInfoEntity.getOverallRank())
                    .setTeamValue(entryInfoEntity.getTeamValue())
                    .setBank(entryInfoEntity.getBank());
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
                    .setEventRank(entryEventResultEntity.getEventRank())
                    .setEventChip(entryEventResultEntity.getEventChip());
        } else {
            this.staticSerive.getUserPicks(event, entry)
                    .ifPresent(userPick -> leagueEventStatEntity
                            .setEventPoints(userPick.getEntryHistory().getPoints())
                            .setEventTransfers(userPick.getEntryHistory().getEventTransfers())
                            .setEventTransfersCost(userPick.getEntryHistory().getEventTransfersCost())
                            .setEventNetPoints(userPick.getEntryHistory().getPoints() - userPick.getEntryHistory().getEventTransfersCost())
                            .setEventBenchPoints(userPick.getEntryHistory().getPointsOnBench())
                            .setEventRank(userPick.getEntryHistory().getRank())
                            .setOverallRank(userPick.getEntryHistory().getOverallRank())
                            .setEventChip(StringUtils.isBlank(userPick.getActiveChip()) ? Chip.NONE.getValue() : userPick.getActiveChip()));
        }
        // captain
        int captain = leagueEventStatEntity.getCaptain();
        if (eventLiveMap.containsKey(captain)) {
            EventLiveEntity captainEventLiveEntity = eventLiveMap.get(captain);
            leagueEventStatEntity
                    .setCaptainPoints(captainEventLiveEntity.getTotalPoints())
                    .setCaptainBlank(this.setElementBlank(captainEventLiveEntity));
        }
        // vice captain
        int viceCaptain = leagueEventStatEntity.getViceCaptain();
        if (eventLiveMap.containsKey(viceCaptain)) {
            EventLiveEntity viceCaptainEventLiveEntity = eventLiveMap.get(viceCaptain);
            leagueEventStatEntity
                    .setViceCaptainPoints(viceCaptainEventLiveEntity.getTotalPoints())
                    .setViceCaptainBlank(this.setElementBlank(viceCaptainEventLiveEntity));
        }
        // highest score
        int highestElement = this.getHighestScoreElement(leagueEventStatEntity, eventLiveMap);
        if (eventLiveMap.containsKey(highestElement)) {
            EventLiveEntity highestEventLiveEntity = eventLiveMap.get(highestElement);
            leagueEventStatEntity
                    .setHighestScore(highestElement)
                    .setHighestScorePoints(highestEventLiveEntity.getTotalPoints())
                    .setHighestScoreBlank(this.setElementBlank(highestEventLiveEntity));
        }
        return leagueEventStatEntity;
    }

    private EntryInfoEntity qryEntryInfo(int entry) {
        if (entry <= 0) {
            return new EntryInfoEntity();
        }
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        if (entryInfoEntity != null) {
            return entryInfoEntity;
        }
        EntryRes entryRes = this.staticSerive.getEntry(entry).orElse(null);
        if (entryRes == null) {
            return new EntryInfoEntity();
        }
        return new EntryInfoEntity()
                .setEntry(entryRes.getId())
                .setEntryName(entryRes.getName())
                .setPlayerName(entryRes.getPlayerFirstName() + " " + entryRes.getPlayerLastName())
                .setRegion(entryRes.getPlayerRegionName())
                .setStartedEvent(entryRes.getStartedEvent())
                .setOverallPoints(entryRes.getSummaryOverallPoints())
                .setOverallRank(entryRes.getSummaryOverallRank())
                .setBank(entryRes.getLastDeadlineBank())
                .setTeamValue(entryRes.getLastDeadlineValue())
                .setTotalTransfers(entryRes.getLastDeadlineTotalTransfers());
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

}

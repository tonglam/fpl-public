package com.tong.fpl.task;

import com.tong.fpl.log.TaskLog;
import com.tong.fpl.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Create by tong on 2020/7/21
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MatchDayTask {

    private final IQueryService queryService;
    private final IRedisCacheService redisCacheService;
    private final ITournamentService tournamentService;
    private final IEventDataService eventDataService;
    private final IGroupService scoutService;

    @Scheduled(cron = "0 */1 0-7,19-23 * * *")
    public void insertEventLiveCache() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDayTime(event)) {
            return;
        }
        TaskLog.info("start true insertEventLiveCache task");
        this.redisCacheService.insertEventLiveCache(event);
        this.redisCacheService.insertSingleEventFixtureCache(event);
        this.redisCacheService.insertLiveFixtureCache();
        this.redisCacheService.insertLiveBonusCache();
    }

    @Scheduled(cron = "0 30 6,9 * * *")
    public void insertEventLive() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        TaskLog.info("start true insertEventLive task");
        this.redisCacheService.insertSingleEventFixture(event);
        this.redisCacheService.insertLiveFixtureCache();
        this.redisCacheService.insertLiveBonusCache();
        this.redisCacheService.insertEventLive(event);
    }

//    @Scheduled(cron = "0 0/5 0-4,18-23 * * *")
//    public void insertEntryEventPicks() {
//        int event = this.queryService.getCurrentEvent();
//        if (!this.queryService.isSelectTime(event)) {
//            return;
//        }
//        this.queryService.qryAllTournamentList()
//                .stream()
//                .map(TournamentInfoEntity::getId)
//                .forEach(tournamentId -> this.eventDataService.insertTournamentEntryEventPick(event, tournamentId));
//    }

//    @Scheduled(cron = "0 0/5 0-4,18-23 * * *")
//    public void insertEntryEventTransfers() {
//        int event = this.queryService.getCurrentEvent();
//        if (!this.queryService.isSelectTime(event)) {
//            return;
//        }
//        this.queryService.qryAllTournamentList()
//                .stream()
//                .map(TournamentInfoEntity::getId)
//                .forEach(this.eventDataService::insertTournamentEntryEventTransfers);
//    }

//    @Scheduled(cron = "0 0 8,11 * * *")
//    public void updateTournamentResult() {
//        int event = this.queryService.getCurrentEvent();
//        if (!this.queryService.isMatchDay(event)) {
//            return;
//        }
//        this.redisCacheService.insertEventLive(event);
//        this.queryService.qryAllTournamentList()
//                .stream()
//                .map(TournamentInfoEntity::getId)
//                .forEach(tournamentId -> {
//                    this.updateSingleTournamentResult(event, tournamentId);
//                    this.eventDataService.updateEntryEventTransfers(event, tournamentId);
//                });
//    }
//
//    private void updateSingleTournamentResult(int event, int tournamentId) {
//        try {
//            TaskLog.info("start add tournament new entry, event:{}, tournament:{}", event, tournamentId);
//            this.tournamentService.addTournamentNewEntry(tournamentId);
//            TaskLog.info("start update tournament result, event:{}, tournament:{}", event, tournamentId);
//            this.eventDataService.upsertTournamentEntryEventResult(event, tournamentId);
//            TaskLog.info("end update tournament result, event:{}, tournament:{}", event, tournamentId);
//        } catch (Exception e) {
//            e.printStackTrace();
//            TaskLog.error("update tournament result error:{}, event:{}, tournament:{}", e.getMessage(), event, tournamentId);
//        }
//    }

//    @Scheduled(cron = "0 5 8,11 * * *")
//    public void updatePointsRaceGroupResult() {
//        int event = this.queryService.getCurrentEvent();
//        if (!this.queryService.isMatchDay(event)) {
//            return;
//        }
//        this.queryService.qryAllTournamentList()
//                .stream()
//                .filter(o -> StringUtils.equals(o.getTournamentMode(), TournamentMode.Normal.name()))
//                .filter(o -> StringUtils.equals(o.getGroupMode(), GroupMode.Points_race.name()))
//                .filter(o -> o.getGroupStartGw() <= event && o.getGroupEndGw() >= event)
//                .map(TournamentInfoEntity::getId)
//                .forEach(tournamentId -> this.updateSinglePointsRaceGroupResult(event, tournamentId));
//    }
//
//    private void updateSinglePointsRaceGroupResult(int event, int tournamentId) {
//        try {
//            TaskLog.info("start update points_race group result, event:{}, tournament:{}", event, tournamentId);
//            this.eventDataService.updatePointsRaceGroupResult(event, tournamentId);
//            TaskLog.info("end update points_race group result, event:{}, tournament:{}", event, tournamentId);
//        } catch (Exception e) {
//            e.printStackTrace();
//            TaskLog.error("update points_race group result error:{}, event:{}, tournament:{}", e.getMessage(), event, tournamentId);
//        }
//    }

//    @Scheduled(cron = "0 10 8,11 * * *")
//    public void updateBattleRaceGroupResult() {
//        int event = this.queryService.getCurrentEvent();
//        if (!this.queryService.isMatchDay(event)) {
//            return;
//        }
//        this.queryService.qryAllTournamentList()
//                .stream()
//                .filter(o -> StringUtils.equals(o.getTournamentMode(), TournamentMode.Normal.name()))
//                .filter(o -> StringUtils.equals(o.getGroupMode(), GroupMode.Battle_race.name()))
//                .filter(o -> o.getGroupStartGw() <= event && o.getGroupEndGw() >= event)
//                .map(TournamentInfoEntity::getId)
//                .forEach(tournamentId -> this.updateSingleBattleRaceGroupResult(event, tournamentId));
//    }
//
//    private void updateSingleBattleRaceGroupResult(int event, int tournamentId) {
//        try {
//            TaskLog.info("start update battle_race group result, event:{}, tournament:{}", event, tournamentId);
//            this.eventDataService.updateBattleRaceGroupResult(event, tournamentId);
//            TaskLog.info("end update battle_race group result, event:{}, tournament:{}", event, tournamentId);
//        } catch (Exception e) {
//            e.printStackTrace();
//            TaskLog.error("update battle_race group result error:{}, event:{}, tournament:{}", e.getMessage(), event, tournamentId);
//        }
//    }

//    @Scheduled(cron = "0 15 8,11 * * *")
//    public void updateKnockoutResult() {
//        int event = this.queryService.getCurrentEvent();
//        if (!this.queryService.isMatchDay(event)) {
//            return;
//        }
//        this.queryService.qryAllTournamentList()
//                .stream()
//                .filter(o -> StringUtils.equals(o.getTournamentMode(), TournamentMode.Normal.name()))
//                .filter(o -> !StringUtils.equals(o.getKnockoutMode(), KnockoutMode.No_knockout.name()))
//                .filter(o -> o.getKnockoutStartGw() <= event && o.getKnockoutEndGw() >= event)
//                .map(TournamentInfoEntity::getId)
//                .forEach(tournamentId -> this.updateSingleKnockoutResult(event, tournamentId));
//    }
//
//    private void updateSingleKnockoutResult(int event, int tournamentId) {
//        try {
//            TaskLog.info("start update knockout result, event:{}, tournament:{}", event, tournamentId);
//            this.eventDataService.updateKnockoutResult(event, tournamentId);
//            TaskLog.info("end update knockout result, event:{}, tournament:{}", event, tournamentId);
//        } catch (Exception e) {
//            e.printStackTrace();
//            TaskLog.error("update knockout result error:{}, event:{}, tournament:{}", e.getMessage(), event, tournamentId);
//        }
//    }

    @Scheduled(cron = "0 20 8,11 * * *")
    public void updateScoutResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        this.scoutService.updateEventScoutResult(event);
    }

//    @Scheduled(cron = "0 40 9,11 * * *")
//    public void updateTournamentEventTransfersPlayed() {
//        int event = this.queryService.getCurrentEvent();
//        if (!this.queryService.isMatchDay(event)) {
//            return;
//        }
//        this.queryService.qryAllTournamentList()
//                .stream()
//                .map(TournamentInfoEntity::getId)
//                .forEach(tournamentId -> this.eventDataService.updateTournamentEventTransfers(event, tournamentId));
//    }

}

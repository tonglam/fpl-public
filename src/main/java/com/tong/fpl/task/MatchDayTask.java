package com.tong.fpl.task;

import com.tong.fpl.log.TaskLog;
import com.tong.fpl.service.IEventDataService;
import com.tong.fpl.service.IGroupService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.IRedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Create by tong on 2020/7/21
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MatchDayTask {

    private final IQueryService queryService;
    private final IRedisCacheService redisCacheService;
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
        this.eventDataService.updateEventLiveData(event);
    }

    @Scheduled(cron = "0 35 6,9 * * *")
    public void insertEventLiveSummary() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        TaskLog.info("start true insertEventLiveSummary task");
        this.redisCacheService.insertEventLiveSummary();
    }

    @Scheduled(cron = "0 0/5 0-4,18-23 * * *")
    public void insertEntryEventPicks() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isSelectTime(event)) {
            return;
        }
        List<Integer> entryList = this.queryService.qryActiveTournamentEntryList();
        this.eventDataService.insertEventPickByEntryList(event, entryList);
    }

    @Scheduled(cron = "0 0/5 0-4,18-23 * * *")
    public void insertEntryEventTransfers() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isSelectTime(event)) {
            return;
        }
        List<Integer> entryList = this.queryService.qryActiveTournamentEntryList();
        this.eventDataService.insertEventTransfersByEntryList(entryList);
    }

    @Scheduled(cron = "0 0 8,11 * * *")
    public void updateTournamentResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        this.redisCacheService.insertEventLive(event);
        List<Integer> entryList = this.queryService.qryActiveTournamentEntryList();
        this.eventDataService.upsertEventResultByEntryList(event, entryList);
    }

    @Scheduled(cron = "0 5 8,11 * * *")
    public void updatePointsRaceGroupResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        this.queryService.qryActiveTournamentEntryList()
                .forEach(tournamentId -> this.eventDataService.updatePointsRaceGroupResult(event, tournamentId));
    }

    @Scheduled(cron = "0 10 8,11 * * *")
    public void updateBattleRaceGroupResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        this.queryService.qryActiveTournamentEntryList()
                .forEach(tournamentId -> this.eventDataService.updateBattleRaceGroupResult(event, tournamentId));
    }

    @Scheduled(cron = "0 15 8,11 * * *")
    public void updateKnockoutResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        this.queryService.qryActiveTournamentEntryList()
                .forEach(tournamentId -> this.eventDataService.updateKnockoutResult(event, tournamentId));
    }

    @Scheduled(cron = "0 20 8,11 * * *")
    public void updateScoutResult() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        this.scoutService.updateEventScoutResult(event);
    }

    @Scheduled(cron = "0 40 9,11 * * *")
    public void updateTournamentEventTransfersPlayed() {
        int event = this.queryService.getCurrentEvent();
        if (!this.queryService.isMatchDay(event)) {
            return;
        }
        List<Integer> entryList = this.queryService.qryActiveTournamentEntryList();
        this.eventDataService.updateEventTransfersByEntryList(event, entryList);
    }

}

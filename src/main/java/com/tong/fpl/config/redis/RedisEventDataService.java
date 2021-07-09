package com.tong.fpl.config.redis;

import com.tong.fpl.service.IEventDataService;
import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2021/7/9
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RedisEventDataService {

    private final IQueryService queryService;
    private final IEventDataService eventDataService;

    public void insertEventTransfersByEntryList(String eventStr) {
        int event = this.getEvent(eventStr);
        List<Integer> entryList = this.queryService.qryActiveTournamentEntryList();
        this.eventDataService.insertEventCupResultByEntryList(event, entryList);
    }

    public void insertEventCupResultByEntryList(String eventStr) {
        int event = this.getEvent(eventStr);
        List<Integer> entryList = this.queryService.qryActiveTournamentEntryList();
        this.eventDataService.insertEventCupResultByEntryList(event, entryList);
    }

    public void updateEventLiveData(String eventStr) {
        int event = this.getEvent(eventStr);
        this.eventDataService.updateEventLiveData(event);
    }

    public void updateEventTransfersByEntryList(String eventStr) {
        int event = this.getEvent(eventStr);
        List<Integer> entryList = this.queryService.qryActiveTournamentEntryList();
        this.eventDataService.updateEventTransfersByEntryList(event, entryList);
    }

    public void upsertEventCupResultByEntryList(String eventStr) {
        int event = this.getEvent(eventStr);
        List<Integer> entryList = this.queryService.qryActiveTournamentEntryList();
        this.eventDataService.insertEventCupResultByEntryList(event, entryList);
    }

    public void updateEventPointsRaceGroupResult(String eventStr) {
        int event = this.getEvent(eventStr);
        this.queryService.qryPointsRaceGroupTournamentList(event)
                .forEach(tourmentId -> this.eventDataService.updatePointsRaceGroupResult(event, tourmentId));
    }

    public void updateEventBattleRaceGroupResult(String eventStr) {
        int event = this.getEvent(eventStr);
        this.queryService.qryBattleRaceGroupTournamentList(event)
                .forEach(tourmentId -> this.eventDataService.updatePointsRaceGroupResult(event, tourmentId));
    }

    public void updateEventKnockoutResult(String eventStr) {
        int event = this.getEvent(eventStr);
        this.queryService.qryKnockoutTournamentList(event)
                .forEach(tourmentId -> this.eventDataService.updatePointsRaceGroupResult(event, tourmentId));
    }

    private int getEvent(String eventStr) {
        return Integer.parseInt(eventStr);
    }

}

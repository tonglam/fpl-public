package com.tong.fpl.service;

import java.util.List;

/**
 * 四种更新节点：
 * 1.每日更新 => 自然日
 * 2.死线后1h更新 => event deadline
 * 3.比赛中更新 => fixture kickoff_time
 * 4.比赛日结束后更新 => event_live
 * <p>
 * Create by tong on 2020/6/29
 */
public interface IEventDataService {

    /**
     * @apiNote daily
     */
    void updateEventData();

    void updatePlayerData();

    void updateEntryInfo();

    /**
     * @apiNote after deadline
     */
    void insertEntryEventPick(int event, int entry);

    void insertEventPickByEntryList(int event, List<Integer> entryList);

    void insertEntryEventTransfers(int entry);

    void insertEventTransfersByEntryList(List<Integer> entryList);

    void insertEntryEventCupResult(int event, int entry);

    void insertEventCupResultByEntryList(int event, List<Integer> entryList);

    /**
     * @apiNote after matchDay
     */
    void updateEventLiveData(int event);

    void upsertEntryEventResult(int event, int entry);

    void upsertTournamentEntryEventResult(int event, int tournamentId);

    void upsertEventResultByEntryList(int event, List<Integer> entryList);

    void updateEntryEventTransfers(int event, int entry);

    void updateEventTransfersByEntryList(int event, List<Integer> entryList);

    void upsertEntryEventCupResult(int event, int entry);

    void upsertEventCupResultByEntryList(int event, List<Integer> entryList);

    void updatePointsRaceGroupResult(int event, int tournamentId);

    void updateBattleRaceGroupResult(int event, int tournamentId);

    void updateKnockoutResult(int event, int tournamentId);

    void updateZjPhaseOneResult(int event, int tournamentId);

    void updateZjPhaseTwoResult(int event, int tournamentId);

    void updateZjPkResult(int event, int tournamentId);

    void updateZjTournamentResult(int tournamentId);

    /**
     * @apiNote during match
     */
    void updateEventLiveCache(int event);

    /**
     * @apiNote after season
     */
    void upsertEventLiveSummary();

}

package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.global.MapData;

import java.util.List;

/**
 * Create by tong on 2021/9/30
 */
public interface IDataService {

    /**
     * @apiNote daily
     */
    void updatePlayerValue();

    void updatePlayerStat();

    /**
     * @apiNote matchDay
     */
    void updateEventLiveCache(int event);

    void updateEventLive(int event);

    void upsertEventOverallResult();

    /**
     * @apiNote entry
     */
    void upsertEntryInfo(int entry);

    void upsertEntryHistoryInfo(int entry);

    void insertEntryEventPick(int event, int entry);

    void insertEntryEventTransfers(int entry);

    void updateEntryEventTransfers(int event, int entry);

    void upsertEntryEventResult(int event, int entry);

    /**
     * @apiNote entry_list
     */
    void upsertEntryInfoByList(List<Integer> entryList);

    void upsertEntryHistoryInfoByList(List<Integer> entryList);

    void insertEventPickByEntryList(int event, List<Integer> entryList);

    void upsertEventCupResultByEntryList(int event, List<Integer> entryList);

    void upsertEventResultByEntryList(int event, List<Integer> entryList);

    /**
     * @apiNote tournament
     */
    void upsertTournamentEventResult(int event, int tournamentId);

    void updatePointsRaceGroupResult(int event, int tournamentId);

    void updateBattleRaceGroupResult(int event, int tournamentId);

    void updateKnockoutResult(int event, int tournamentId);

    /**
     * @apiNote league
     */
    void updateEntryLeagueEventResult(int event, int leagueId, int entry);

    void insertLeagueEventPick(int event, int leagueId);

    void updateLeagueEventResult(int event, int leagueId);

    /**
     * @apiNote scout
     */
    void insertEventSourceScout(int event, String source, List<MapData<Integer>> scoutDataList);

    void updateEventSourceScoutResult(int event);

}

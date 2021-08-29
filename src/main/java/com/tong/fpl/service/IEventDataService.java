package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.StaticRes;

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
    void updateEventData(StaticRes staticRes);

    void updatePlayerData(StaticRes staticRes);

    void updateEntryInfo(EntryRes entryRes);

    void updateEntryInfoByList(List<Integer> entryList);

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
     * @apiNote refresh
     */
    void refreshEventLiveCache(int event);

    void refreshPlayerValue();

    void refreshEntryInfo(int entry);

    void refreshEntryEventResult(int event, int entry);

    void refreshEntryEventTransfers(int event, int entry);

    void refreshCurrentEventScoutResult(int entry);

    void refreshTournamentEventResult(int event, int tournamentId);

    void refreshEntrySummary(int event, int entry);

    void refreshLeagueSummary(int event, String leagueName, int entry);

    void refreshLeagueSelect(int event, String leagueName);

    void refreshPlayerSummary(String season, int code);

    void refreshTeamSummary(String season, String name);

}

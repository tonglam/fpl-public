package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;

import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2020/6/29
 */
public interface IInterfaceService {

    /**
     * @apiNote fantasy
     */
    Optional<StaticRes> getBootstrapStatic();

    Optional<EntryRes> getEntry(int entry);

    Optional<UserPicksRes> getUserPicks(int event, int entry);

    Optional<LeagueClassicRes> getNewLeaguesClassic(int classicId, int page);

    Optional<LeagueH2hRes> getNewLeagueH2H(int h2hId, int page);

    List<EntryInfoData> getNewEntryInfoListFromClassic(int classicId);

    List<EntryInfoData> getNewEntryInfoListFromH2h(int h2hId);

    Optional<LeagueClassicRes> getLeaguesClassic(int classicId, int page);

    Optional<LeagueH2hRes> getLeagueH2H(int h2hId, int page);

    List<EntryInfoData> getEntryInfoListFromClassic(int classicId);

    List<EntryInfoData> getEntryInfoListFromH2h(int h2hId);

    LeagueInfoData getEntryInfoListFromClassicByLimit(int classicId, int limit);

    LeagueInfoData getEntryInfoListFromH2hByLimit(int h2hId, int limit);

    Optional<EventLiveRes> getEventLive(int event);

    Optional<List<EventFixturesRes>> getEventFixture(int event);

    Optional<List<UserTransfersRes>> getUserTransfers(int entry);

    /**
     * @apiNote fpl-data
     */
    void refreshEvent();

    void refreshPlayerStat();

    void refreshPlayerValue();

    void refreshEventLive(int event);

    void refreshEventLiveCache(int event);

    void refreshEntryInfo(int entry);

    void refreshEntryInfoList(List<Integer> entryList);

    void refreshEntryHistoryInfo(int entry);

    void refreshEntryHistoryInfoList(List<Integer> entryList);

    void refreshEntryEventPick(int event, int entry);

    void refreshEntryEventPickList(int event, List<Integer> entryList);

    void insertEntryEventTransfers(int entry);

    void insertEntryEventTransfersList(int event, List<Integer> entryList);

    void updateEntryEventTransfers(int event, int entry);

    void updateEntryEventTransfersList(int event, List<Integer> entryList);

    void refreshEntryEventCupResult(int event, int entry);

    void refreshEntryEventCupResultList(int event, List<Integer> entryList);

    void refreshEntryEventResult(int event, int entry);

    void refreshEntryEventResultList(int event, List<Integer> entryList);

    void refreshTournamentEventResult(int event, int tournamentId);

    void refreshPointsRaceGroupResult(int event, int tournamentId);

    void refreshBattleRaceGroupResult(int event, int tournamentId);

    void refreshKnockoutResult(int event, int tournamentId);

    void insertEntryLeagueEventPick(int event, int tournamentId, int entry);

    void updateEntryLeagueEventResult(int event, int tournamentId, int entry);

    void insertLeagueEventPick(int event, int tournamentId);

    void updateLeagueEventResult(int event, int tournamentId);

}

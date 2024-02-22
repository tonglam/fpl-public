package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.live.*;

import java.util.List;

/**
 * Create by tong on 2020/7/13
 */
public interface ILiveService {

    /**
     * calculate entry live points
     */
    LiveCalcData calcLivePointsByEntry(int event, int entry);

    /**
     * calculate entry_list live points
     */
    List<LiveCalcData> calcLivePointsByEntryList(int event, List<Integer> entryList);

    /**
     * calculate entry live points(do not have entry)
     */
    LiveCalcData calcLivePointsByElementList(LiveCalcParamData liveCalcParamData);

    /**
     * calculate entry live points in the tournament
     */
    LiveTournamentCalcData calcLivePointsByTournament(int event, int tournamentId);

    /**
     * calculate entry live points in the tournament
     */
    SearchLiveTournamentCalcData calcSearchLivePointsByTournament(LiveCalcSearchParamData liveCalcSearchParamData);

    /**
     * calculate entry live points in the knockout tournament
     */
    List<LiveKnockoutResultData> calcLivePointsByKnockout(int event, int tournamentId);

    /**
     * calculate element live points
     */
    LiveCalcElementData calcLivePointsByElement(int event, int element);

    /**
     * calculate champion_league live gw points
     */
    List<LiveCalcData> calcLiveGwPointsByChampionLeague(int event, int tournamentId, String stage);

    /**
     * calculate champion_league live total points
     */
    List<LiveCalcData> calcLiveTotalPointsByChampionLeague(int event, int tournamentId, String stage);

}

package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.live.*;

import java.util.List;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiLive {

    /**
     * 获取球员实时得分
     */
    LiveCalcElementData calcLivePointsByElement(int event, int element);

    /**
     * 获取个人实时得分
     */
    LiveCalcData calcLivePointsByEntry(int event, int entry);

    /**
     * 获取entry列表实时得分
     */
    List<LiveCalcData> calcLivePointsByEntries(int event, String entries);

    /**
     * 获取球员列表实时得分
     */
    LiveCalcData calcLivePointsByElementList(LiveCalcParamData liveCalcParamData);

    /**
     * 获取实时联赛得分
     */
    LiveTournamentCalcData calcLivePointsByTournament(int event, int tournamentId);

    /**
     * 获取搜索球员后的实时联赛得分
     */
    SearchLiveTournamentCalcData calcSearchLivePointsByTournament(LiveCalcSearchParamData liveCalcSearchParamData);

    /**
     * 获取实时淘汰赛得分
     */
    List<LiveKnockoutResultData> calcLivePointsByKnockout(int event, int tournamentId);

    /**
     * 获取实时冠军杯周得分
     */
    List<LiveCalcData> calcLiveGwPointsByChampionLeague(int event, int tournamentId, String stage);

    /**
     * 获取实时冠军杯总得分
     */
    List<LiveCalcData> calcLiveTotalPointsByChampionLeague(int event, int tournamentId, String stage);

    /**
     * 获取实时比赛数据
     */
    List<LiveMatchData> qryLiveMatchByStatus(String playStatus);

}

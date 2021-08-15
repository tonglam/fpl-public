package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;

import java.util.List;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiLive {

    /**
     * 获取个人实时得分
     */
    LiveCalcData calcLivePointsByEntry(int event, int entry);

    /**
     * 获取实时联赛得分
     */
    List<LiveCalcData> calcLivePointsByTournament(int event, int tournamentId);

    /**
     * 获取搜索球员后的实时联赛得分
     */
    List<LiveCalcData> calcSearchLivePointsByTournament(int event, int tournamentId, int element);

    /**
     * 获取实时比赛数据
     */
    List<LiveMatchData> qryLiveMatchByStatus(String playStatus);

}

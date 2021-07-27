package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.team.TeamData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/2/26
 */
public interface IApiCommon {

    /**
     * 获取当前比赛周和下一比赛周死线
     */
    Map<String, String> qryCurrentEventAndNextUtcDeadline();

    /**
     * 刷新event_live：
     * 1.event_live cache
     * 2.event_fixture cache
     * 3.Live_fixture cache
     * 4.live_bonus cache
     */
    void insertEventLiveCache(int event);

    /**
     * 获取每周平均分
     */
    Map<String, Integer> qryEventAverageScore();

    /**
     * 获取球队缩写列表
     */
    List<TeamData> qryTeamList();

    /**
     * 查询存储的所有联赛
     */
    List<String> qryAllLeagueName();

}

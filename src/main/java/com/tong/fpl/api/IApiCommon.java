package com.tong.fpl.api;

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
     * 刷新player
     */
    void insertPlayer();

}

package com.tong.fpl.api;

/**
 * Create by tong on 2021/2/26
 */
public interface IApiCommon {

    /**
     * 获取当前比赛周
     */
    int getCurrentEvent();

    /**
     * 获取下一比赛周
     */
    int getNextEvent();

    /**
     * 获取event死线（utc格式）
     */
    String getUtcDeadlineByEvent(int event);

}

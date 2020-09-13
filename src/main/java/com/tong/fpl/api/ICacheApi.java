package com.tong.fpl.api;

/**
 * Create by tong on 2020/8/28
 */
public interface ICacheApi {

    void insertTeam();

    void insertHisTeam(String season);

    void insertEvent();

    void insertHisEvent(String season);

    void insertEventFixture();

    void insertHisEventFixture(String season);

    void insertPlayer();

    void insertHisPlayer(String season);

    void insertPlayerStat();

    void insertHisPlayerStat(String season);

    void insertPlayerValue();

    void insertEventLive(int event);

    void deleteKeys(String pattern);

}

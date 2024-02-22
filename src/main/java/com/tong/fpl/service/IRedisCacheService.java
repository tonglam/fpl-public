package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.EventFixturesRes;
import com.tong.fpl.domain.data.response.EventLiveRes;
import com.tong.fpl.domain.data.response.StaticRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerHistoryData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/8/23
 */
public interface IRedisCacheService {

    /**
     * @apiNote insert
     */
    void insertSingleEventFixture(int event, List<EventFixturesRes> eventFixturesResList);

    void insertSingleEventFixtureCache(int event, List<EventFixturesRes> eventFixturesResList);

    void insertLiveFixtureCache();

    void insertPlayer(StaticRes staticRes);

    void insertPlayerStat(StaticRes staticRes);

    void insertPlayerValue(StaticRes staticRes);

    void insertEventLive(int event, EventLiveRes eventLiveRes);

    void insertEventLiveCache(int event, EventLiveRes eventLiveRes);

    void insertLiveBonusCache();

    void insertEventOverallResult(StaticRes staticRes);

    /**
     * @apiNote get
     */
    int getCurrentEvent();

    Map<String, String> getTeamNameMap(String season);

    Map<String, String> getTeamShortNameMap(String season);

    String getUtcDeadlineByEvent(String season, int event);

    String getDeadlineByEvent(String season, int event);

    List<EventFixtureEntity> getEventFixtureByEvent(String season, int event);

    Map<String, List<PlayerFixtureData>> getEventFixtureByTeamId(String season, int teamId);

    Map<Integer, Map<String, List<PlayerFixtureData>>> getTeamEventFixtureMap(String season);

    Map<String, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap();

    Map<String, PlayerEntity> getPlayerMap(String season);

    PlayerEntity getPlayerByElement(String season, int element);

    Map<String, PlayerStatEntity> getPlayerStatMap(String season);

    PlayerStatEntity getPlayerStatByElement(String season, int element);

    Map<String, EventLiveEntity> getEventLiveByEvent(int event);

    Map<String, EventLiveExplainEntity> getEventLiveExplainByEvent(int event);

    Map<String, EventLiveSummaryEntity> getEventLiveSummaryMap(String season);

    Map<String, Map<String, Integer>> getLiveBonusCacheMap();

    Map<String, EventOverallResultData> getEventOverallResultMap(String season);

    EventOverallResultData getEventOverallResultByEvent(String season, int event);

    Map<String, List<PlayerHistoryData>> getPlayerHistoryMap();

    Map<String, List<PlayerSummaryEntity>> getPlayerSummaryMap();


}

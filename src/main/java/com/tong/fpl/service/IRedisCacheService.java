package com.tong.fpl.service;

import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/8/23
 */
public interface IRedisCacheService {

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

    Map<String, EventLiveSummaryEntity> getEventLiveSummaryMap(String season);

    Map<String, Map<String, Integer>> getLiveBonusCacheMap();

}

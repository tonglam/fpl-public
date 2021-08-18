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

    void insertTeam();

    void insertHisTeam(String season);

    void insertEvent();

    void insertHisEvent(String season);

    void insertEventFixture();

    void insertHisEventFixture(String season);

    void insertSingleEventFixture(int event);

    void insertSingleEventFixtureCache(int event);

    void insertLiveFixtureCache();

    void insertPlayer();

    void insertHisPlayer(String season);

    void insertPlayerStat();

    void insertHisPlayerStat(String season);

    void insertPlayerValue();

    void insertEventLive(int event);

    void insertEventLiveCache(int event);

    void insertLiveBonusCache();

    void insertAverageScore(int event);

    void insertDiscloseCache(int tournamentId, int captainGroupId, int entry);

    void insertEventAfterDeadlineCache(int event);

    void insertEventMatchDayCache(int event);

    void insertEventMatchCache(int event);

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

    List<PlayerValueEntity> getPlayerValueByChangeDay(String changeDay);

    Map<String, EventLiveEntity> getEventLiveByEvent(int event);

    Map<String, String> getPositionMap();

    Map<String, Map<String, Integer>> getLiveBonusCacheMap();

    List<Integer> getDiscloseList(int tournamentId, int captainGroupId);

}

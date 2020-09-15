package com.tong.fpl.service;

import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.utils.CommonUtils;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/8/23
 */
public interface IRedisCacheSerive {

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

	void insertPosition();

	int getCurrentEvent();

	int getNextEvent();

	default Map<String, String> getTeamNameMap() {
		return this.getTeamNameMap(CommonUtils.getCurrentSeason());
	}

	Map<String, String> getTeamNameMap(String season);

	default Map<String, String> getTeamShortNameMap() {
		return this.getTeamShortNameMap(CommonUtils.getCurrentSeason());
	}

	Map<String, String> getTeamShortNameMap(String season);

	default Map<String, String> getDeadlineMap() {
		return this.getDeadlineMap(CommonUtils.getCurrentSeason());
	}

	Map<String, String> getDeadlineMap(String season);

	default String getDeadlineByEvent(int event) {
		return this.getDeadlineByEvent(CommonUtils.getCurrentSeason(), event);
	}

	String getDeadlineByEvent(String season, int event);

	default List<EventFixtureEntity> getEventFixtureByEvent(int event) {
		return this.getEventFixtureByEvent(CommonUtils.getCurrentSeason(), event);
	}

	List<EventFixtureEntity> getEventFixtureByEvent(String season, int event);

	default Map<String, List<PlayerFixtureData>> getEventFixtureByTeamId(int teamId) {
		return this.getEventFixtureByTeamId(CommonUtils.getCurrentSeason(), teamId);
	}

	Map<String, List<PlayerFixtureData>> getEventFixtureByTeamId(String season, int teamId);

	default List<PlayerFixtureData> getEventFixtureByTeamIdAndEvent(int teamId, int event) {
		return this.getEventFixtureByTeamIdAndEvent(CommonUtils.getCurrentSeason(), teamId, event);
	}

	List<PlayerFixtureData> getEventFixtureByTeamIdAndEvent(String season, int teamId, int event);

	Map<Integer, LiveFixtureData> getEventLiveFixtureMap();

	default PlayerEntity getPlayerByElememt(int element) {
		return this.getPlayerByElememt(CommonUtils.getCurrentSeason(), element);
	}

	PlayerEntity getPlayerByElememt(String season, int element);

	default PlayerStatEntity getPlayerStatByElement(int element) {
		return this.getPlayerStatByElement(CommonUtils.getCurrentSeason(), element);
	}

	PlayerStatEntity getPlayerStatByElement(String season, int element);

	List<PlayerValueEntity> getPlayerValueByChangeDay(String changeDay);

	Map<String, EventLiveEntity> getEventLiveByEvent(int event);

	Map<String, String> getPositionMap();

}

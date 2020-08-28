package com.tong.fpl.service;

import com.tong.fpl.domain.data.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.entity.EventFixtureEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.PlayerStatEntity;
import com.tong.fpl.domain.entity.PlayerValueEntity;
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

	void insertPlayer();

	void insertHisPlayer(String season);

	void insertPlayerStat();

	void insertHisPlayerStat(String season);

	void insertPlayerValue();

	int getCurrentEvent();

	default Map<Integer, String> getTeamNameMap() {
		return this.getTeamNameMap(CommonUtils.getCurrentSeason());
	}

	Map<Integer, String> getTeamNameMap(String season);

	default Map<Integer, String> getTeamShortNameMap() {
		return this.getTeamShortNameMap(CommonUtils.getCurrentSeason());
	}

	Map<Integer, String> getTeamShortNameMap(String season);

	default Map<Integer, String> getDeadlineMap() {
		return this.getDeadlineMap(CommonUtils.getCurrentSeason());
	}

	Map<Integer, String> getDeadlineMap(String season);

	default String getDeadlineByEvent(int event) {
		return this.getDeadlineByEvent(CommonUtils.getCurrentSeason(), event);
	}

	String getDeadlineByEvent(String season, int event);

	default List<EventFixtureEntity> getEventFixtureByEvent(int event) {
		return this.getEventFixtureByEvent(CommonUtils.getCurrentSeason(), event);
	}

	List<EventFixtureEntity> getEventFixtureByEvent(String season, int event);

	default List<PlayerFixtureData> getEventFixtureByTeamId(int teamId) {
		return this.getEventFixtureByTeamId(CommonUtils.getCurrentSeason(), teamId);
	}

	List<PlayerFixtureData> getEventFixtureByTeamId(String season, int teamId);

	default PlayerEntity getPlayerByElememt(int element) {
		return this.getPlayerByElememt(CommonUtils.getCurrentSeason(), element);
	}

	PlayerEntity getPlayerByElememt(String season, int element);

	default PlayerStatEntity getPlayerStatByElement(int element) {
		return this.getPlayerStatByElement(CommonUtils.getCurrentSeason(), element);
	}

	PlayerStatEntity getPlayerStatByElement(String season, int element);

	List<PlayerValueEntity> getPlayerValueByChangeDay(String changeDay);

}

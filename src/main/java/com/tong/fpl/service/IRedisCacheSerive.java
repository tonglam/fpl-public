package com.tong.fpl.service;

import com.tong.fpl.domain.data.letletme.team.TeamFixtureData;
import com.tong.fpl.domain.entity.EventFixtureEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.PlayerStatEntity;
import com.tong.fpl.domain.entity.PlayerValueEntity;

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

	Map<Integer, String> getTeamNameMap(String season);

	Map<Integer, String> getTeamShortNameMap(String season);

	String getDeadlineByEvent(String season, int event);

	List<EventFixtureEntity> getEventFixtureByEvent(String season, int event);

	List<TeamFixtureData> getEventFixtureByTeamId(String season, int teamId);

	PlayerEntity getPlayerByElememt(String season, int element);

	PlayerStatEntity getPlayerStatByElement(String season, int element);

	List<PlayerValueEntity> getPlayerValueByChangeDay(String changeDay);

}

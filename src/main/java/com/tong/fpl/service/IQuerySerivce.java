package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import com.tong.fpl.utils.CommonUtils;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/7/31
 */
public interface IQuerySerivce {

	default Map<Integer, PlayerEntity> qryAllPlayerList() {
		return this.qryAllPlayerList(CommonUtils.getCurrentSeason());
	}

	Map<Integer, PlayerEntity> qryAllPlayerList(String season);

	default Map<Integer, PlayerStatEntity> qryAllPlayerStatList() {
		return this.qryAllPlayerStatList(CommonUtils.getCurrentSeason());
	}

	Map<Integer, PlayerStatEntity> qryAllPlayerStatList(String season);

	default int qryPlayerElementByCode(int code) {
		return this.qryPlayerElementByCode(CommonUtils.getCurrentSeason(), code);
	}

	int qryPlayerElementByCode(String season, int code);

	default int qryPlayerElementByWebName(String webName) throws Exception {
		return this.qryPlayerElementByWebName(CommonUtils.getCurrentSeason(), webName);
	}

	int qryPlayerElementByWebName(String season, String webName) throws Exception;

	EntryEventData qryEntryInfoData(String season, int entry);

	List<EntryEventResultData> qryEntryResult(String season, int entry);

	EntryEventResultData qryEntryEventResult(String season, int event, int entry);

	List<EventLiveEntity> qryEventLiveAll(String season, int element);

	List<EventLiveEntity> qryEventLive(String season, int event, int element);

	PlayerData qryPlayerData(int element);

	PlayerInfoData initPlayerInfo(String season, PlayerEntity playerEntity);

	List<PlayerInfoData> qryAllPlayers(String season);

	default List<EntryPickData> qryPickListFromPicks(String picks) {
		return this.qryPickListFromPicks(CommonUtils.getCurrentSeason(), picks);
	}

	List<EntryPickData> qryPickListFromPicks(String season, String picks);

	EntryInfoEntity qryEntryInfo(int entry);

	TournamentInfoEntity qryTournamentInfoById(int tournamentId);

	List<TournamentKnockoutEntity> qryKnockoutListByTournamentId(int tournamentId);

	List<TournamentKnockoutResultData> qryKnockoutResultByTournament(int tournamentId);

	List<TournamentGroupFixtureData> qryGroupFixtureListById(int tournamentId);

	List<TournamentKnockoutFixtureData> qryKnockoutFixtureListById(int tournamentId);

	int getCurrentEvent();

	int getNextEvent();

	UserPicksRes getUserPicks(int event, int entry);

	EntryRes getEntry(int entry);

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

	Map<String, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap();

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

    List<Integer> qryEntryListByTournament(int tournamentId);

    PlayerEntity qryPlayerInfo(int element);

    List<String> qryTeamSelectStatList();

}

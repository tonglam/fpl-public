package com.tong.fpl.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.TransferRes;
import com.tong.fpl.domain.data.response.UserHistoryRes;
import com.tong.fpl.domain.data.response.UserPicksRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.*;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/7/31
 */
public interface IQueryService {

	/**
	 * @apiNote player
	 */
	default int qryPlayerElementByCode(int code) {
		return this.qryPlayerElementByCode(CommonUtils.getCurrentSeason(), code);
	}

	int qryPlayerElementByCode(String season, int code);

	default int qryPlayerElementByWebName(String webName) throws Exception {
		return this.qryPlayerElementByWebName(CommonUtils.getCurrentSeason(), webName);
	}

	int qryPlayerElementByWebName(String season, String webName) throws Exception;

	default String qryPlayerWebNameByElement(int element) {
		return this.qryPlayerWebNameByElement(CommonUtils.getCurrentSeason(), element);
	}

	String qryPlayerWebNameByElement(String season, int element);

	PlayerData qryPlayerData(int element);

	PlayerInfoData initPlayerInfo(String season, PlayerEntity playerEntity);

	List<PlayerFixtureData> qryPlayerFixture(int teamId);

	PlayerDetailData qrySeasonData(String season, int code);

	List<PlayerDetailData> qryHistorySeasonData(int code);

	List<PlayerInfoData> qryAllPlayers(String season);

	default PlayerEntity getPlayerByElement(int element) {
		return this.getPlayerByElement(CommonUtils.getCurrentSeason(), element);
	}

	PlayerEntity getPlayerByElement(String season, int element);

	default PlayerStatEntity getPlayerStatByElement(int element) {
		return this.getPlayerStatByElement(CommonUtils.getCurrentSeason(), element);
	}

	PlayerStatEntity getPlayerStatByElement(String season, int element);

	List<PlayerValueEntity> getPlayerValueByChangeDay(String changeDay);

	default PlayerShowData qryPlayerShowData(int element) {
		return this.qryPlayerShowData(element, Maps.newHashMap(), Maps.newHashMap(), Maps.newHashMap(), Maps.newHashMap(), HashMultimap.create());
	}

	PlayerShowData qryPlayerShowData(int element, Map<String, String> positionMap, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap,
	                                 Map<Integer, PlayerEntity> playerMap, Multimap<Integer, EventLiveEntity> eventLiveMap);

	/**
	 * @apiNote entry
	 */
	default EntryInfoEntity qryEntryInfo(int entry) {
		return this.qryEntryInfo(CommonUtils.getCurrentSeason(), entry);
	}

	EntryInfoEntity qryEntryInfo(String season, int entry);

	EntryRes getEntry(int entry);

	UserPicksRes getUserPicks(int event, int entry);

	UserHistoryRes getUserHistory(int entry);

	List<Integer> qryEntryTournamentList(int entry);

	List<TransferRes> getTransfer(int entry);

	/**
	 * @apiNote event
	 */
	int getCurrentEvent();

	int getLastEvent();

	int getNextEvent();

	default String getDeadlineByEvent(int event) {
		return this.getDeadlineByEvent(CommonUtils.getCurrentSeason(), event);
	}

	String getDeadlineByEvent(String season, int event);

	default String getScoutDeadlineByEvent(int event) {
		String deadline = this.getDeadlineByEvent(event);
		String checkTime = StringUtils.substringBefore(deadline, " ") + "T08:30:00";
		return LocalDateTime.parse(deadline.replaceAll(" ", "T")).isAfter(LocalDateTime.parse(checkTime)) ?
				LocalDateTime.parse(checkTime).format(DateTimeFormatter.ofPattern(Constant.DATETIME)) :
				LocalDate.parse(StringUtils.substringBefore(this.getDeadlineByEvent(event), " ")).minusDays(1)
						.format(DateTimeFormatter.ofPattern(Constant.DATE)) + " 08:30:00";
	}

	List<LocalDate> getMatchDayByEvent(int event);

	List<LocalDateTime> getMatchDayTimeByEvent(int event);

	boolean isMatchDay(int event);

	boolean isMatchDayTime(int event);

	boolean isLastMatchDay(int event);

	/**
	 * @apiNote team
	 */
	default Map<String, String> getTeamNameMap() {
		return this.getTeamNameMap(CommonUtils.getCurrentSeason());
	}

	Map<String, String> getTeamNameMap(String season);

	default Map<String, String> getTeamShortNameMap() {
		return this.getTeamShortNameMap(CommonUtils.getCurrentSeason());
	}

	Map<String, String> getTeamShortNameMap(String season);

	Map<String, String> getPositionMap();

	/**
	 * @apiNote fixture
	 */
	default List<EventFixtureEntity> getEventFixtureByEvent(int event) {
		return this.getEventFixtureByEvent(CommonUtils.getCurrentSeason(), event);
	}

	List<EventFixtureEntity> getEventFixtureByEvent(String season, int event);

	default Map<String, List<PlayerFixtureData>> getEventFixtureByTeamId(int teamId) {
		return this.getEventFixtureByTeamId(CommonUtils.getCurrentSeason(), teamId);
	}

	Map<String, List<PlayerFixtureData>> getEventFixtureByTeamId(String season, int teamId);

	List<TournamentGroupFixtureData> qryGroupFixtureListById(int tournamentId);

	List<TournamentKnockoutFixtureData> qryKnockoutFixtureListById(int tournamentId);

	/**
	 * @apiNote event_live
	 */
	List<EventLiveEntity> qryEventLiveAll(String season, int element);

	List<EventLiveEntity> qryEventLive(String season, int event, int element);

	default List<EntryPickData> qryPickListFromPicks(String picks) {
		return this.qryPickListFromPicks(CommonUtils.getCurrentSeason(), picks);
	}

	List<EntryPickData> qryPickListFromPicks(String season, String picks);

	default PlayerPickData qryPickListByPosition(String picks) {
		return this.qryPickListByPosition(CommonUtils.getCurrentSeason(), picks);
	}

	PlayerPickData qryPickListByPosition(String season, String picks);

	/**
	 * @apiNote event_result
	 */
	List<EntryEventResultData> qryEntryResult(String season, int entry);

	default EntryEventResultData qryEntryEventResult(int event, int entry) {
		return this.qryEntryEventResult(CommonUtils.getCurrentSeason(), event, entry);
	}

	EntryEventResultData qryEntryEventResult(String season, int event, int entry);

	PlayerPickData qryEntryPickData(int entry);

	/**
	 * @apiNote tournament
	 */
	List<TournamentInfoEntity> qryAllTournamentList();

	TournamentInfoData qryTournamentDataById(int tournamentId);

	TournamentInfoEntity qryTournamentInfoById(int tournamentId);

	List<Integer> qryEntryListByTournament(int tournamentId);

	List<EntryInfoData> qryGroupEntryInfoList(int tournamentId, int groupId);

	List<TournamentKnockoutEntity> qryKnockoutListByTournamentId(int tournamentId);

	KnockoutBracketData qryKnockoutBracketResultByTournament(int tournamentId);

	List<TournamentKnockoutResultData> qryKnockoutResultByTournament(int tournamentId);

	List<ZjTournamentCaptainData> qryZjTournamentCaptain(int tournamentId);

	Map<String, String> qryZjTournamentGroupNameMap(int tournamentId);

	List<TournamentKnockoutResultData> qryZjTournamentPkResultByTournament(int tournamentId);

	Map<String, Integer> qryZjTournamentPhaseOneRankMap(int tournamentId);

	Map<String, Integer> qryZjTournamentPhaseTwoGroupPointsMap(int tournamentId);

	Map<String, Integer> qryZjTournamentPkGroupPointsMap(int tournamentId);

	Map<String, Integer> qryZjTournamentPhaseTwoRankMap(int tournamentId);

	Map<String, Integer> qryZjTournamentPkRankMap(int tournamentId);

	Map<String, Integer> qryZjTournamentRankMap(List<ZjTournamentResultData> list);

	Map<String, Integer> qryZjTournamentGroupEntryGroupIdMap(int tournamentId);

	Map<String, String> qryZjTournamentGroupEntryGroupNameMap(int tournamentId);

	TournamentGroupData qryDiscloseGroupData(int tournamentId, int entry, int currentGroupId);

	List<TournamentKnockoutEventFixtureData> qryZjPkPickListById(int tournamentId);

	/**
	 * @apiNote report
	 */
	String qryLeagueNameByIdAndType(int leagueId, String leagueType);

	List<String> qryTeamSelectStatList();

	/**
	 * @apiNote live, cannot be cached
	 */
	Map<String, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap();

	Map<String, EventLiveEntity> getEventLiveByEvent(int event);

	Map<String, Map<String, Integer>> getLiveBonusCacheMap();

	List<LiveMatchData> qryLiveMatchList(int statusId);

	/**
	 * @apiNote scout
	 */
	ScoutData qryScoutEntryEventData(int event, int entry);

}

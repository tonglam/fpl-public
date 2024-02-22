package com.tong.fpl.service;

import com.google.common.collect.Multimap;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventAutoSubsData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.league.LeagueEventInfoData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.*;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.domain.letletme.team.TeamAgainstMatchInfoData;
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
     * @apiNote time
     */
    boolean isAfterMatchDay(int event);

    List<LocalDate> getAfterMatchDayByEvent(int event);

    boolean isMatchDayTime(int event);

    List<LocalDateTime> getMatchDayTimeByEvent(int event);

    /**
     * @apiNote player
     */
    default Map<String, PlayerEntity> getPlayerMap() {
        return this.getPlayerMap(CommonUtils.getCurrentSeason());
    }

    Map<String, PlayerEntity> getPlayerMap(String season);

    default PlayerEntity getPlayerByElement(int element) {
        return this.getPlayerByElement(CommonUtils.getCurrentSeason(), element);
    }

    PlayerEntity getPlayerByElement(String season, int element);

    default Map<String, PlayerStatEntity> getPlayerStatMap() {
        return this.getPlayerStatMap(CommonUtils.getCurrentSeason());
    }

    Map<String, PlayerStatEntity> getPlayerStatMap(String season);

    default PlayerStatEntity getPlayerStatByElement(int element) {
        return this.getPlayerStatByElement(CommonUtils.getCurrentSeason(), element);
    }

    PlayerStatEntity getPlayerStatByElement(String season, int element);

    default int qryPlayerElementByWebName(String webName) throws Exception {
        return this.qryPlayerElementByWebName(CommonUtils.getCurrentSeason(), webName);
    }

    int qryPlayerElementByWebName(String season, String webName) throws Exception;

    default Map<String, String> qryPlayerWebNameMap() {
        return this.qryPlayerWebNameMap(CommonUtils.getCurrentSeason());
    }

    Map<String, String> qryPlayerWebNameMap(String season);

    default int qryPlayerElementByCode(int code) {
        return this.qryPlayerElementByCode(CommonUtils.getCurrentSeason(), code);
    }

    int qryPlayerElementByCode(String season, int code);

    PlayerData qryPlayerData(int element);

    PlayerInfoData initPlayerInfo(String season, PlayerEntity playerEntity);

    default List<PlayerFixtureData> qryPlayerFixtureList(int teamId, int previous, int next) {
        return this.qryPlayerFixtureList(CommonUtils.getCurrentSeason(), teamId, previous, next);
    }

    List<PlayerFixtureData> qryPlayerFixtureList(String season, int teamId, int previous, int next);

    default PlayerDetailData qryPlayerDetailData(int element) {
        return this.qryPlayerDetailData(CommonUtils.getCurrentSeason(), element);
    }

    PlayerDetailData qryPlayerDetailData(String season, int element);

    List<PlayerDetailData> qryHistorySeasonData(int code);

    List<PlayerInfoData> qryAllPlayers(String season);

    PlayerShowData qryPlayerShowData(int event, int element,
                                     Map<String, String> teamNameMap, Map<String, String> teamShortNameMap,
                                     Map<String, PlayerEntity> playerMap, Map<Integer, PlayerStatEntity> playerStatMap,
                                     Multimap<Integer, EventLiveEntity> eventLiveMap,
                                     Map<Integer, Map<String, List<PlayerFixtureData>>> teamFixtureMap);

    /**
     * @apiNote entry
     */
    default EntryInfoData qryEntryInfo(int entry) {
        return this.qryEntryInfo(CommonUtils.getCurrentSeason(), entry);
    }

    EntryInfoData qryEntryInfo(String season, int entry);

    List<Integer> qryEntryTournamentEntryList(int entry);

    List<EntryEventPickEntity> qryEventPickByEntryList(int event, List<Integer> entryList);

    /**
     * @apiNote event
     */
    int getCurrentEvent();

    int getLastEvent();

    int getNextEvent();

    default String getUtcDeadlineByEvent(int event) {
        return this.getUtcDeadlineByEvent(CommonUtils.getCurrentSeason(), event);
    }

    String getUtcDeadlineByEvent(String season, int event);

    default String getDeadlineByEvent(int event) {
        return this.getDeadlineByEvent(CommonUtils.getCurrentSeason(), event);
    }

    String getDeadlineByEvent(String season, int event);

    default String getScoutDeadlineByEvent(int event) {
        String deadline = this.getDeadlineByEvent(event);
        String checkTime = StringUtils.substringBefore(deadline, " ") + "T08:30:00";
        String scoutDeadLine =
                LocalDateTime.parse(deadline.replaceAll(" ", "T"))
                        .isAfter(LocalDateTime.parse(checkTime)) ?
                        LocalDateTime.parse(checkTime)
                                .format(DateTimeFormatter.ofPattern(Constant.DATETIME)) :
                        LocalDate.parse(StringUtils.substringBefore(deadline, " ")).minusDays(1)
                                .format(DateTimeFormatter.ofPattern(Constant.DATE)) + " 08:30:00";
        return scoutDeadLine.replaceAll(" ", "T") + "Z";
    }

    /**
     * @apiNote team
     */
    default Map<String, String> getTeamNameMap() {
        return this.getTeamNameMap(CommonUtils.getCurrentSeason());
    }

    Map<String, String> getTeamNameMap(String season);

    default String getTeamNameByTeam(String season, int teamId) {
        return this.getTeamNameMap(season).getOrDefault(String.valueOf(teamId), "");
    }

    default Map<String, String> getTeamShortNameMap() {
        return this.getTeamShortNameMap(CommonUtils.getCurrentSeason());
    }

    Map<String, String> getTeamShortNameMap(String season);

    default String getTeamShortNameByTeam(String season, int teamId) {
        return this.getTeamShortNameMap(season).getOrDefault(String.valueOf(teamId), "");
    }

    Map<String, List<EventFixtureEntity>> qryTeamAgainstFixture(int teamCode, int againstCode);

    Map<String, TeamAgainstMatchInfoData> qryTeamAgainstMatchInfo(int teamCode, int againstCode);

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

    default Map<Integer, Map<String, List<PlayerFixtureData>>> getTeamEventFixtureMap() {
        return this.getTeamEventFixtureMap(CommonUtils.getCurrentSeason());
    }

    Map<Integer, Map<String, List<PlayerFixtureData>>> getTeamEventFixtureMap(String season);

    List<TournamentGroupFixtureData> qryGroupFixtureListById(int tournamentId);

    List<TournamentKnockoutFixtureData> qryKnockoutFixtureListById(int tournamentId);

    /**
     * @apiNote event_live
     */
    Map<String, EventLiveExplainEntity> getEventLiveExplainByEvent(int event);

    default Map<String, EventLiveSummaryEntity> getEventLiveSummaryMap() {
        return this.getEventLiveSummaryMap(CommonUtils.getCurrentSeason());
    }

    Map<String, EventLiveSummaryEntity> getEventLiveSummaryMap(String season);

    default EventLiveEntity qryEventLiveByElement(int event, int element) {
        return this.qryEventLiveByElement(CommonUtils.getCurrentSeason(), event, element);
    }

    EventLiveEntity qryEventLiveByElement(String season, int event, int element);

    EventLiveSummaryEntity qryEventLiveSummaryByElement(String season, int element);

    /**
     * @apiNote entry_event_result
     */
    List<EntryEventResultData> qryEntryResult(String season, int entry);

    default EntryEventResultData qryEntryEventResult(int event, int entry) {
        return this.qryEntryEventResult(CommonUtils.getCurrentSeason(), event, entry);
    }

    EntryEventResultData qryEntryEventResult(String season, int event, int entry);

    default List<EntryPickData> qryPickListFromPicks(String picks) {
        return this.qryPickListFromPicks(CommonUtils.getCurrentSeason(), picks);
    }

    List<EntryPickData> qryPickListFromPicks(String season, String picks);

    default PlayerPickData qryPickListByPosition(String picks) {
        return this.qryPickListByPosition(CommonUtils.getCurrentSeason(), picks);
    }

    PlayerPickData qryPickListByPosition(String season, String picks);

    default PlayerPickData qryPickListByPositionForTransfers(String picks) {
        return this.qryPickListByPositionForTransfers(CommonUtils.getCurrentSeason(), picks);
    }

    PlayerPickData qryPickListByPositionForTransfers(String season, String picks);

    PlayerPickData qryEntryPickData(int event, int entry);

    PlayerPickData qryEntryPickDataForTransfers(int event, int entry);

    List<PlayerPickData> qryLeaguePickDataList(int leagueId, String leagueType, List<Integer> entryList);

    List<PlayerPickData> qryLeagueEventPickDataList(int event, int leagueId, String leagueType);

    List<PlayerPickData> qryOffiaccountPickList();

    List<PlayerPickData> qryOffiaccountLineupForTransfers();

    default List<EntryEventAutoSubsData> qryEntryAutoSubDataList(int event, int entry) {
        return this.qryEntryAutoSubDataList(CommonUtils.getCurrentSeason(), event, entry);
    }

    List<EntryEventAutoSubsData> qryEntryAutoSubDataList(String season, int event, int entry);

    List<EntryEventAutoSubsData> qryAutoSubListFromAutoSubs(String season, int event, String autoSubs);

    List<EntryEventAutoSubsData> qryLeagueEventAutoSubDataList(int event, int leagueId, String leagueType);

    Map<Integer, Integer> qryEntryFreeTransfersMap(int entry);

    String qryEntryEventPicks(int event, int entry, int operator);

    /**
     * @apiNote league
     */
    int qryCountTournamentLeagueTeams(String url);

    /**
     * @apiNote tournament
     */
    TournamentInfoData qryTournamentDataById(int tournamentId);

    TournamentInfoEntity qryTournamentInfoById(int tournamentId);

    List<Integer> qryEntryListByTournament(int tournamentId);

    List<Integer> qryEntryListByKnockout(int tournamentId, int event);

    Map<String, TournamentKnockoutEntity> qryKnockoutMapByTournament(int tournamentId, int event);

    Map<String, List<TournamentKnockoutEventResultData>> qryKnockoutRoundMapByTournament(int tournamentId, int round, int event);

    List<Integer> qryEntryListByChampionLeagueGroup(int tournamentId, String groupName);

    List<Integer> qryEntryListByChampionLeagueKnockout(int tournamentId, int round);

    KnockoutBracketData qryKnockoutBracketResultByTournament(int tournamentId);

    List<TournamentKnockoutResultData> qryKnockoutResultByTournament(int tournamentId);

    List<EntryEventPickEntity> qryTournamentEntryEventPick(int event, int tournamentId);

    Map<String, String> qryTournamentGroupNameMap(int tournamentId);

    Map<String, Integer> qryTournamentGroupEntryGroupIdMap(int tournamentId);

    TournamentRoyaleData qryEventTournamentRoyale(int event, int tournamentId);

    /**
     * @apiNote report
     */
    LeagueEventReportEntity qryLeagueInfoByName(String leagueName);

    String qryLeagueNameByIdAndType(int leagueId, String leagueType);

    List<String> qryTeamSelectStatList();

    Map<String, String> qryLeagueEventEoMap(int event, int leagueId, String leagueType);

    LeagueEventInfoData qryLeagueEventReportDataByLeagueId(int leagueId);

    /**
     * @apiNote live, cannot be cached
     */
    Map<String, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap();

    Map<String, EventLiveEntity> getEventLiveByEvent(int event);

    Map<String, Map<String, Integer>> getLiveBonusCacheMap();

    List<LiveMatchData> qryLiveMatchList(int statusId);

    List<LiveMatchTeamData> qryLiveTeamDataList(int statusId);

    /**
     * @apiNote scout
     */
    ScoutData qryScoutEntryEventData(int event, int entry);

    /**
     * @apiNote simulate
     */
    PlayerPickData qryEntryEventPickData(int event, int entry, int operator);

}

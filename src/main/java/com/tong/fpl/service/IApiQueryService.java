package com.tong.fpl.service;

import com.tong.fpl.domain.entity.TournamentKnockoutResultEntity;
import com.tong.fpl.domain.letletme.element.*;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.league.LeagueEventSelectData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.*;
import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.scout.PopularScoutData;
import com.tong.fpl.domain.letletme.team.TeamAgainstInfoData;
import com.tong.fpl.domain.letletme.team.TeamData;
import com.tong.fpl.domain.letletme.team.TeamSummaryData;
import com.tong.fpl.domain.letletme.tournament.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiQueryService {

    /**
     * @apiNote common
     */
    Map<String, String> qryCurrentEventAndNextUtcDeadline();

    Map<String, Integer> qryEventAverageScore();

    List<TeamData> qryTeamList(String season);

    List<LeagueInfoData> qryAllLeagueName(String season);

    List<PlayerFixtureData> qryNextFixture(int event);

    /**
     * @apiNote entry
     */
    List<EntryInfoData> fuzzyQueryEntry(EntryQueryParam param);

    EntryInfoData qryEntryInfo(int entry);

    EntryLeagueData qryEntryLeagueInfo(int entry);

    EntryHistoryData qryEntryHistoryInfo(int entry);

    EntryEventResultData qryEntryEventResult(int event, int entry);

    List<ElementEventResultData> qryEntryEventPicksResult(int event, String chip, List<EntryPickData> pickList);

    List<EntryEventTransfersData> qryEntryEventTransfers(int event, int entry);

    List<EntryEventTransfersData> qryEntryAllTransfers(int entry);

    /**
     * @apiNote scout
     */
    Map<String, String> qryScoutEntry();

    EventScoutData qryEventScoutPickResult(int event, int entry);

    List<EventScoutData> qryEventScoutResult(int event);

    int qryEventScoutLeftTransfers(int entry, int event);

    List<String> qryAllPopularScoutSource();

    PopularScoutData qryEventSourceScoutResult(int event, String source);

    List<PopularScoutData> qryOverallEventScoutResult(int event);

    /**
     * @apiNote live
     */
    List<LiveMatchData> qryLiveMatchByStatus(String playStatus);

    /**
     * @apiNote player
     */
    PlayerInfoData qryPlayerInfoByElement(String season, int element);

    PlayerInfoData qryPlayerInfoByCode(String season, int code);

    LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType);

    PlayerDetailData qryPlayerDetailByElement(int element);

    Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(String shortName);

    List<PlayerFilterData> qryFilterPlayers(String season);

    /**
     * @apiNote stat
     */
    Map<String, List<PlayerValueData>> qryPlayerValueByDate(String changeDate);

    List<PlayerValueData> qryPlayerPriceChange(String date);

    List<PlayerValueData> qryPlayerValueByElement(int element);

    List<List<String>> qrySeasonFixture();

    Map<String, String> qryLeagueEventEoWebNameMap(String season, int event, int leagueId, String leagueType);

    LeagueEventSelectData qryLeagueSelectByName(String season, int event, int leagueId, String leagueName);

    PlayerSummaryData qryPlayerSummary(String season, int code);

    List<PlayerSeasonSummaryData> qryPlayerSeasonSummary(int code);

    TeamSummaryData qryTeamSummary(String season, String name);

    ElementEventLiveExplainData qryElementEventExplainResult(int event, int element);

    TeamAgainstInfoData qryTeamAgainstRecordInfo(int teamId, int againstId);

    List<ElementSummaryData> qryTeamAgainstRecordResult(String season, int event, int teamHId, int teamAId);

    List<ElementAgainstInfoData> qryTopElementAgainstInfo(int teamId, int againstId, boolean active);

    List<ElementAgainstRecordData> qryElementAgainstRecord(int teamId, int againstId, int elementCode);

    /**
     * @apiNote tournament
     */
    List<Integer> qryEntryTournamentEntry(int entry);

    List<TournamentInfoData> qryEntryPointsRaceTournament(int entry);

    List<TournamentInfoData> qryEntryKnockoutTournament(int entry);

    String qryEntryChampionLeagueStage(int entry, int tournamentId);

    LinkedHashMap<String, List<String>> qryChampionLeagueStage(int tournamentId);

    LinkedHashMap<String, List<String>> qryChampionLeagueStageGroup(int tournamentId);

    List<List<TournamentGroupData>> qryChampionLeagueGroupQualifications(int tournamentId);

    List<List<TournamentKnockoutData>> qryChampionLeagueStageKnockoutRound(int tournamentId, int round);

    List<TournamentKnockoutResultEntity> qryChampionLeagueStageKnockoutRoundResult(int tournamentId, int round);

    List<TournamentInfoData> qryEntryChampionLeague(int entry);

    TournamentInfoData qryTournamentInfo(int id);

    List<EntryEventResultData> qryTournamentEventResult(int event, int tournamentId);

    SearchEntryEventResultData qryTournamentEventSearchResult(int event, int tournamentId, int element);

    List<EntryEventResultData> qryChampionLeagueEventResult(int event, int tournamentId);

    List<TournamentPointsGroupEventResultData> qryTournamentEventSummary(int event, int tournamentId);

    List<TournamentPointsGroupEventResultData> qryTournamentEntryEventSummary(int tournamentId, int entry);

    TournamentGroupEventChampionData qryTournamentEventChampion(int tournamentId);

    List<Integer> qryDrawKnockoutEntries(int tournamentId);

    List<EntryAgainstInfoData> qryDrawKnockoutResults(int tournamentId);

    List<EntryInfoData> qryDrawKnockoutOpponents(int tournamentId, int entry);

    String qryDrawKnockoutNotice(int tournamentId);

    List<List<EntryInfoData>> qryDrawKnockoutPairs(int tournamentId);

    /**
     * @apiNote summary
     */
    EventOverallResultData qryEventOverallResult(int event);

    List<ElementEventData> qryEventDreamTeam(int event);

    List<ElementEventData> qryEventEliteElements(int event);

    Map<String, List<ElementEventData>> qryEventOverallTransfers(int event);

}

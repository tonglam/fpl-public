package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.domain.letletme.league.LeagueEventSelectData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.team.TeamData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentPointsGroupEventResultData;

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

    /**
     * @apiNote entry
     */
    EntryInfoData qryEntryInfo(int entry);

    List<EntryInfoData> fuzzyQueryEntry(EntryQueryParam param);

    EntryLeagueInfoData qryEntryLeagueInfo(int entry);

    EntryHistoryInfoData qryEntryHistoryInfo(int entry);

    EntryEventResultData qryEntryEventResult(int event, int entry);

    List<ElementEventResultData> qryEntryEventPicksResult(int event, String chip, List<EntryPickData> pickList);

    List<EntryEventTransfersData> qryEntryEventTransfers(int event, int entry);

    List<EntryEventResultData> qryEntryEventSummary(int entry);

    /**
     * @apiNote live
     */
    List<LiveMatchData> qryLiveMatchByStatus(String playStatus);

    /**
     * @apiNote player
     */
    PlayerInfoData qryPlayerInfoByElement(int event, int element);

    LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType);

    PlayerDetailData qryPlayerDetailByElement(int element);

    Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(String shortName);

    /**
     * @apiNote team
     */
    List<TeamData> qryTeamList();

    /**
     * @apiNote stat
     */
    Map<String, List<PlayerValueData>> qryPlayerValueByDate(String changeDate);

    List<PlayerValueData> qryPlayerValueByElement(int element);

    Map<String, List<PlayerValueData>> qryPlayerValueByTeamId(int teamId);

    List<String> qryAllLeagueName();

    Map<String, String> qryLeagueEventEoWebNameMap(int event, int leagueId, String leagueType);

    LeagueEventSelectData qryTeamSelectByLeagueName(int event, String leagueName);

    /**
     * @apiNote scout
     */
    Map<String, String> qryScoutEntry();

    EventScoutData qryEventScoutPickResult(int event, int entry);

    List<EventScoutData> qryEventScoutResult(int event);

    /**
     * @apiNote tournament
     */
    List<Integer> qryEntryTournamentEntry(int entry);

    List<TournamentInfoData> qryEntryPointsRaceTournament(int entry);

    TournamentInfoData qryTournamentInfo(int id);

    List<EntryEventResultData> qryTournamentEventResult(int event, int tournamentId);

    List<Integer> qryTournamentEntryContainElement(int event, int tournamentId, int element);

    List<Integer> qryTournamentEntryPlayElement(int event, int tournamentId, int element);

    List<TournamentPointsGroupEventResultData> qryTournamentEventSummary(int event, int tournamentId);

    List<TournamentPointsGroupEventResultData> qryTournamentEntryEventSummary(int tournamentId, int entry);

}

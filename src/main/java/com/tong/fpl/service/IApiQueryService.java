package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;

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

    /**
     * @apiNote entry
     */
    EntryInfoData qryEntryInfo(int entry);

    List<EntryInfoData> fuzzyQueryEntry(EntryQueryParam param);

    EntryLeagueInfoData qryEntryLeagueInfo(int entry);

    EntryHistoryInfoData qryEntryHistoryInfo(int entry);

    EntryEventResultData qryEntryEventResult(int event, int entry);

    /**
     * @apiNote live
     */
    List<LiveMatchData> qryLiveMatchByStatus(String playStatus);

    /**
     * @apiNote team
     */
    Map<String, String> getTeamNameMap();

    default String getTeamNameByTeam(int teamId) {
        return this.getTeamNameMap().getOrDefault(String.valueOf(teamId), "");
    }

    Map<String, String> getTeamShortNameMap();

    default String getShortTeamNameByTeam(int teamId) {
        return this.getTeamShortNameMap().getOrDefault(String.valueOf(teamId), "");
    }

    /**
     * @apiNote player
     */
    PlayerInfoData qryPlayerInfoByElement(int event, int element);

    LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType);

    PlayerDetailData qryPlayerDetailByElement(int element);

    Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(String shortName);

    /**
     * @apiNote report
     */
    Map<String, List<PlayerValueData>> qryPlayerValueByDate(String changeDate);

    List<String> qryLeagueName();

    Map<String, String> qryLeagueEventEoWebNameMap(int event, int leagueId, String leagueType);

    LeagueStatData qryTeamSelectByLeagueName(int event, String leagueName);

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

    List<TournamentInfoData> qryEntryTournament(int entry);

}

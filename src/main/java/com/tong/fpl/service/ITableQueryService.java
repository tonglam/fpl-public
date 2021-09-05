package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.entry.EntryCupData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportStatData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerShowData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.domain.letletme.tournament.*;

import java.util.List;

/**
 * Create by tong on 2020/8/28
 */
public interface ITableQueryService {

    /**
     * @apiNote player
     */
    TableData<PlayerInfoData> qryPlayerList(String season);

    TableData<PlayerInfoData> qryPagePlayerDataList(int page, int limit);

    TableData<PlayerValueData> qryPriceChangeList();

    TableData<PlayerShowData> qryPlayerShowListByElementType(int elementType);

    TableData<PlayerShowData> qryEntryEventPlayerShowList(int event, int entry, int operator);

    TableData<PlayerShowData> qrySortedEntryEventPlayerShowList(List<PlayerShowData> playerShowDataList);

    TableData<PlayerShowData> qryEntryEventPlayerShowListForTransfers(int event, int entry);

    TableData<PlayerShowData> qryPlayerShowListByElementForTransfers(List<EntryPickData> pickList);

    TableData<PlayerDetailData> qryPlayerDetailData(int element);

    /**
     * @apiNote league
     */
    TableData<EntryInfoData> qryLeagueEntryList(String url);

    /**
     * @apiNote tournament
     */
    TableData<TournamentInfoData> qryTournamentList(TournamentQueryParam param);

    TableData<TournamentEntryData> qryEntryTournamentList(int entry);

    TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId);

    TableData<TournamentGroupEventChampionData> qryPointsGroupChampion(int tournamentId);

    TableData<TournamentPointsGroupEventResultData> qryPagePointsGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

    TableData<EntryCupData> qryPageEntryEventCupResult(int entry, int page, int limit);

    TableData<TournamentBattleGroupEventResultData> qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

    /**
     * @apiNote live
     */
    TableData<LiveCalcData> qryEntryLivePoints(int entry);

    TableData<LiveCalcData> qryTournamentLivePoints(int tournamentId);

    TableData<LiveMatchTeamData> qryLiveTeamDataList(int statusId);

    /**
     * @apiNote entry_result
     */
    TableData<EntryPickData> qryEntryEventResult(int event, int entry);

    TableData<EntryEventResultData> qryEntryResultList(int entry);

    /**
     * @apiNote report
     */
    TableData<LeagueStatData> qryTeamSelectStatByName(int event, String leagueName);

    TableData<LeagueEventReportStatData> qryLeagueCaptainReportStat(int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryLeagueCaptainEventReportList(int event, int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryEntryCaptainEventReportList(int leagueId, String leagueType, int entry);

    TableData<LeagueEventReportStatData> qryLeagueTransfersReportStat(int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryLeagueTransfersEventReportList(int event, int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryEntryTransfersEventReportList(int leagueId, String leagueType, int entry);

    TableData<LeagueEventReportStatData> qryLeagueScoringReportStat(int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryLeagueScoringEventReportList(int event, int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryEntryScoringEventReportList(int leagueId, String leagueType, int entry);

    /**
     * @apiNote scout
     */
    TableData<ScoutData> qryEventScoutPickList(int event);

    TableData<ScoutData> qryEventScoutList(int event);

}

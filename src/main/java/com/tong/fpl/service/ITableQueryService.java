package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.StepsData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportStatData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerShowData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.domain.letletme.tournament.*;

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

	TableData<PlayerShowData> qryEntryEventPlayerShowList(int event, int entry);

	/**
	 * @apiNote tournament
	 */
	TableData<TournamentInfoData> qryTournamentList(TournamentQueryParam param);

	TableData<TournamentEntryData> qryEntryTournamentList(int entry);

	TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId);

	TableData<TournamentGroupData> qrySeeableGroupInfoListByGroupId(int tournamentId, int currentGroupId, int groupId);

	TableData<TournamentGroupEventChampionData> qryPointsGroupChampion(int tournamentId);

	TableData<TournamentPointsGroupEventResultData> qryPagePointsGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

	TableData<TournamentBattleGroupEventResultData> qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

	TableData<TournamentPointsGroupEventResultData> qryPageZjTournamentGroupResult(int tournamentId, int stage, int groupId, int entry, int page, int limit);

	TableData<ZjTournamentResultData> qryZjTournamentResultById(int tournamentId);

	TableData<StepsData> qryZjTournamentPkPickSteps(int tournamentId);

	TableData<TournamentGroupData> qryZjTournamentPkPickableList(int tournamentId, int currentGroupId);

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

	TableData<ElementEventResultData> qryElementEventResult(int event, int element);

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

	/**
	 * @apiNote scout
	 */
	TableData<ScoutData> qryEventScoutPickList(int event);

	TableData<ScoutData> qryEventScoutList(int event);

}

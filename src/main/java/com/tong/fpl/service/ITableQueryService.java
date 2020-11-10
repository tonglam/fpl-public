package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.StepsData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportStatData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.tournament.*;

/**
 * Create by tong on 2020/8/28
 */
public interface ITableQueryService {

	/**
	 * @apiNote player
	 */
	TableData<PlayerInfoData> qryPlayerList(String season);

	TableData<PlayerInfoData> qryPagePlayerDataList(long page, long limit);

	TableData<PlayerValueData> qryPriceChangeList();

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
	TableData<LiveCalaData> qryEntryLivePoints(int entry);

	TableData<LiveCalaData> qryTournamentLivePoints(int tournamentId);

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
	TableData<LeagueStatData> qryTeamSelectStatByName(String leagueName, int event);

	TableData<LeagueEventReportStatData> qryLeagueReportStat(int leagueId, String leagueType);

	TableData<LeagueEventReportData> qryLeagueEventReportList(int leagueId, String leagueType, int event);

}

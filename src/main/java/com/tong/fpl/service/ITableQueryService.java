package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
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
	 * @apiNote entry
	 */
	TableData<EntryInfoData> qryEntryInfoByTournament(String season, int tournamentId);

	/**
	 * @apiNote tournament
	 */
	TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param);

	TableData<TournamentEntryData> qryEntryTournamentList(int entry);

	TableData<TournamentInfoData> qryEntryPointsGroupTournamentList(int entry);

	TableData<TournamentGroupData> qryTournamentResultList(int tournamentId, int event);

	TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId);

	TableData<TournamentPointsGroupEventResultData> qryPagePointsGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

	TableData<TournamentBattleGroupEventResultData> qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

	/**
	 * @apiNote live
	 */
	TableData<LiveCalaData> qryEntryLivePoints(int entry);

	TableData<LiveCalaData> qryTournamentLivePoints(int tournamentId);

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

	//待重构
	TableData<EntryEventCaptainData> qryEntryCaptainList(String season, int entry);

}

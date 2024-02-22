package com.tong.fpl.letletmeApi;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;

import java.util.List;

/**
 * Create by tong on 2020/8/3
 */
public interface ILiveApi {

	/**
	 * @apiNote entry
	 */
	TableData<LiveCalcData> qryEntryLivePoints(int entry);

	/**
	 * @apiNote league
	 */
	TableData<LiveCalcData> qryTournamentLivePoints(int tournamentId);

	/**
	 * @apiNote match
	 */
	List<LiveMatchData> qryLiveMatchList(int statusId);

	TableData<LiveMatchTeamData> qryLiveTeamDataList(int statusId);

	/**
	 * @apiNote common
	 */
	int getCurrentEvent();

}

package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;

/**
 * Create by tong on 2020/8/3
 */
public interface ILiveApi {

	/**
	 * @apiNote entry
	 */
	TableData<LiveCalaData> qryEntryLivePoints(int entry);

	/**
	 * @apiNote league
	 */
	TableData<LiveCalaData> qryTournamentLivePoints(int tournamentId);

	/**
	 * @apiNote match
	 */
	TableData<LiveMatchData> qryLiveMatchList(int statusId);

}

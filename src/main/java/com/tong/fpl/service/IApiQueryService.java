package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;

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
	EntryInfoData qryEntryInfoData(int entry);

	/**
	 * @apiNote league
	 */

	/**
	 * @apiNote live
	 */
	List<LiveMatchData> qryLiveFixtureByStatus(String playStatus);

	Map<String, LiveMatchTeamData> qryLiveMatchDataByStatus(String playStatus);

	/**
	 * @apiNote player
	 */
	LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType);

	PlayerDetailData qryPlayerDetailData(int element);

	Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(String shortName);

	/**
	 * @apiNote report
	 */

	/**
	 * @apiNote scout
	 */
	Map<String, String> qryScoutEntry();

	/**
	 * @apiNote tournament
	 */


}

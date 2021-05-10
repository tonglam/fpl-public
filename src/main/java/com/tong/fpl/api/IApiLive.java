package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiLive {

	/**
	 * 获取实时比赛
	 */
	List<LiveMatchData> qryLiveFixtureByStatus(String playStatus);

	/**
	 * 获取实时比赛详情(team_short_name -> data)
	 */
	Map<String, LiveMatchTeamData> qryLiveMatchDataByStatus(String playStatus);

}

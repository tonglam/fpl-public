package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.player.PlayerFixtureData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiLive {


	/**
	 * 根据球队缩写获取赛程
	 */
	Map<String, List<PlayerFixtureData>> qryTeamLiveFixture(String shortName);

}

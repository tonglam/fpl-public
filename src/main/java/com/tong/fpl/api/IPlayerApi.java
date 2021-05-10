package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public interface IPlayerApi {

	/**
	 * 根据位置获取球员列表(team_short_name -> player_info)
	 */
	LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType);

	/**
	 * 获取球员详情
	 */
	PlayerDetailData qryPlayerDetailData(@RequestParam int element);

	/**
	 * 根据球队缩写获取赛程
	 */
	Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(String shortName);

}

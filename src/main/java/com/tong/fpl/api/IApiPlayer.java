package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiPlayer {

	/**
	 * 根据位置获取球员列表(team_short_name -> player_info)
	 */
	LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType);

	/**
	 * 获取球员详情
	 */
	PlayerDetailData qryPlayerDetailData(@RequestParam int element);

}

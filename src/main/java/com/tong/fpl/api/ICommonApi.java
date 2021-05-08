package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/2/26
 */
public interface ICommonApi {

	/**
	 * 获取当前比赛周
	 */
	int getCurrentEvent();

	/**
	 * 获取下一比赛周
	 */
	int getNextEvent();

	/**
	 * 获取event死线（utc格式）
	 */
	String getUtcDeadlineByEvent(int event);

	/**
	 * 获取entry信息
	 */
	EntryInfoData qryEntryInfoData(int entry);

	/**
	 * 根据位置获取球员列表(team_short_name -> player_info)
	 */
	Map<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType);

}

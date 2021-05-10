package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.live.LiveMatchData;

import java.util.List;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiLive {

	/**
	 * 获取实时比赛数据
	 */
	List<LiveMatchData> qryLiveMatchDataByStatus(String playStatus);

}

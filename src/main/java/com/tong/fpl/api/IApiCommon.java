package com.tong.fpl.api;

import java.util.Map;

/**
 * Create by tong on 2021/2/26
 */
public interface IApiCommon {

	/**
	 * 获取当前比赛周和下一比赛周死线
	 */
	Map<String, String> qryCurrentEventAndNextUtcDeadline();

	/**
	 * 刷新event_live
	 */
	void insertEventLive(int event);

}

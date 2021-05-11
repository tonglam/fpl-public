package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.scout.ScoutData;

import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiGroup {

	/**
	 * 获取球探名单
	 */
	Map<String, String> qryScoutEntry();

	/**
	 * 提交球探推荐
	 */
	void upsertEventScout(ScoutData scoutData);

	/**
	 * 更新指定比赛周的球探推荐结果
	 */
	void updateEventScoutResult(int event);

}

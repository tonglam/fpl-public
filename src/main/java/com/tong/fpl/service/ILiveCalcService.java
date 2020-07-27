package com.tong.fpl.service;

import com.tong.fpl.domain.web.LiveCalaData;
import com.tong.fpl.domain.web.PlayerValueData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/7/13
 */
public interface ILiveCalcService {

	/**
	 * calculate entry live points
	 *
	 * @param event event
	 * @param entry entry
	 */
	LiveCalaData calcLivePointsByEntry(int event, int entry);

	/**
	 * calculate entry live points(do not has entry id)
	 *
	 * @param event       event
	 * @param elementMap  elementMap
	 * @param captain     captain
	 * @param viceCaptain viceCaptain
	 */
	LiveCalaData calcLivePointsByElementList(int event, Map<Integer, Integer> elementMap, int captain, int viceCaptain);

	Map<Integer, Integer> calcEventCaptainStat(int event, int num);

	List<PlayerValueData> qryDayChangePlayerValue(String changeDate);

}

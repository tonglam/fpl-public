package com.tong.fpl.service;

import com.tong.fpl.domain.data.fpl.LiveCalaData;

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

}

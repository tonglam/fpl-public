package com.tong.fpl.service;

import java.util.List;

/**
 * Create by tong on 2020/6/29
 */
public interface IUpdateGwResultService {

	/**
	 * calculate event points and save
	 *
	 * @param event     event
	 * @param entryList entryList
	 */
	void upsertEventResult(int event, List<Integer> entryList);

	void updateGroupResult(int event);

	void updateKnockoutResult(int event);

}

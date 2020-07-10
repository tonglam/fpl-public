package com.tong.fpl.service;

import java.util.List;

/**
 * Create by tong on 2020/6/29
 */
public interface IUpdateGwResultService {

	void updateEntryInfo(int event);

	/**
	 * calculate event points and save
	 *
	 * @param event     event
	 * @param entryList entryList
	 */
	void upsertEventResult(int event, List<Integer> entryList);

	void updateGroupResult(int event);

	/**
	 * update tournament_knockout_result every gw;
	 * if round finished:
	 * a.update tournament_kouckout this roind;
	 * b.update next round entry for tournament_kouckout and tournament_knockout_result
	 *
	 * @param event event
	 */
	void updateKnockoutResult(int event);

}

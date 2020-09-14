package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.live.LiveCalaData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/7/13
 */
public interface ILiveService {

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

	/**
	 * calculate entry live points in the tournament
	 *
	 * @param event        event
	 * @param tournamentId tournamentId
	 */
	List<LiveCalaData> calcLivePointsByTournament(int event, int tournamentId);

}

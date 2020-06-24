package com.tong.fpl.api;

import com.tong.fpl.data.fpl.QueryParam;
import com.tong.fpl.data.fpl.TournamentCreateData;
import com.tong.fpl.db.entity.TournamentInfoEntity;

import java.util.List;

/**
 * Create by tong on 2020/6/24
 */
public interface ITournamentManagementApi {

	/**
	 * Create a new tournament
	 *
	 * @param tournamentCreateData data
	 * @return message
	 */
	String createNewTournament(TournamentCreateData tournamentCreateData);

	/**
	 * query tournament_info list by param
	 *
	 * @param param param
	 * @return tournament_info list
	 */
	List<TournamentInfoEntity> queryTournamentInfo(QueryParam param);

	/**
	 * delete tournament by cupName
	 *
	 * @param cupName cupName
	 * @return message
	 */
	String deleteTournamentByCupName(String cupName);

}

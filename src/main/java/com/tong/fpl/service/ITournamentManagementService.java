package com.tong.fpl.service;

import com.tong.fpl.data.fpl.QueryParam;
import com.tong.fpl.data.fpl.TournamentCreateData;
import com.tong.fpl.db.entity.TournamentInfoEntity;

import java.util.List;

/**
 * Create by tong on 2020/6/24
 */
public interface ITournamentManagementService {

	/**
	 * create a new tournament_info record
	 *
	 * @param tournamentCreateData tournamentCreateData
	 * @return message
	 */
	String createNewTournament(TournamentCreateData tournamentCreateData);

	/**
	 * save entry info in the tournament
	 *
	 * @param cupName cupName
	 */
	void saveTournamentEntryInfo(String cupName);

	/**
	 * draw groups
	 *
	 * @param cupName cupName
	 */
	void drawGroups(String cupName);

	/**
	 * draw knockout phase
	 *
	 * @param cupName cupName
	 */
	void drawKnockouts(String cupName) throws Exception;

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

	/**
	 * update qualified teams from group stage
	 *
	 * @param cupName cupName
	 */
	void updateQualifiedTeams(String cupName);

}

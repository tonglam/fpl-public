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
	 * @param tournamentName tournamentName
	 */
	void saveTournamentEntryInfo(String tournamentName);

	/**
	 * draw groups
	 *
	 * @param tournamentName tournamentName
	 */
	void drawGroups(String tournamentName);

	/**
	 * draw knockout phase
	 *
	 * @param tournamentName tournamentName
	 */
	void drawKnockouts(String tournamentName) throws Exception;

	/**
	 * query tournament_info list by param
	 *
	 * @param param param
	 * @return tournament_info list
	 */
	List<TournamentInfoEntity> queryTournamentInfo(QueryParam param);

	/**
	 * delete tournament by tournamentName
	 *
	 * @param tournamentName tournamentName
	 * @return message
	 */
	String deleteTournamentByCupName(String tournamentName);

	/**
	 * update qualified teams from group stage
	 *
	 * @param tournamentName tournamentName
	 */
	void updateQualifiedTeams(String tournamentName);

	/**
	 * count entry number in a tournament
	 *
	 * @return number
	 */
	int countEntryNumInGroup(String tournamentName);

}

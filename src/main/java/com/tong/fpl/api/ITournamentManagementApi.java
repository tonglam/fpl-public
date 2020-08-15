package com.tong.fpl.api;

import com.tong.fpl.domain.data.letletme.QueryParam;
import com.tong.fpl.domain.data.letletme.TournamentCreateData;
import com.tong.fpl.domain.entity.TournamentInfoEntity;

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

    /**
     * count league teams from fpl league url
     *
     * @param url url
     * @return numver
     */
    int countLeagueTeams(String url);

    /**
     * check if tournament name exists
     *
     * @param name name
     * @return retrun
     */
    boolean checkTournamentName(String name);

}

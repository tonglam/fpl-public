package com.tong.fpl.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.domain.letletme.tournament.TournamentCreateData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;

/**
 * Create by tong on 2020/6/24
 */
public interface ITournamentApi {

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
    Page<TournamentInfoData> qryTournamenList(TournamentQueryParam param);

    /**
     * count league teams from fpl league url
     *
     * @param url url
     * @return number
     */
    int countTournamentLeagueTeams(String url);

    /**
     * check if tournament name exists
     *
     * @param name name
     * @return retrun
     */
    boolean checkTournamentName(String name);

    /**
     * update tournament info
     *
     * @param tournamentCreateData tournamentCreateData
     * @return message
     */
    String updateTournament(TournamentCreateData tournamentCreateData);

    /**
     * delete tournament
     *
     * @param name tournament_name
     * @return message
     */
    String deleteTournamentByName(String name);

}

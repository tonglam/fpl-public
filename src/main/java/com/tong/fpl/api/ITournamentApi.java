package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.table.TableData;
import com.tong.fpl.domain.letletme.tournament.*;

import java.util.List;

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
	TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param);

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

	TableData<EntryTournamentData> qryEntryTournamentList(int entry);

	EntryInfoData qryEntryInfoData(int entry);

	TournamentInfoData qryTournamentInfoById(int tournamentId);

	List<TournamentGroupData> qryGroupListByTournamentId(int tournamentId);

	List<TournamentKnockoutData> qryKnockoutListByTournamentId(int tournamentId);

}

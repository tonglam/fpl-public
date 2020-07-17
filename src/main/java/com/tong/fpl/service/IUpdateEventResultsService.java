package com.tong.fpl.service;

/**
 * Create by tong on 2020/6/29
 */
public interface IUpdateEventResultsService {

	/**
	 * update basic informations which could be changed by every gw
	 * run after gw deadline passed
	 *
	 * @param event event
	 */
	void updateBaseInfoByEvent(int event);

	/**
	 * calculate event points and save
	 *
	 * @param event        event
	 * @param tournamentId tournamentId
	 */
	void updateTournamentEntryEventResult(int event, int tournamentId);

	/**
	 * update points race mode group results every gw
	 *
	 * @param event event
	 */
	void updatePointsRaceGroupResult(int event);

	/**
	 * update points race mode group results every gw bt tournament
	 *
	 * @param event event
	 */
	void updatePointsRaceGroupResultByTournament(int event, int tournamentId);

	/**
	 * update points race mode group results every gw
	 *
	 * @param event event
	 */
	void updateBattleRaceGroupResult(int event);

	/**
	 * update points race mode group results every gw bt tournament
	 *
	 * @param event event
	 */
	void updateBattleRaceGroupResultByTournament(int event, int tournamentId);

	/**
	 * update all tournament_knockout_result every gw;
	 * if round finished:
	 * a.update tournament_kouckout this round;
	 * b.update next round entry for tournament_kouckout and tournament_knockout_result
	 *
	 * @param event event
	 */
	void updateKnockoutResult(int event);

	/**
	 * update single tournament_knockout_result every gw;
	 *
	 * @param event        event
	 * @param tournamentId tournamentId
	 */
	void updateKnockoutResultByTournament(int event, int tournamentId);

}

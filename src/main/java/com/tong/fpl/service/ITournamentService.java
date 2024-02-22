package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.tournament.TournamentCreateData;

import java.util.List;

/**
 * Create by tong on 2020/6/24
 */
public interface ITournamentService {

    /**
     * create a new tournament_info record
     * 1.group stage: group number depends on total team and team per group
     * a.no group stage input: mode, group_fill_average==false, draw knockout state immediately
     * b.point_race input: mode, team in group, group_fill_average==false, group_qualifiers, group_start_gw, group_end_gw
     * c.single_round and double_round input: mode, team in group, group_fill_average, group_qualifiers, group_start_gw, rounds would be decided by team in group
     * 2.knockout stage
     * a.single round
     * b.home_away
     * c.no knockout(must have group stage)
     */
    String createNewTournament(TournamentCreateData tournamentCreateData);

    /**
     * new tournament async methods
     * 1.save entry
     * 2.draw groups
     * 3.draw points group result
     * 4.draw battle group result
     * 5.draw knockouts
     * 6.create knockout result records
     */
    void createNewTournamentBackground(int tournamentId, List<Integer> inputEntryList);

    boolean checkTournamentName(String name);

    /**
     * update tournament info
     * editable: creator, adminer_entry
     */
    String updateTournamentInfo(TournamentCreateData tournamentCreateData);

    String deleteTournamentByName(String name);

    /**
     * exist tournament add new entry
     * only use for normal tournament which group mode is points race and no knockout
     * 1.save new entry_info
     * 2.save new tournament_group and tournament_group_result
     * 3.update tournament_points_group_result
     * 4.update league_event_report
     */
    void addTournamentNewEntry(int tournamentId);

    void drawTournamentKnockout(int tournamentId);

    void drawKnockoutCreateManually(String tournamentName, int leagueId, int totalTeam, int playAgainstNum, int startGw, int endGw, int rounds);

    String drawKnockoutSinglePair(int tournamentId, String groupName, int entry, int position);

}

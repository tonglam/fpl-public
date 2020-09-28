package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.tournament.TournamentCreateData;
import com.tong.fpl.domain.letletme.tournament.ZjTournamentCreateData;

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
     * 3.draw group battles
     * 4.drow knockouts
     * 5.create knockout result records
     */
    void createNewTournamentBackground(String tournamentName);

    void saveTournamentEntryInfo(int tournamentId, String leagueType, int leagueId, boolean groupFillAverage);

    void drawGroups(int tournamentId, String groupMode, int teamsPerGroup, boolean groupFillAverage, int groupNum,
                    int groupStartGw, int groupEndGw);

    void drawGroupBattle(int tournamentId, String groupMode, int playAgainstNum, int knockoutTeam, int groupNum, int groupStartGw, int groupEndGw);

    void drawKnockouts(int tournamentId, String groupMode, int groupNum, int groupQualifiers,
                       String knockoutMode, int knockoutPlayAgainstNum, int knockoutTeam,
                       int knockoutStartGw, int knockoutRounds);

    String createNewZjTournament(ZjTournamentCreateData zjTournamentCreateData);

    int countTournamentLeagueTeams(String url);

    boolean checkTournamentName(String name);

    /**
     * update tournament info
     * editable: creator, adminir_entry
     */
    String updateTournament(TournamentCreateData tournamentCreateData);

    String deleteTournamentByName(String name);

    void updateTournamentEntry(int tournamentId);

}

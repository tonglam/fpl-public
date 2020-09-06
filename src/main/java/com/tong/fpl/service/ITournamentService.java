package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.tournament.TournamentCreateData;

/**
 * Create by tong on 2020/6/24
 */
public interface ITournamentService {

    /**
     * create a new tournament_info record
     * <p>
     * 1.group stage: group number depends on total team and team per group
     * a.no group stage input: mode, group_fill_average==false, draw knockout state immediately
     * b.point_race input: mode, team in group, group_fill_average==false, group_qualifiers, group_start_gw, group_end_gw
     * c.single_round and double_round input: mode, team in group, group_fill_average, group_qualifiers, group_start_gw, rounds would be decided by team in group
     * 2.knockout stage
     * a.single round
     * b.home_away
     * c.no knockout(must have group stage)
     *
     * @param tournamentCreateData tournamentCreateData
     * @return message
     */
    String createNewTournament(TournamentCreateData tournamentCreateData);

    /**
     * new tournament async methods
     * 1.save entry
     * 2.draw groups
     * 3.draw group battles
     * 4.drow knockouts
     * 5.create knockout result records
     *
     * @param tournamentName tournamentName
     */
    void createNewTournamentBackground(String tournamentName);

    /**
     * save entry info in the tournament
     *
     * @param tournamentId     tournamentId
     * @param leagueType       leagueType
     * @param leagueId         leagueId
     * @param groupFillAverage groupFillAverage
     */
    void saveTournamentEntryInfo(int tournamentId, String leagueType, int leagueId, boolean groupFillAverage);

    /**
     * draw groups
     *
     * @param tournamentId     tournamentId
     * @param groupMode        groupMode
     * @param teamsPerGroup    teamsPerGroup
     * @param groupFillAverage groupFillAverage
     * @param groupNum         groupNum
     * @param groupStartGw     groupStartGw
     * @param groupEndGw       groupEndGw
     */
    void drawGroups(int tournamentId, String groupMode, int teamsPerGroup, boolean groupFillAverage, int groupNum,
                    int groupStartGw, int groupEndGw);

    /**
     * draw group battle
     *
     * @param tournamentId   tournamentId
     * @param groupMode      groupMode
     * @param playAgainstNum playAgainstNum
     * @param knockoutTeam   knockoutTeam
     * @param groupNum       groupNum
     * @param groupStartGw   groupStartGw
     * @param groupEndGw     groupEndGw
     */
    void drawGroupBattle(int tournamentId, String groupMode, int playAgainstNum, int knockoutTeam, int groupNum, int groupStartGw, int groupEndGw);

    /**
     * draw knockout phase
     *
     * @param tournamentId           tournamentId
     * @param groupMode              groupMode
     * @param groupNum               groupNum
     * @param groupQualifiers        groupQualifiers
     * @param knockoutMode           knockoutMode
     * @param knockoutPlayAgainstNum knockoutPlayAgainstNum
     * @param knockoutTeam           knockoutTeam
     * @param knockoutStartGw        knockoutStartGw
     * @param knockoutRounds         knockoutRounds
     */
    void drawKnockouts(int tournamentId, String groupMode, int groupNum, int groupQualifiers,
                       String knockoutMode, int knockoutPlayAgainstNum, int knockoutTeam,
                       int knockoutStartGw, int knockoutRounds);

    /**
     * count league teams from fpl league url
     *
     * @param url url
     * @return numver
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
     * editable: creator, adminir_entry
     *
     * @param tournamentCreateData tournamentCreateData
     * @return message
     */
    String updateTournament(TournamentCreateData tournamentCreateData);

    /**
     * delete tournament by tournament id
     *
     * @param name tournament_name
     * @return message
     */
    String deleteTournamentByName(String name);

}

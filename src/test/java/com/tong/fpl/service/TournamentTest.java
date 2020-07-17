package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.fpl.TournamentCreateData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by tong on 2020/7/15
 */
public class TournamentTest extends FplApplicationTests {

	@Autowired
	private ITournamentManagementService tournamentManagementService;

	@Test
	void createNewTournament() {
		try {
			TournamentCreateData tournamentCreateData = new TournamentCreateData();
			tournamentCreateData.setUrl("https://fantasy.premierleague.com/leagues/710/standings/c");
			tournamentCreateData.setTournamentName("letletme3");
			tournamentCreateData.setCreator("tong");
			// group
			tournamentCreateData.setGroupMode("Battle_race");
			tournamentCreateData.setGroupPlayAgainstNum(1);
			tournamentCreateData.setGroupStartGw("1");
			tournamentCreateData.setGroupEndGw("");
			tournamentCreateData.setTeamsPerGroup(7);
			tournamentCreateData.setGroupFillAverage(true);
			tournamentCreateData.setGroupQualifiers(4);
			// knockout
			tournamentCreateData.setKnockoutMode("Single_round");
			tournamentCreateData.setKnockoutRounds(0);
			tournamentCreateData.setKnockoutStartGw("");
			String result = this.tournamentManagementService.createNewTournament(tournamentCreateData);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@ParameterizedTest
	@CsvSource({"letletme3"})
	void createNewTournamentBackground(String name) {
		this.tournamentManagementService.createNewTournamentBackground(name);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1, Classic, 710"})
	void saveEntryInfo(int tournamentId, String leagueType, int leagueId) {
		this.tournamentManagementService.saveTournamentEntryInfo(tournamentId, leagueType, leagueId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"2, Points_race, 107, false, 1, 1, 47"})
	void drawGroups(int tournamentId, String groupMode, int teamsPerGroup, boolean groupFillAverage, int groupNum,
	                int groupStartGw, int groupEndGw) {
		this.tournamentManagementService.drawGroups(tournamentId, groupMode, teamsPerGroup, groupFillAverage, groupNum,
				groupStartGw, groupEndGw);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1, No_group, 2, 64, 16, 1"})
	void drawGroupBattle(int tournamentId, String groupMode, int playAgainstNum, int knockoutTeam, int groupNum, int groupStartGw) {
		this.tournamentManagementService.drawGroupBattle(tournamentId, groupMode, playAgainstNum, knockoutTeam, groupNum, groupStartGw);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1, No_group, 16, Single_round, 2, 1, 107, 39, 7"})
	void drawKnockouts(int tournamentId, String groupMode, int groupNum, int groupQualifiers,
	                   String knockoutMode, int knockoutPlayAgainstNum, int knockoutTeam, int knockoutStartGw, int knockoutRounds) {
		this.tournamentManagementService.drawKnockouts(tournamentId, groupMode, groupNum, groupQualifiers,
				knockoutMode, knockoutPlayAgainstNum, knockoutTeam, knockoutStartGw, knockoutRounds);
		System.out.println(1);
	}

}

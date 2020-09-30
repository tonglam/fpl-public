package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.domain.letletme.tournament.TournamentCreateData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by tong on 2020/7/15
 */
public class TournamentTest extends FplApplicationTests {

    @Autowired
    private ITournamentService tournamentManagementService;

	@Test
	void createNewTournament() {
		try {
			TournamentCreateData tournamentCreateData = new TournamentCreateData();
			tournamentCreateData.setUrl("https://fantasy.premierleague.com/leagues/65/standings/c");
			tournamentCreateData.setCreator("tong");
			tournamentCreateData.setTournamentName("points-china");
			this.configCreateData("classic", tournamentCreateData);
			String result = this.tournamentManagementService.createNewTournament(tournamentCreateData);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void configCreateData(String tournamnetMode, TournamentCreateData tournamentCreateData) {
		switch (tournamnetMode) {
			case "FA cup": {
				tournamentCreateData.setGroupMode(GroupMode.No_group.name()).setKnockoutMode(KnockoutMode.Single_round.name());
				tournamentCreateData.setKnockoutStartGw(39);
				break;
			}
			case "FA cup home-away": {
				tournamentCreateData.setGroupMode(GroupMode.No_group.name()).setKnockoutMode(KnockoutMode.Home_away.name());
				tournamentCreateData.setKnockoutStartGw(39);
				break;
			}
			case "classic": {
				tournamentCreateData.setGroupMode(GroupMode.Points_race.name()).setKnockoutMode(KnockoutMode.No_knockout.name());
				tournamentCreateData.setTeamsPerGroup(0).setGroupStartGw(1).setGroupEndGw(47).setGroupQualifiers(1000);
				break;
			}
			case "h2h": {
				tournamentCreateData.setGroupMode(GroupMode.Battle_race.name()).setKnockoutMode(KnockoutMode.No_knockout.name());
				tournamentCreateData.setTeamsPerGroup(0).setGroupStartGw(1).setGroupEndGw(47).setGroupQualifiers(0);
				break;
			}
			case "points world cup": {
				tournamentCreateData.setGroupMode(GroupMode.Points_race.name()).setKnockoutMode(KnockoutMode.Single_round.name());
				tournamentCreateData.setTeamsPerGroup(7).setGroupStartGw(1).setGroupEndGw(47).setGroupQualifiers(4);
				break;
			}
			case "points world cup home-away": {
				tournamentCreateData.setGroupMode(GroupMode.Points_race.name()).setKnockoutMode(KnockoutMode.Home_away.name());
				tournamentCreateData.setTeamsPerGroup(7).setGroupStartGw(1).setGroupEndGw(47).setGroupQualifiers(4);
				break;
			}
			case "world cup": {
				tournamentCreateData.setGroupMode(GroupMode.Battle_race.name()).setKnockoutMode(KnockoutMode.Single_round.name());
				break;
			}
			case "world cup home-away": {
				tournamentCreateData.setGroupMode(GroupMode.Battle_race.name()).setKnockoutMode(KnockoutMode.Home_away.name());
				break;
			}
			default:
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
		this.tournamentManagementService.saveTournamentEntryInfo(tournamentId, leagueType, leagueId, false);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"2, Points_race, 107, false, 1, 1, 47"})
	void drawGroups(int tournamentId, String groupMode, int teamsPerGroup, boolean groupFillAverage,
	                int groupNum, int groupStartGw, int groupEndGw) {
		this.tournamentManagementService.drawGroups(tournamentId, groupMode, teamsPerGroup, groupFillAverage,
				groupNum, groupStartGw, groupEndGw);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1, No_group, 2, 64, 16, 1, 38"})
	void drawGroupBattle(int tournamentId, String groupMode, int playAgainstNum, int knockoutTeam,
	                     int groupNum, int groupStartGw, int groupEndGw) {
		this.tournamentManagementService.drawGroupBattle(tournamentId, groupMode, playAgainstNum, knockoutTeam,
				groupNum, groupStartGw, groupEndGw);
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

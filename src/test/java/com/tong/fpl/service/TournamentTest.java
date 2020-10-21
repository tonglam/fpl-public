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
	private ITournamentService tournamentService;

	@Test
	void createNewTournament() {
		try {
			TournamentCreateData tournamentCreateData = new TournamentCreateData();
			tournamentCreateData.setUrl("https://fantasy.premierleague.com/leagues/65/standings/c");
			tournamentCreateData.setCreator("tong");
			tournamentCreateData.setTournamentName("points-china");
			this.configCreateData("classic", tournamentCreateData);
			String result = this.tournamentService.createNewTournament(tournamentCreateData);
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
				tournamentCreateData.setTeamPerGroup(0).setGroupStartGw(1).setGroupEndGw(47).setGroupQualifiers(1000);
				break;
			}
			case "h2h": {
				tournamentCreateData.setGroupMode(GroupMode.Battle_race.name()).setKnockoutMode(KnockoutMode.No_knockout.name());
				tournamentCreateData.setTeamPerGroup(0).setGroupStartGw(1).setGroupEndGw(47).setGroupQualifiers(0);
				break;
			}
			case "points world cup": {
				tournamentCreateData.setGroupMode(GroupMode.Points_race.name()).setKnockoutMode(KnockoutMode.Single_round.name());
				tournamentCreateData.setTeamPerGroup(7).setGroupStartGw(1).setGroupEndGw(47).setGroupQualifiers(4);
				break;
			}
			case "points world cup home-away": {
				tournamentCreateData.setGroupMode(GroupMode.Points_race.name()).setKnockoutMode(KnockoutMode.Home_away.name());
				tournamentCreateData.setTeamPerGroup(7).setGroupStartGw(1).setGroupEndGw(47).setGroupQualifiers(4);
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
		this.tournamentService.createNewTournamentBackground(name);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"2"})
	void addTournamentNewEntry(int tournamentId) {
		String a = this.tournamentService.addTournamentNewEntry(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4, 24755, 2138108, 24755"})
	void updateZjTournamentPkData(int tournamentId, int entry, int pkEntry, int captainEntry) {
		this.tournamentService.updateZjTournamentPkData(tournamentId, entry, pkEntry, captainEntry);
		System.out.println(1);
	}

}

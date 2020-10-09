package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by tong on 2020/7/15
 */
public class UpdateEventResultTest extends FplApplicationTests {

	@Autowired
	private IUpdateEventResultService updateEventResultsService;

	@Test
	void updateEntryInfo() {
		this.updateEventResultsService.updateEntryInfo();
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4, 2"})
	void updateTournamentEntryEventResult(int event, int tournament) {
		this.updateEventResultsService.updateTournamentEntryEventResult(event, tournament);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4, 3"})
	void updatePointsRaceGroupResult(int event, int tournamentId) {
		this.updateEventResultsService.updatePointsRaceGroupResult(event, tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4, 5"})
	void updateBattleRaceGroupResult(int event, int tournamentId) {
		this.updateEventResultsService.updateBattleRaceGroupResult(event, tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"45", "1"})
	void updateKnockoutResult(int event, int tournamentId) {
		this.updateEventResultsService.updateKnockoutResult(event, tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4, 4"})
	void updateZjPhaseOneResult(int event, int tournamentId) {
		this.updateEventResultsService.updateZjPhaseOneResult(event, tournamentId);
		System.out.println(1);
	}

}

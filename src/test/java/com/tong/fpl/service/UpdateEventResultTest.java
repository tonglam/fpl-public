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
	@CsvSource({"9, 8"})
	void updateTournamentEntryEventResult(int tournament, int event) {
		this.updateEventResultsService.updateTournamentEntryEventResult(event, tournament);
		System.out.println("event: " + event + ", update finished!");
	}

	@ParameterizedTest
	@CsvSource({"9, 8"})
	void updatePointsRaceGroupResult(int tournamentId, int event) {
		this.updateEventResultsService.updatePointsRaceGroupResult(event, tournamentId);
		System.out.println("event: " + event + ", update finished!");
	}

	@ParameterizedTest
	@CsvSource({"5, 8"})
	void updateBattleRaceGroupResult(int tournamentId, int event) {
		this.updateEventResultsService.updateBattleRaceGroupResult(event, tournamentId);
		System.out.println("event: " + event + ", update finished!");
	}

	@ParameterizedTest
	@CsvSource({"6, 7"})
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

	@ParameterizedTest
	@CsvSource({"5, 4"})
	void updateZjPhaseTwoResult(int event, int tournamentId) {
		this.updateEventResultsService.updateZjPhaseTwoResult(event, tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"6, 4"})
	void updateZjPkResult(int event, int tournamentId) {
		this.updateEventResultsService.updateZjPkResult(event, tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4"})
	void updateZjTournamentResult(int tournamentId) {
		this.updateEventResultsService.updateZjTournamentResult(tournamentId);
		System.out.println(1);
	}

}

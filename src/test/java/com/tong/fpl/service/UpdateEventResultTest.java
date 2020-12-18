package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.IntStream;

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
	@CsvSource({"7, 9"})
	void upsertTournamentEntryEventResult(int event, int tournament) {
		this.updateEventResultsService.upsertTournamentEntryEventResult(event, tournament);
		System.out.println("event: " + event + ", update finished!");
	}

	@ParameterizedTest
	@CsvSource({"9"})
	void insertTournamentEntryEventTransfer(int tournament) {
		this.updateEventResultsService.insertTournamentEntryEventTransfer(tournament);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"9"})
	void updateTournamentEventTransferPlayed(int tournament) {
		IntStream.rangeClosed(1, 13).forEach(event -> {
			this.updateEventResultsService.updateTournamentEventTransferPlayed(event, tournament);
			System.out.println("event: " + event + ", update finished!");
		});
	}

	@ParameterizedTest
	@CsvSource({"7, 9"})
	void updatePointsRaceGroupResult(int event, int tournamentId) {
		this.updateEventResultsService.updatePointsRaceGroupResult(event, tournamentId);
		System.out.println("event: " + event + ", update finished!");
	}

	@ParameterizedTest
	@CsvSource({"7, 5"})
	void updateBattleRaceGroupResult(int event, int tournamentId) {
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

package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.IntStream;

/**
 * Create by tong on 2020/7/15
 */
public class UpdateEventResultTest extends FplApplicationTests {

	@Autowired
	private IUpdateEventResultsService updateEventResultsService;

	static IntStream intStreamProvider() {
		return IntStream.range(1, 48);
	}

	@Test
	void updateEntryInfo() {
		this.updateEventResultsService.updateEntryInfo();
		System.out.println(1);
	}

	@ParameterizedTest
//	@MethodSource("intStreamProvider")
    @CsvSource({"2, 3"})
	void updateTournamentEntryEventResult(int event, int tournament) {
		System.out.println("start event: " + event);
		this.updateEventResultsService.updateTournamentEntryEventResult(event, tournament);
		System.out.println(1);
	}

    @ParameterizedTest
//	@MethodSource("intStreamProvider")
    @CsvSource({"2"})
	void updatePointsRaceGroupResult(int event) {
		this.updateEventResultsService.updatePointsRaceGroupResult(event);
		System.out.println("event: " + event);
	}

	@ParameterizedTest
//	@MethodSource("intStreamProvider")
	@CsvSource({"1"})
	void updateBattleRaceGroupResult(int event) {
		this.updateEventResultsService.updateBattleRaceGroupResult(event);
		System.out.println("event: " + event);
	}

	@ParameterizedTest
	@MethodSource("intStreamProvider")
	@ValueSource(ints = {45})
	void updateKnockoutResult(int event) {
		this.updateEventResultsService.updateKnockoutResult(event);
		System.out.println(1);
	}

}

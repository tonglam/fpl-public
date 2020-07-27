package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.IntStream;

/**
 * Create by tong on 2020/7/15
 */
public class EventResultTest extends FplApplicationTests {

	@Autowired
	private IUpdateEventResultsService updateEventResultsService;

	static IntStream intStreamProvider() {
		return IntStream.range(1, 47);
	}

	@ParameterizedTest
//	@MethodSource("intStreamProvider")
	@CsvSource({"47"})
	void updateBaseInfoByEvent(int event) {
		this.updateEventResultsService.updateBaseInfoByEvent(event);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"46, 1"})
	void updateEventResult(int event, int tournamentId) {
		System.out.println("start event: " + event);
		this.updateEventResultsService.updateTournamentEntryEventResult(event, tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@MethodSource("intStreamProvider")
//	@CsvSource({"1"})
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

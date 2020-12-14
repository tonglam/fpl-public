package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutEventFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class QueryTest extends FplApplicationTests {

	@Autowired
	private IQuerySerivce querySerivce;

	@ParameterizedTest
	@CsvSource({"2021, 1, 1870"})
	void qryEntryEvent(String season, int event, int entry) {
		EntryEventResultData entryEventResultData = this.querySerivce.qryEntryEventResult(season, event, entry);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"483"})
	void qryPlayerData(int element) {
		PlayerData playerData = this.querySerivce.qryPlayerData(element);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"2021"})
	void qryAllPlayers(String season) {
		List<PlayerInfoData> list = this.querySerivce.qryAllPlayers(season);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"5"})
	void qryGroupFixtureListById(int tournamentId) {
		List<TournamentGroupFixtureData> list = this.querySerivce.qryGroupFixtureListById(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"6"})
	void qryKnockoutFixtureListById(int tournamentId) {
		List<TournamentKnockoutFixtureData> list = this.querySerivce.qryKnockoutFixtureListById(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"6"})
	void qryKnockoutResultByTournament(int tournamentId) {
		List<TournamentKnockoutResultData> list = this.querySerivce.qryKnockoutResultByTournament(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4"})
	void getPlayerByElememt(int element) {
		long start = System.currentTimeMillis();
		this.querySerivce.getPlayerByElement(element);
		long end = System.currentTimeMillis();
		System.out.println("escape: " + (end - start) + "ms!");
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1"})
	void getMatchDayByEvent(int event) {
		List<LocalDate> list = this.querySerivce.getMatchDayByEvent(event);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"2"})
	void getMatchDayTimeByEvent(int event) {
		List<LocalDateTime> list = this.querySerivce.getMatchDayTimeByEvent(event);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"12"})
	void isMatchDay(int event) {
		boolean a = this.querySerivce.isMatchDay(event);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"12"})
	void isMatchDayTime(int event) {
		boolean a = this.querySerivce.isMatchDayTime(event);
		System.out.println(1);
	}

	@Test
	void isLastMatchDayByEvent() {
		int event = this.querySerivce.getCurrentEvent();
		boolean a = this.querySerivce.isLastMatchDay(event);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4"})
	void qryZjTournamentPhaseOneRankMap(int tournamentId) {
		Map<String, Integer> map = this.querySerivce.qryZjTournamentPhaseOneRankMap(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4"})
	void qryZjTournamentPhaseTwoGroupPointsMap(int tournamentId) {
		Map<String, Integer> map = this.querySerivce.qryZjTournamentPhaseTwoGroupPointsMap(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4"})
	void qryZjTournamentPhaseTwoRankMap(int tournamentId) {
		Map<String, Integer> map = this.querySerivce.qryZjTournamentPhaseTwoRankMap(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4"})
	void qryZjTournamentPkGroupPointsMap(int tournamentId) {
		Map<String, Integer> map = this.querySerivce.qryZjTournamentPkGroupPointsMap(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4"})
	void qryZjTournamentPkRankMap(int tournamentId) {
		Map<String, Integer> map = this.querySerivce.qryZjTournamentPkRankMap(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4"})
	void qryZjPkPickListById(int tournamentId) {
		List<TournamentKnockoutEventFixtureData> list = this.querySerivce.qryZjPkPickListById(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4"})
	void qryZjTournamentPkResultByTournament(int tournamentId) {
		List<TournamentKnockoutResultData> list = this.querySerivce.qryZjTournamentPkResultByTournament(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"6"})
	void qryKnockoutBracketResultByTournament(int tournamentId) {
		KnockoutBracketData knockoutBracketData = this.querySerivce.qryKnockoutBracketResultByTournament(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"8"})
	void getDeadlineByEvent(int event) {
		String deadline = this.querySerivce.getDeadlineByEvent(event).replace(" ", "T");
		LocalDateTime localDateTime = LocalDateTime.parse(deadline).minusHours(1);
		System.out.println(LocalDateTime.now().equals(localDateTime));
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource("12")
	void getScoutDeadlineByEvent(int event) {
		String a = this.querySerivce.getScoutDeadlineByEvent(event);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"65, Classic"})
	void qryLeagueNameByIdAndType(int leagueId, String leagueType) {
		String a = this.querySerivce.qryLeagueNameByIdAndType(leagueId, leagueType);
		System.out.println(1);
	}


}

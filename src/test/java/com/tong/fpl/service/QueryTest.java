package com.tong.fpl.service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.constant.enums.TournamentMode;
import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutEventFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import org.apache.commons.lang3.StringUtils;
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
		this.querySerivce.getPlayerByElememt(element);
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
	@CsvSource({"2"})
	void isMatchDay(int event) {
		boolean a = this.querySerivce.isMatchDay(event);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"3"})
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
	@CsvSource({"2, 1870"})
	void qryEntryEventCaptainDataList(int event, int entry) {
		EntryEventCaptainData data = this.querySerivce.qryEntryEventCaptainDataList(event, entry);
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

	@Test
	void a() {
		Table<Integer, String, Integer> table = HashBasedTable.create();
		this.querySerivce.qryAllTournamentList().forEach(o -> {
			if (StringUtils.equals(TournamentMode.Normal.name(), o.getTournamentMode()) && !table.containsRow(o.getLeagueId())) {
				table.put(o.getLeagueId(), o.getLeagueType(), 0);
			}
		});
		// China
		table.put(65, "Classic", 0);
		// Overall top 10k
		table.put(314, "Classic", 10000);
		table.rowKeySet().forEach(leagueId -> {
			String leagueType = table.row(leagueId).keySet()
					.stream()
					.findFirst()
					.orElse("");
			if (StringUtils.isEmpty(leagueType)) {
				return;
			}
			int limit = table.row(leagueId).values()
					.stream()
					.findFirst()
					.orElse(0);
			System.out.println(leagueType + "-" + limit);
		});
		System.out.println(1);
	}

}

package com.tong.fpl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.letletme.entry.EntryEventAutoSubsData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.player.*;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutEventFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import com.tong.fpl.service.db.EntryEventResultService;
import org.assertj.core.util.Lists;
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
	private IQueryService querySerivce;
	@Autowired
	private EntryEventResultService entryEventResultService;

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
	@CsvSource({"16, 0, 5"})
	void qryPlayerFixtureList(int teamId, int previous, int next) {
		List<PlayerFixtureData> list = this.querySerivce.qryPlayerFixtureList(teamId, previous, next);
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
	@CsvSource({"11"})
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
	@CsvSource("15")
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

	@Test
	void qryPickListByPosition() {
		String picks = "[{\"element\":383,\"position\":1,\"multiplier\":1,\"points\":3,\"captain\":false,\"viceCaptain\":false},{\"element\":375,\"position\":2,\"multiplier\":1,\"points\":1,\"captain\":false,\"viceCaptain\":false},{\"element\":259,\"position\":3,\"multiplier\":1,\"points\":1,\"captain\":false,\"viceCaptain\":false},{\"element\":353,\"position\":4,\"multiplier\":1,\"points\":0,\"captain\":false,\"viceCaptain\":false},{\"element\":200,\"position\":5,\"multiplier\":1,\"points\":3,\"captain\":false,\"viceCaptain\":false},{\"element\":254,\"position\":6,\"multiplier\":1,\"points\":20,\"captain\":false,\"viceCaptain\":false},{\"element\":338,\"position\":7,\"multiplier\":1,\"points\":3,\"captain\":false,\"viceCaptain\":false},{\"element\":390,\"position\":8,\"multiplier\":1,\"points\":2,\"captain\":false,\"viceCaptain\":true},{\"element\":500,\"position\":9,\"multiplier\":1,\"points\":2,\"captain\":false,\"viceCaptain\":false},{\"element\":4,\"position\":10,\"multiplier\":2,\"points\":7,\"captain\":true,\"viceCaptain\":false},{\"element\":506,\"position\":11,\"multiplier\":1,\"points\":8,\"captain\":false,\"viceCaptain\":false},{\"element\":35,\"position\":12,\"multiplier\":0,\"points\":0,\"captain\":false,\"viceCaptain\":false},{\"element\":262,\"position\":13,\"multiplier\":0,\"points\":0,\"captain\":false,\"viceCaptain\":false},{\"element\":27,\"position\":14,\"multiplier\":0,\"points\":0,\"captain\":false,\"viceCaptain\":false},{\"element\":50,\"position\":15,\"multiplier\":0,\"points\":0,\"captain\":false,\"viceCaptain\":false}]";
		PlayerPickData playerData = this.querySerivce.qryPickListByPosition(picks);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"363, 17"})
	void qryPlayerShowData(int event, int element) {
		PlayerShowData data = this.querySerivce.qryPlayerShowData(event, element);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"16, 4074865"})
	void qryEntryPickData(int event, int entry) {
		PlayerPickData data = this.querySerivce.qryEntryPickData(event, entry);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"14, 3571, Classic"})
	void qryLeagueEventEoMap(int event, int leagueId, String leagueType) {
		Map<Integer, String> map = this.querySerivce.qryLeagueEventEoMap(event, leagueId, leagueType);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"3571, Classic"})
	void qryLeaguePickDataList(int leagueId, String leagueType) {
		long start = System.currentTimeMillis();
		List<PlayerPickData> list = this.querySerivce.qryLeaguePickDataList(leagueId, leagueType, Lists.newArrayList());
		long end = System.currentTimeMillis();
		System.out.println("escaped: " + (end - start));
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"16, 3571, Classic"})
	void qryLeagueEventPickDataList(int event, int leagueId, String leagueType) {
		List<PlayerPickData> list = this.querySerivce.qryLeagueEventPickDataList(event, leagueId, leagueType);
		System.out.println(1);
	}

	@Test
	void qryAutoSubListFromAutoSubs() {
		EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEntry, 1870)
				.eq(EntryEventResultEntity::getEvent, 16));
		List<EntryEventAutoSubsData> list = this.querySerivce.qryAutoSubListFromAutoSubs(entryEventResultEntity.getEventAutoSubs());
		System.out.println(1);
	}

	@Test
	void qryPickListFromPicks() {
		EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEntry, 1870)
				.eq(EntryEventResultEntity::getEvent, 16));
		List<EntryPickData> list = this.querySerivce.qryPickListFromPicks(entryEventResultEntity.getEventPicks());
		System.out.println(1);
	}

}

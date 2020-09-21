package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupFixtureData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutFixtureData;
import com.tong.fpl.service.db.PlayerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QueryTest extends FplApplicationTests {

	@Autowired
	private IQuerySerivce querySerivce;
	@Autowired
	private PlayerService playerService;

	@Test
	void test() {
		long startTime = System.currentTimeMillis();
		IntStream.range(1, 21).forEach(teamId -> {
//			String name = this.querySerivce.qryTeamPropertyById("2021", teamId, "name");
		});
		long endTime = System.currentTimeMillis();
		System.out.println("esaped: " + (endTime - startTime) + "ms");
	}

	@ParameterizedTest
	@CsvSource({"1920, 1, 1404"})
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
	@CsvSource({"2"})
	void qryGroupFixtureListById(int tournamentId) {
		List<TournamentGroupFixtureData> list = this.querySerivce.qryGroupFixtureListById(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"2"})
	void qryKnockoutFixtureListById(int tournamentId) {
		List<TournamentKnockoutFixtureData> list = this.querySerivce.qryKnockoutFixtureListById(tournamentId);
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

	@Test
	void getAllPlayer() {
		long start = System.currentTimeMillis();
		Map<Integer, PlayerEntity> map = this.playerService.list().stream().collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
		long end = System.currentTimeMillis();
		System.out.println("escape: " + (end - start) + "ms!");
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1"})
	void getMatchDayByEvent(int event) {
		List<String> list = this.querySerivce.getMatchDayByEvent(event);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"2"})
	void isMatchDay(int event) {
		boolean a = this.querySerivce.isMatchDay(event);
		System.out.println(1);
	}

}

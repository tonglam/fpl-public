package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import com.tong.fpl.domain.letletme.tournament.TournamentBattleGroupEventResultData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupEventChampionData;
import com.tong.fpl.domain.letletme.tournament.ZjTournamentResultData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

public class TableQueryTest extends FplApplicationTests {

	@Autowired
	private ITableQueryService tableQueryService;

	@ParameterizedTest
	@CsvSource({"1920, 1, 2, 20"})
	void qryEntryInfoByTournament(String season, int tournamentId) {
		this.tableQueryService.qryEntryInfoByTournament(season, tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1870"})
	void qryEntryLivePoints(int entry) {
		TableData<LiveCalaData> liveCalaDataTableData = this.tableQueryService.qryEntryLivePoints(entry);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"Overall, 1"})
	void qryTeamSelectStatByName(String leagueName, int event) {
		TableData<LeagueStatData> leagueStatData = this.tableQueryService.qryTeamSelectStatByName(leagueName, event);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"7"})
	void qryLiveFixturePlayerList(int teamId) {
		TableData<ElementEventResultData> data = this.tableQueryService.qryLiveFixturePlayerList(teamId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4"})
	void qryZjTournamentResultById(int tournamentId) {
		TableData<ZjTournamentResultData> data = this.tableQueryService.qryZjTournamentResultById(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"4, 2"})
	void qryZjTournamentPkPickableList(int tournamentId, int currentGroupId) {
		TableData<TournamentGroupData> data = this.tableQueryService.qryZjTournamentPkPickableList(tournamentId, currentGroupId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1870"})
	void qryEntryResultList(int entry) {
		TableData<EntryEventResultData> data = this.tableQueryService.qryEntryResultList(entry);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"8"})
	void qryPointsGroupChampion(int tournamentId) {
		TableData<TournamentGroupEventChampionData> data = this.tableQueryService.qryPointsGroupChampion(tournamentId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"5, 1, 1466060, 1, 20"})
	void qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
		TableData<TournamentBattleGroupEventResultData> data = this.tableQueryService.qryPageBattleGroupResult(tournamentId, groupId, entry, page, limit);
		System.out.println(1);
	}

}

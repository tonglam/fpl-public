package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportStatData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.PlayerShowData;
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
	@CsvSource({"1870"})
	void qryEntryLivePoints(int entry) {
		TableData<LiveCalaData> liveCalaDataTableData = this.tableQueryService.qryEntryLivePoints(entry);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1, Overall"})
	void qryTeamSelectStatByName(int event, String leagueName) {
		TableData<LeagueStatData> leagueStatData = this.tableQueryService.qryTeamSelectStatByName(event, leagueName);
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

	@ParameterizedTest
	@CsvSource({"0"})
	void qryLiveMatchList(int statusId) {
		TableData<LiveMatchTeamData> data = this.tableQueryService.qryLiveTeamDataList(statusId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"3571, Classic"})
	void qryLeagueCaptainReportStat(int leagueId, String leagueType) {
		TableData<LeagueEventReportStatData> data = this.tableQueryService.qryLeagueCaptainReportStat(leagueId, leagueType);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"14, 3571, Classic"})
	void qryLeagueCaptainEventReportList(int event, int leagueId, String leagueType) {
		TableData<LeagueEventReportData> data = this.tableQueryService.qryLeagueCaptainEventReportList(event, leagueId, leagueType);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"3571, Classic"})
	void qryLeagueTransferReportStat(int leagueId, String leagueType) {
		TableData<LeagueEventReportStatData> data = this.tableQueryService.qryLeagueTransferReportStat(leagueId, leagueType);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"14, 3571, Classic"})
	void qryLeagueTransferEventReportList(int event, int leagueId, String leagueType) {
		TableData<LeagueEventReportData> data = this.tableQueryService.qryLeagueTransferEventReportList(event, leagueId, leagueType);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"3571, Classic, 1870"})
	void qryEntryEventReportList(int leagueId, String leagueType, int entry) {
		TableData<LeagueEventReportData> data = this.tableQueryService.qryEntryCaptainEventReportList(leagueId, leagueType, entry);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"8, 1"})
	void qryGroupInfoListByGroupId(int tournamentId, int groupId) {
		TableData<TournamentGroupData> data = this.tableQueryService.qryGroupInfoListByGroupId(tournamentId, groupId);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1"})
	void qryPlayerShowListByElementType(int element) {
		TableData<PlayerShowData> data = this.tableQueryService.qryPlayerShowListByElementType(element);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"13, 4074865"})
	void qryEntryPlayerShowList(int event, int entry) {
		TableData<PlayerShowData> data = this.tableQueryService.qryEntryEventPlayerShowList(event, entry);
		System.out.println(1);
	}


}

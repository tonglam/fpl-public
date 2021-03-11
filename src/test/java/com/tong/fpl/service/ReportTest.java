package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class ReportTest extends FplApplicationTests {

	@Autowired
	private IReportService reportService;

	@ParameterizedTest
	@CsvSource({"21, 314, Classic, 10000"})
	void insertLeagueEventSelectStat(int event, int leagueId, String leagueType, int limit) {
		this.reportService.insertLeagueEventSelect(event, leagueId, leagueType, limit);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"27, 314, Classic"})
	void updateLeagueEventResultStat(int event, int leagueId, String leagueType) {
		this.reportService.updateLeagueEventResult(event, leagueId, leagueType);
		System.out.println("event: " + event + ", update finished!");
	}

	@ParameterizedTest
	@CsvSource({"27, 65, Classic, 100"})
	void calcEventStat(int event, int leagueId, String leagueType, int topNum) {
		Map<String, Object> map = this.reportService.calcEventStat(event, leagueId, leagueType, topNum);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"1, 13"})
	void insertEntryLeagueEventSelectByTournament(int event, int tournamentId) {
		this.reportService.insertEntryLeagueEventSelectByTournament(event, tournamentId);
		System.out.println(1);
	}

}

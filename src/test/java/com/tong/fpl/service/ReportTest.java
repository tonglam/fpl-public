package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportTest extends FplApplicationTests {

	@Autowired
	private IReportService reportService;

	@ParameterizedTest
	@CsvSource({"10, 65, Classic, 0"})
	void insertLeagueEventSelectStat(int event, int leagueId, String leagueType, int limit) {
		this.reportService.insertLeagueEventSelect(event, leagueId, leagueType, limit);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"10, 65, Classic"})
	void updateLeagueEventResultStat(int event, int leagueId, String leagueType) {
		this.reportService.updateLeagueEventResult(event, leagueId, leagueType);
		System.out.println(1);
	}

}

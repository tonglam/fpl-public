package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.stream.IntStream;

public class ReportTest extends FplApplicationTests {

	@Autowired
	private IReportService reportService;

	@ParameterizedTest
	@CsvSource({"8, 11316, Classic, 0"})
	void insertLeagueEventSelectStat(int event, int leagueId, String leagueType, int limit) {
		this.reportService.insertLeagueEventSelect(event, leagueId, leagueType, limit);
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"3571, Classic"})
	void updateLeagueEventResultStat(int leagueId, String leagueType) {
		IntStream.rangeClosed(1, 11).forEach(event -> {
			this.reportService.updateLeagueEventResult(event, leagueId, leagueType);
			System.out.println("event: " + event + ", update finished!");
		});
	}

	@ParameterizedTest
	@CsvSource({"11, 11316, Classic, 1000"})
	void calcEventStat(int event, int leagueId, String leagueType, int topNum) {
		Map<String, Object> map = this.reportService.calcEventStat(event, leagueId, leagueType, topNum);
		System.out.println(1);
	}

}

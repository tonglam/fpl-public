package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportTest extends FplApplicationTests {

	@Autowired
	private IReportService reportService;

	@ParameterizedTest
	@CsvSource({"1920, 1"})
	void insertEntryCaptainStat(String season, int tournamentId) {
		this.reportService.insertEntryCaptainStat(season, tournamentId);
		System.out.println(1);
	}


}

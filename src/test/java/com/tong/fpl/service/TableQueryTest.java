package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

public class TableQueryTest extends FplApplicationTests {

	@Autowired
	private ITableQueryService tableQueryService;

	@ParameterizedTest
	@CsvSource({"1920, 1, 2, 20"})
	void qryEntryInfoByTournament(String season, int tournamentId, long page, long limit) {
		this.tableQueryService.qryPageEntryInfoByTournament(season, tournamentId, page, limit);
		System.out.println(1);
	}


}

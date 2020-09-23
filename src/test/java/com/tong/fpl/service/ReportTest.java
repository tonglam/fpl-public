package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportTest extends FplApplicationTests {

    @Autowired
    private IReportService reportService;


    @ParameterizedTest
    @CsvSource({"2, Classic, 314, 10000"})
    void inertTeamSelectStat(int event, String leagueType, int leagueId, int limit) {
        this.reportService.inertTeamSelectStat(event, leagueType, leagueId, limit);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2, Classic, 3571, 0"})
    void insertLeagueResultStat(int event, String leagueType, int leagueId, int limit) {
        long startTime = System.currentTimeMillis();
        this.reportService.insertLeagueResultStat(event, leagueType, leagueId, limit);
        long endTime = System.currentTimeMillis();
        System.out.println("escape: " + (endTime - startTime) + "ms");
        System.out.println(1);
    }

}

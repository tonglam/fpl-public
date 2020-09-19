package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

public class ReportTest extends FplApplicationTests {

    @Autowired
    private IReportService reportService;

    @ParameterizedTest
    @CsvSource({"1"})
    void insertEntryCaptainStat(int tournamentId) {
        this.reportService.insertEntryCaptainStat(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1, Classic, 65, 0"})
    void insertLeagueResultStat(int event, String leagueType, int leagueId, int limit) {
        long startTime = System.currentTimeMillis();
        this.reportService.insertLeagueResultStat(event, leagueType, leagueId, limit);
        long endTime = System.currentTimeMillis();
        System.out.println("escape: " + (endTime - startTime) + "ms");
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1, Classic, 314, 10000"})
    void inertTeamSelectStat(int event, String leagueType, int leagueId, int limit) {
        this.reportService.inertTeamSelectStat(event, leagueType, leagueId, limit);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"Overall, 1"})
    void getTopSelectedMap(String leagueName, int event) {
        LeagueStatData leagueStatData = this.reportService.getLeagueStatData(leagueName, event);
        System.out.println(1);
    }

}

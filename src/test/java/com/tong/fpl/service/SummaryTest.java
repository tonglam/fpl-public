package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.summary.entry.*;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonCaptainData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonInfoData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonScoreData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonSummaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by tong on 2021/6/2
 */
public class SummaryTest extends FplApplicationTests {

    @Autowired
    private ISummaryService summaryService;

    /**
     * @apiNote entry
     */
    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntrySeasonInfo(int entry) {
        long start = System.currentTimeMillis();
        EntrySeasonInfoData data = this.summaryService.qryEntrySeasonInfo(entry);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntrySeasonSummary(int entry) {
        long start = System.currentTimeMillis();
        EntrySeasonSummaryData data = this.summaryService.qryEntrySeasonSummary(entry);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntrySeasonCaptain(int entry) {
        long start = System.currentTimeMillis();
        EntrySeasonCaptainData data = this.summaryService.qryEntrySeasonCaptain(entry);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntrySeasonTransfers(int entry) {
        long start = System.currentTimeMillis();
        EntrySeasonTransfersData data = this.summaryService.qryEntrySeasonTransfers(entry);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntrySeasonScore(int entry) {
        long start = System.currentTimeMillis();
        EntrySeasonScoreData data = this.summaryService.qryEntrySeasonScore(entry);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    /**
     * @apiNote league
     */
    @ParameterizedTest
    @CsvSource({"3571, Classic"})
    void qryLeagueSeasonInfo(int leagueId, String leagueType) {
        long start = System.currentTimeMillis();
        LeagueSeasonInfoData data = this.summaryService.qryLeagueSeasonInfo(leagueId, leagueType);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic, 1870"})
    void qryLeagueSeasonSummary(int leagueId, String leagueType, int entry) {
        long start = System.currentTimeMillis();
        LeagueSeasonSummaryData data = this.summaryService.qryLeagueSeasonSummary(leagueId, leagueType, entry);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic, 1870"})
    void qryLeagueSeasonCaptain(int leagueId, String leagueType, int entry) {
        long start = System.currentTimeMillis();
        LeagueSeasonCaptainData data = this.summaryService.qryLeagueSeasonCaptain(leagueId, leagueType, entry);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic, 1870"})
    void qryLeagueSeasonScore(int leagueId, String leagueType, int entry) {
        long start = System.currentTimeMillis();
        LeagueSeasonScoreData data = this.summaryService.qryLeagueSeasonScore(leagueId, leagueType, entry);
        long end = System.currentTimeMillis();
        System.out.println("escaped: " + (end - start));
        System.out.println(1);
    }

}

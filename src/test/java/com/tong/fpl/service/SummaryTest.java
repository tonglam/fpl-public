package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.summary.entry.*;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonCaptainData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonEntryData;
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
        EntrySeasonInfoData data = this.summaryService.qryEntrySeasonInfo(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntrySeasonSummary(int entry) {
        EntrySeasonSummaryData data = this.summaryService.qryEntrySeasonSummary(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntrySeasonCaptain(int entry) {
        EntrySeasonCaptainData data = this.summaryService.qryEntrySeasonCaptain(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntrySeasonTransfers(int entry) {
        EntrySeasonTransfersData data = this.summaryService.qryEntrySeasonTransfers(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1870"})
    void qryEntrySeasonScore(int entry) {
        EntrySeasonScoreData data = this.summaryService.qryEntrySeasonScore(entry);
        System.out.println(1);
    }

    /**
     * @apiNote league
     */
    @ParameterizedTest
    @CsvSource({"3571, Classic"})
    void qryLeagueSeasonSummary(int leagueId, String leagueType) {
        LeagueSeasonSummaryData data = this.summaryService.qryLeagueSeasonSummary(leagueId, leagueType);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic"})
    void qryLeagueSeasonCaptain(int leagueId, String leagueType) {
        LeagueSeasonCaptainData data = this.summaryService.qryLeagueSeasonCaptain(leagueId, leagueType);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic"})
    void qryLeagueSeasonScore(int leagueId, String leagueType) {
        LeagueSeasonScoreData data = this.summaryService.qryLeagueSeasonScore(leagueId, leagueType);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"3571, Classic, 1870"})
    void qryLeagueSeasonEntry(int leagueId, String leagueType, int entry) {
        LeagueSeasonEntryData data = this.summaryService.qryLeagueSeasonEntry(leagueId, leagueType, entry);
        System.out.println(1);
    }

}

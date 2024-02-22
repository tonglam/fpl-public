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
    @CsvSource({"1713"})
    void qryEntrySeasonInfo(int entry) {
        EntrySeasonInfoData data = this.summaryService.qryEntrySeasonInfo(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1713"})
    void qryEntrySeasonSummary(int entry) {
        EntrySeasonSummaryData data = this.summaryService.qryEntrySeasonSummary(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1713"})
    void qryEntrySeasonCaptain(int entry) {
        EntrySeasonCaptainData data = this.summaryService.qryEntrySeasonCaptain(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1713"})
    void qryEntrySeasonTransfers(int entry) {
        EntrySeasonTransfersData data = this.summaryService.qryEntrySeasonTransfers(entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1713"})
    void qryEntrySeasonScore(int entry) {
        EntrySeasonScoreData data = this.summaryService.qryEntrySeasonScore(entry);
        System.out.println(1);
    }

    /**
     * @apiNote league
     */
    @ParameterizedTest
    @CsvSource({"⚽让让群21/22积分联赛"})
    void qryLeagueSeasonInfo(String leagueName) {
        LeagueSeasonInfoData data = this.summaryService.qryLeagueSeasonInfo(leagueName);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"?让让我冠军杯, 18377"})
    void qryLeagueSeasonSummary(String leagueName, int entry) {
        LeagueSeasonSummaryData data = this.summaryService.qryLeagueSeasonSummary(leagueName, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"⚽让让群21/22积分联赛, 1713"})
    void qryLeagueSeasonCaptain(String leagueName, int entry) {
        LeagueSeasonCaptainData data = this.summaryService.qryLeagueSeasonCaptain(leagueName, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"⚽让让群21/22积分联赛, 1713"})
    void qryLeagueSeasonScore(String leagueName, int entry) {
        LeagueSeasonScoreData data = this.summaryService.qryLeagueSeasonScore(leagueName, entry);
        System.out.println(1);
    }

}

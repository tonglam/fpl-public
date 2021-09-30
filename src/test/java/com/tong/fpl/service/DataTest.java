package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by tong on 2021/9/30
 */
public class DataTest extends FplApplicationTests {

    @Autowired
    private IDataService dataService;

    /**
     * @apiNote daily
     */
    @Test
    void updatePlayerValue() {
        this.dataService.updatePlayerValue();
    }

    @Test
    void updatePlayerStat() {
        this.dataService.updatePlayerStat();
    }

    /**
     * @apiNote matchDay
     */
    @ParameterizedTest
    @CsvSource({"6"})
    void updateEventLiveCache(int event) {
        this.dataService.updateEventLiveCache(event);
    }

    @ParameterizedTest
    @CsvSource({"6"})
    void updateEventLive(int event) {
        this.dataService.updateEventLive(event);
    }

    @Test
    void upsertEventOverallResult() {
        this.dataService.upsertEventOverallResult();
    }

    /**
     * @apiNote entry
     */
    @ParameterizedTest
    @CsvSource({"1713"})
    void upsertEntryInfo(int entry) {
        this.dataService.upsertEntryInfo(entry);
    }

    @ParameterizedTest
    @CsvSource({"1713"})
    void upsertEntryHistoryInfo(int entry) {
        this.dataService.upsertEntryHistoryInfo(entry);
    }

    @ParameterizedTest
    @CsvSource({"6, 1713"})
    void insertEntryEventPick(int event, int entry) {
        this.dataService.insertEntryEventPick(event, entry);
    }

    @ParameterizedTest
    @CsvSource({"1713"})
    void insertEntryEventTransfers(int entry) {
        this.dataService.insertEntryEventTransfers(entry);
    }

    @ParameterizedTest
    @CsvSource({"6, 1713"})
    void updateEntryEventTransfers(int event, int entry) {
        this.dataService.updateEntryEventTransfers(event, entry);
    }

    @ParameterizedTest
    @CsvSource({"6, 1713"})
    void upsertEntryEventResult(int event, int entry) {
        this.dataService.upsertEntryEventResult(event, entry);
    }

    /**
     * @apiNote tournament
     */
    @ParameterizedTest
    @CsvSource({"6, 1"})
    void upsertTournamentEventResult(int event, int tournamentId) {
        this.dataService.upsertTournamentEventResult(event, tournamentId);
    }

    @ParameterizedTest
    @CsvSource({"6, 1"})
    void updatePointsRaceGroupResult(int event, int tournamentId) {
        this.dataService.updatePointsRaceGroupResult(event, tournamentId);
    }

    @ParameterizedTest
    @CsvSource({"6, 1"})
    void updateBattleRaceGroupResult(int event, int tournamentId) {
        this.dataService.updateBattleRaceGroupResult(event, tournamentId);
    }

    @ParameterizedTest
    @CsvSource({"6, 1"})
    void updateKnockoutResult(int event, int tournamentId) {
        this.dataService.updateKnockoutResult(event, tournamentId);
    }

    /**
     * @apiNote league
     */
    @ParameterizedTest
    @CsvSource({"6, 1353, 1713"})
    void updateEntryLeagueEventResult(int event, int leagueId, int entry) {
        this.dataService.updateEntryLeagueEventResult(event, leagueId, entry);
    }

    @ParameterizedTest
    @CsvSource({"6, 1353"})
    void insertLeagueEventPick(int event, int leagueId) {
        this.dataService.insertLeagueEventPick(event, leagueId);
    }

    @ParameterizedTest
    @CsvSource({"6, 1353"})
    void updateLeagueEventResult(int event, int leagueId) {
        this.dataService.updateLeagueEventResult(event, leagueId);
    }

}

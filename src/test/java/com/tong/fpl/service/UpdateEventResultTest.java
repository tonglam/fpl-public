package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.IntStream;

/**
 * Create by tong on 2020/7/15
 */
public class UpdateEventResultTest extends FplApplicationTests {

    @Autowired
    private IUpdateEventService updateEventResultsService;

    @Test
    void updateEntryInfo() {
        this.updateEventResultsService.updateEntryInfo();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void upsertTournamentEntryEventResult(int even1t) {
        IntStream.rangeClosed(1, 38).forEach(event ->
                IntStream.rangeClosed(1, 14).forEach(tournamentId -> {
                    this.updateEventResultsService.upsertTournamentEntryEventResult(event, tournamentId);
                    System.out.println("event: " + event + ", update finished!");
                }));
    }

    @ParameterizedTest
    @CsvSource({"17, 1870"})
    void upsertEntryEventCupResult(int event, int entry) {
        this.updateEventResultsService.upsertEntryEventCupResult(event, entry);
        System.out.println("event: " + event + ", update finished!");
    }

    @ParameterizedTest
    @CsvSource({"25, 1"})
    void upsertTournamentEntryEventCupResult(int event, int tournamentId) {
        this.updateEventResultsService.upsertTournamentEntryEventCupResult(event, tournamentId);
        System.out.println("event: " + event + ", update finished!");
    }

    @ParameterizedTest
    @CsvSource({"1, 1870"})
    void insertEntryEventPicks(int event, int entry) {
        this.updateEventResultsService.insertEntryEventPick(event, entry);
        System.out.println(1);
    }

    @Test
    void insertTournamentEntryEventPicks() {
        int event = 32;
        IntStream.rangeClosed(1, 14).forEach(tournamentId -> {
            System.out.println("tournament:" + tournamentId);
            this.updateEventResultsService.insertTournamentEntryEventPick(event, tournamentId);
            System.out.println("event:" + event);
        });
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"14"})
    void insertTournamentEntryEventTransfer(int tournamentId) {
        this.updateEventResultsService.insertTournamentEntryEventTransfers(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"38"})
    void updateTournamentEventTransferPlayed(int event, int tournamentId) {
        this.updateEventResultsService.updateTournamentEventTransfersPlayed(event, tournamentId);
        System.out.println("event: " + event + ", update finished!");
    }

    @ParameterizedTest
    @CsvSource({"29, 14"})
    void updatePointsRaceGroupResult(int event, int tournamentId) {
        this.updateEventResultsService.updatePointsRaceGroupResult(event, tournamentId);
        System.out.println("event: " + event + ", update finished!");
    }

    @ParameterizedTest
    @CsvSource({"14, 5"})
    void updateBattleRaceGroupResult(int event, int tournamentId) {
        this.updateEventResultsService.updateBattleRaceGroupResult(event, tournamentId);
        System.out.println("event: " + event + ", update finished!");
    }

    @ParameterizedTest
    @CsvSource({"6, 7"})
    void updateKnockoutResult(int event, int tournamentId) {
        this.updateEventResultsService.updateKnockoutResult(event, tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"4, 4"})
    void updateZjPhaseOneResult(int event, int tournamentId) {
        this.updateEventResultsService.updateZjPhaseOneResult(event, tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"5, 4"})
    void updateZjPhaseTwoResult(int event, int tournamentId) {
        this.updateEventResultsService.updateZjPhaseTwoResult(event, tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"6, 4"})
    void updateZjPkResult(int event, int tournamentId) {
        this.updateEventResultsService.updateZjPkResult(event, tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"4"})
    void updateZjTournamentResult(int tournamentId) {
        this.updateEventResultsService.updateZjTournamentResult(tournamentId);
        System.out.println(1);
    }

    @Test
    void upsertEventLiveSummary() {
        this.updateEventResultsService.upsertEventLiveSummary();
        System.out.println(1);
    }

}

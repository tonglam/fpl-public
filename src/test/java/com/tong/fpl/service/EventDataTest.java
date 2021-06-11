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
public class EventDataTest extends FplApplicationTests {

    @Autowired
    private IEventDataService eventDataService;

    @Test
    void updateEntryInfo() {
        this.eventDataService.updateEntryInfo();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void upsertTournamentEntryEventResult(int even1t) {
        IntStream.rangeClosed(1, 38).forEach(event ->
                IntStream.rangeClosed(1, 14).forEach(tournamentId -> {
                    this.eventDataService.upsertTournamentEntryEventResult(event, tournamentId);
                    System.out.println("event: " + event + ", update finished!");
                }));
    }

    @ParameterizedTest
    @CsvSource({"17, 1870"})
    void upsertEntryEventCupResult(int event, int entry) {
        this.eventDataService.upsertEntryEventCupResult(event, entry);
        System.out.println("event: " + event + ", update finished!");
    }

    @ParameterizedTest
    @CsvSource({"25, 1"})
    void upsertTournamentEntryEventCupResult(int event, int tournamentId) {
        this.eventDataService.upsertTournamentEntryEventCupResult(event, tournamentId);
        System.out.println("event: " + event + ", update finished!");
    }

    @ParameterizedTest
    @CsvSource({"1, 1870"})
    void insertEntryEventPicks(int event, int entry) {
        this.eventDataService.insertEntryEventPick(event, entry);
        System.out.println(1);
    }

    @Test
    void insertTournamentEntryEventPicks() {
        int event = 32;
        IntStream.rangeClosed(1, 14).forEach(tournamentId -> {
            System.out.println("tournament:" + tournamentId);
            this.eventDataService.insertTournamentEntryEventPick(event, tournamentId);
            System.out.println("event:" + event);
        });
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"14"})
    void insertTournamentEntryEventTransfer(int tournamentId) {
        this.eventDataService.insertTournamentEntryEventTransfers(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2, 1"})
    void updateTournamentEventTransfer(int even1t, int tournament1Id) {
        IntStream.rangeClosed(2, 38).forEach(event -> {
            IntStream.rangeClosed(1, 14).forEach(tournamentId -> {
                this.eventDataService.updateTournamentEventTransfers(event, tournamentId);
                System.out.println("event: " + event + ", update finished!");
            });
        });

    }

    @ParameterizedTest
    @CsvSource({"29, 14"})
    void updatePointsRaceGroupResult(int event, int tournamentId) {
        this.eventDataService.updatePointsRaceGroupResult(event, tournamentId);
        System.out.println("event: " + event + ", update finished!");
    }

    @ParameterizedTest
    @CsvSource({"14, 5"})
    void updateBattleRaceGroupResult(int event, int tournamentId) {
        this.eventDataService.updateBattleRaceGroupResult(event, tournamentId);
        System.out.println("event: " + event + ", update finished!");
    }

    @ParameterizedTest
    @CsvSource({"6, 7"})
    void updateKnockoutResult(int event, int tournamentId) {
        this.eventDataService.updateKnockoutResult(event, tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"4, 4"})
    void updateZjPhaseOneResult(int event, int tournamentId) {
        this.eventDataService.updateZjPhaseOneResult(event, tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"5, 4"})
    void updateZjPhaseTwoResult(int event, int tournamentId) {
        this.eventDataService.updateZjPhaseTwoResult(event, tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"6, 4"})
    void updateZjPkResult(int event, int tournamentId) {
        this.eventDataService.updateZjPkResult(event, tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"4"})
    void updateZjTournamentResult(int tournamentId) {
        this.eventDataService.updateZjTournamentResult(tournamentId);
        System.out.println(1);
    }

    @Test
    void upsertEventLiveSummary() {
        this.eventDataService.upsertEventLiveSummary();
        System.out.println(1);
    }

}

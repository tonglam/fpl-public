package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/7/15
 */
public class EventDataTest extends FplApplicationTests {

    @Autowired
    private IQueryService queryService;
    @Autowired
    private IEventDataService eventDataService;

    @Test
    void updateEntryInfo() {
        this.eventDataService.updateEntryInfo();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void upsertTournamentEntryEventResult(int event) {
        IntStream.rangeClosed(1, 5).forEach(tournamentId -> {
            this.eventDataService.upsertTournamentEntryEventResult(event, tournamentId);
            System.out.println("event: " + event + ", update finished!");
        });
    }

    @ParameterizedTest
    @CsvSource({"17, 1870"})
    void upsertEntryEventCupResult(int event, int entry) {
        this.eventDataService.upsertEntryEventCupResult(event, entry);
        System.out.println("event: " + event + ", update finished!");
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void upsertEventCupResultByEntryList(int event) {
        List<Integer> entryList = this.queryService.qryActiveTournamentEntryList();
        this.eventDataService.upsertEventCupResultByEntryList(event, entryList);
        System.out.println("event: " + event + ", update finished!");
    }

    @ParameterizedTest
    @CsvSource({"1, 1870"})
    void insertEntryEventPicks(int event, int entry) {
        this.eventDataService.insertEntryEventPick(event, entry);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void insertEventPickByEntryList(int event) {
        List<Integer> entryList = this.queryService.qryActiveTournamentEntryList();
        this.eventDataService.insertEventPickByEntryList(event, entryList);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void insertEntryEventTransfers(int entry) {
        this.eventDataService.insertEntryEventTransfers(entry);
        System.out.println(1);
    }

    @Test
    void insertEventTransfersByEntryList() {
        List<Integer> entryList = this.queryService.qryActiveTournamentEntryList();
        this.eventDataService.insertEventTransfersByEntryList(entryList);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2"})
    void updateEventTransfersByEntryList(int event) {
        IntStream.rangeClosed(1, 5).forEach(tournamentId -> {
            List<Integer> entryList = this.queryService.qryActiveTournamentEntryList();
            this.eventDataService.updateEventTransfersByEntryList(event, entryList);
            System.out.println("event: " + event + ", update finished!");
        });
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void updatePointsRaceGroupResult(int event) {
        IntStream.rangeClosed(1, 5).forEach(tournamentId -> {
            this.eventDataService.updatePointsRaceGroupResult(event, tournamentId);
            System.out.println("event: " + event + ", update finished!");
        });
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void updateBattleRaceGroupResult(int event) {
        IntStream.rangeClosed(1, 5).forEach(tournamentId -> {
            this.eventDataService.updateBattleRaceGroupResult(event, tournamentId);
            System.out.println("event: " + event + ", update finished!");
        });
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void updateKnockoutResult(int event) {
        IntStream.rangeClosed(1, 5).forEach(tournamentId -> {
            this.eventDataService.updateKnockoutResult(event, tournamentId);
            System.out.println("event: " + event + ", update finished!");
        });
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

}

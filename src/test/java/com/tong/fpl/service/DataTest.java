package com.tong.fpl.service;

import com.google.common.collect.Lists;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.global.MapData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
    @CsvSource({"106575"})
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
    @CsvSource({"30, 6"})
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
    @CsvSource({"24, 17"})
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

    /**
     * @apiNote scout
     */
    @ParameterizedTest
    @CsvSource("2, fantasy_football_fix")
    void insertEventSourceScout(int event, String source) {
        this.dataService.insertEventSourceScout(event, source, this.getScoutDataList());
    }

    private List<MapData<Integer>> getScoutDataList() {
        List<MapData<Integer>> list = Lists.newArrayList();
        MapData<Integer> i = new MapData<Integer>()
                .setKey("1")
                .setValue(15);
        list.add(i);
        MapData<Integer> a = new MapData<Integer>()
                .setKey("2")
                .setValue(285);
        list.add(a);
        MapData<Integer> b = new MapData<Integer>()
                .setKey("3")
                .setValue(306);
        list.add(b);
        MapData<Integer> c = new MapData<Integer>()
                .setKey("4")
                .setValue(146);
        list.add(c);
        MapData<Integer> d = new MapData<Integer>()
                .setKey("5")
                .setValue(284);
        list.add(d);
        MapData<Integer> e = new MapData<Integer>()
                .setKey("6")
                .setValue(283);
        list.add(e);
        MapData<Integer> f = new MapData<Integer>()
                .setKey("7")
                .setValue(45);
        list.add(f);
        MapData<Integer> g = new MapData<Integer>()
                .setKey("8")
                .setValue(486);
        list.add(g);
        MapData<Integer> h = new MapData<Integer>()
                .setKey("9")
                .setValue(19);
        list.add(h);
        MapData<Integer> j = new MapData<Integer>()
                .setKey("10")
                .setValue(318);
        list.add(j);
        MapData<Integer> k = new MapData<Integer>()
                .setKey("11")
                .setValue(28);
        list.add(k);
        MapData<Integer> l = new MapData<Integer>()
                .setKey("12")
                .setValue(254);
        list.add(l);
        MapData<Integer> m = new MapData<Integer>()
                .setKey("13")
                .setValue(295);
        list.add(m);
        MapData<Integer> n = new MapData<Integer>()
                .setKey("14")
                .setValue(346);
        list.add(n);
        MapData<Integer> o = new MapData<Integer>()
                .setKey("15")
                .setValue(54);
        list.add(o);
        MapData<Integer> captain = new MapData<Integer>()
                .setKey("captain")
                .setValue(318);
        list.add(captain);
        MapData<Integer> viceCaptain = new MapData<Integer>()
                .setKey("vice_captain")
                .setValue(283);
        list.add(viceCaptain);
        return list;
    }

    @ParameterizedTest
    @CsvSource("2")
    void updateEventSourceScoutResult(int event) {
        this.dataService.updateEventSourceScoutResult(event);
    }

}

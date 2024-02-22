package com.tong.fpl.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.special.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by tong on 2022/02/25
 */
public class SpecialTournamentTest extends FplApplicationTests {

    @Autowired
    private ISpecialTournamentService specialTournamentService;

    @Test
    void random() {
        List<Integer> usedIndexList = Lists.newArrayList();
        Random random = new Random();
        IntStream.rangeClosed(1, 5).forEach(i -> {
            int index = random.nextInt(5) + 1;
            while (usedIndexList.contains(index)) {
                index = random.nextInt(5) + 1;
            }
            usedIndexList.add(index);
            System.out.println("index: " + index);
        });

//        List<Integer> entryList = Lists.newArrayList(
//                2418908,
//                18327,
//                6496,
//                1625812,
//                2838303,
//                2404335,
//                2674276,
//                22227,
//                3676272,
//                96138,
//                2313048,
//                2846260,
//                2313026,
//                510141,
//                3359321
//        );
//        List<Integer> usedIndexList = Lists.newArrayList();
//        Random random = new Random();
//        IntStream.rangeClosed(1, 15).forEach(i -> {
//            int index = this.getIndex(random);
//            while (usedIndexList.contains(index)) {
//                index = this.getIndex(random);
//            }
//            usedIndexList.add(index);
//            System.out.println("index: " + index + ", entry: " + entryList.get(index));
//        });
    }

    private int getIndex(Random random) {
        return random.nextInt(15);
    }

    @ParameterizedTest
    @CsvSource({"2"})
    void getTournamentEntryList(int tournamentId) {
        List<Integer> list = this.specialTournamentService.getTournamentEntryList(tournamentId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2"})
    void insertGroupInfo(int tournamentId) {
        Map<String, String> map = Maps.newHashMap();
        map.put("1", "Team A");
        map.put("2", "Team B");
        map.put("3", "Team C");
        map.put("4", "Team D");
        this.specialTournamentService.insertGroupInfo(tournamentId, map);
    }

    @ParameterizedTest
    @CsvSource({"2"})
    void insertGroupEntry(int tournamentId) {
        List<GroupInfoData> list = Lists.newLinkedList();
        GroupInfoData groupA = new GroupInfoData()
                .setGroupId(1)
                .setEntryList(this.initGroupAEntryList());
        GroupInfoData groupB = new GroupInfoData()
                .setGroupId(2)
                .setEntryList(this.initGroupBEntryList());
        GroupInfoData groupC = new GroupInfoData()
                .setGroupId(3)
                .setEntryList(this.initGroupCEntryList());
        list.add(groupA);
        list.add(groupB);
        list.add(groupC);
        this.specialTournamentService.insertGroupEntry(tournamentId, list);
    }

    private List<EntryInfoData> initGroupAEntryList() {
        List<EntryInfoData> list = Lists.newArrayList();
        list.add(
                new EntryInfoData()
                        .setEntry(96138)
                        .setShuffledGroupId(5)
        );
        list.add(
                new EntryInfoData()
                        .setEntry(2404335)
                        .setShuffledGroupId(1)
        );
        list.add(
                new EntryInfoData()
                        .setEntry(6496)
                        .setShuffledGroupId(3)
        );
        list.add(
                new EntryInfoData()
                        .setEntry(2846260)
                        .setShuffledGroupId(2)
        );
        list.add(
                new EntryInfoData()
                        .setEntry(2313026)
                        .setShuffledGroupId(4)
        );
        return list;
    }

    private List<EntryInfoData> initGroupBEntryList() {
        List<EntryInfoData> list = Lists.newArrayList();
        list.add(
                new EntryInfoData()
                        .setEntry(22227)
                        .setShuffledGroupId(2)
        );
        list.add(
                new EntryInfoData()
                        .setEntry(2313048)
                        .setShuffledGroupId(4)
        );
        list.add(
                new EntryInfoData()
                        .setEntry(3359321)
                        .setShuffledGroupId(1)
        );
        list.add(
                new EntryInfoData()
                        .setEntry(2838303)
                        .setShuffledGroupId(3)
        );
        list.add(
                new EntryInfoData()
                        .setEntry(510141)
                        .setShuffledGroupId(5)
        );
        return list;
    }

    private List<EntryInfoData> initGroupCEntryList() {
        List<EntryInfoData> list = Lists.newArrayList();
        list.add(
                new EntryInfoData()
                        .setEntry(2674276)
                        .setShuffledGroupId(4)
        );
        list.add(
                new EntryInfoData()
                        .setEntry(1625812)
                        .setShuffledGroupId(5)
        );
        list.add(
                new EntryInfoData()
                        .setEntry(2418908)
                        .setShuffledGroupId(2)
        );
        list.add(
                new EntryInfoData()
                        .setEntry(3676272)
                        .setShuffledGroupId(1)
        );
        list.add(
                new EntryInfoData()
                        .setEntry(18327)
                        .setShuffledGroupId(3)
        );
        return list;
    }

    @ParameterizedTest
    @CsvSource({"2, 31"})
    void insertEntryEventResult(int tournamentId, int event) {
        this.specialTournamentService.insertEntryEventResult(tournamentId, event);
    }

    @ParameterizedTest
    @CsvSource({"2, 31"})
    void insertGroupEventResult(int tournamentId, int event) {
        this.specialTournamentService.insertGroupEventResult(tournamentId, event);
    }

    @ParameterizedTest
    @CsvSource({"2, 31"})
    void insertShuffledGroupEventResult(int tournamentId, int event) {
        this.specialTournamentService.insertShuffledGroupEventResult(tournamentId, event);
    }

    @ParameterizedTest
    @CsvSource({"2, 31"})
    void getShuffledGroupResult(int tournamentId, int event) {
        Map<Integer, List<ShuffledGroupResultData>> map = this.specialTournamentService.getShuffledGroupResult(tournamentId, event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2, 31"})
    void getEventGroupResult(int tournamentId, int event) {
        List<GroupResultData> list = this.specialTournamentService.getEventGroupResult(tournamentId, event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2, 31"})
    void getGroupRankResult(int tournamentId, int event) {
        List<GroupRankResultData> list = this.specialTournamentService.getGroupRankResult(tournamentId, event);
        System.out.println(1);
    }

}

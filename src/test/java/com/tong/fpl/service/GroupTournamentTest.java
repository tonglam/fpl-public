package com.tong.fpl.service;

import com.google.common.collect.Lists;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.groupTournament.GroupTournamentResultData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Create by tong on 4/11/2023
 */
public class GroupTournamentTest extends FplApplicationTests {

    @Autowired
    private IGroupTournamentService groupTournamentService;

    @Test
    void insertGroupEntry() {
        List<GroupTournamentResultData> list = Lists.newLinkedList();
        GroupTournamentResultData groupA = new GroupTournamentResultData()
                .setGroupId(1)
                .setGroupName("A组")
                .setCaptainId(510081)
                .setEntryList(Lists.newArrayList(21443, 5861342, 2726480, 510081))
                .setEventResultList(Lists.newArrayList())
                .setTotalGroupPoints(0)
                .setTotalGroupCost(0)
                .setRank(0)
                .setRemarks("")
                .setUpdateTime("");
        list.add(groupA);

        GroupTournamentResultData groupB = new GroupTournamentResultData()
                .setGroupId(2)
                .setGroupName("B组")
                .setCaptainId(101736)
                .setEntryList(Lists.newArrayList(2202, 101736, 2798485, 5460482))
                .setEventResultList(Lists.newArrayList())
                .setTotalGroupPoints(0)
                .setTotalGroupCost(0)
                .setRank(0)
                .setRemarks("")
                .setUpdateTime("");
        list.add(groupB);

        GroupTournamentResultData groupC = new GroupTournamentResultData()
                .setGroupId(3)
                .setGroupName("C组")
                .setCaptainId(4191808)
                .setEntryList(Lists.newArrayList(345618, 4191808, 485511, 5588075))
                .setEventResultList(Lists.newArrayList())
                .setTotalGroupPoints(0)
                .setTotalGroupCost(0)
                .setRank(0)
                .setRemarks("")
                .setUpdateTime("");
        list.add(groupC);

        GroupTournamentResultData groupD = new GroupTournamentResultData()
                .setGroupId(4)
                .setGroupName("D组")
                .setCaptainId(1504208)
                .setEntryList(Lists.newArrayList(1504208, 5498449, 2729845, 2727234))
                .setEventResultList(Lists.newArrayList())
                .setTotalGroupPoints(0)
                .setTotalGroupCost(0)
                .setRank(0)
                .setRemarks("")
                .setUpdateTime("");
        list.add(groupD);

        this.groupTournamentService.insertGroupEntry(list);
    }

    @ParameterizedTest
    @CsvSource({"1, 11"})
    void insertEntryEventResult(int groupTournamentId, int event) {
        this.groupTournamentService.insertEntryEventResult(groupTournamentId, event);
    }

    @ParameterizedTest
    @CsvSource({"1, 11"})
    void getEventGroupResult(int groupTournamentId, int event){
        List<GroupTournamentResultData> list = this.groupTournamentService.getEventGroupTournamentResult(groupTournamentId, event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1, 11"})
    void refreshGroupTournamentResult(int groupTournamentId, int event){
        this.groupTournamentService.refreshGroupTournamentResult(groupTournamentId, event);
    }

}

package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by tong on 2020/12/9
 */
public class GroupTest extends FplApplicationTests {

    @Autowired
    private IGroupService groupService;

    @Test
    void upsertEventScout() {
        ScoutData scoutData = new ScoutData()
                .setEvent(2)
                .setEntry(1713)
                .setScoutName("tong话里都是骗人的")
                .setTransfers(4)
                .setLeftTransfers(-1)
                .setGkp(1)
                .setDef(275)
                .setMid(254)
                .setFwd(337)
                .setCaptain(233)
                .setReason("truncate table scout");
        this.groupService.upsertEventScout(scoutData);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1"})
    void updateEventScoutResult(int event) {
        this.groupService.updateEventScoutResult(event);
        System.out.println(1);
    }

}

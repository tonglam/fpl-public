package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by tong on 2020/12/9
 */
public class ScoutTest extends FplApplicationTests {

    @Autowired
    private IGroupService scoutService;

    @ParameterizedTest
    @CsvSource({"12"})
    void updateEventScoutResult(int event) {
        this.scoutService.updateEventScoutResult(event);
        System.out.println(1);
    }

}

package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.IntStream;

/**
 * Create by tong on 2020/12/9
 */
public class ScoutTest extends FplApplicationTests {

    @Autowired
    private IGroupService scoutService;

    @ParameterizedTest
    @CsvSource({"37"})
    void updateEventScoutResult(int event1) {
        IntStream.rangeClosed(1, 37).forEach(event -> {
            this.scoutService.updateEventScoutResult(event);
        });
        System.out.println(1);
    }

}

package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by tong on 2021/8/26
 */
public class DataParseTest extends FplApplicationTests {

    @Autowired
    private IDataParseService dataParseService;

    @ParameterizedTest
    @CsvSource({"2021, E://2021.json"})
    void parseNutmegSeasonData(String season, String fileName) {
        this.dataParseService.parseNutmegSeasonData(season, fileName);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1819"})
    void parseNutmegEventData(String season) {
        this.dataParseService.parseNutmegEventData(season);
        System.out.println(1);
    }

}

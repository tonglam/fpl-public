package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Create by tong on 2021/9/29
 */
public class DataTest extends FplApplicationTests {

    @Autowired
    private IDataService dataService;

    @ParameterizedTest
    @CsvSource({"1713"})
    void refreshEntryInfo(int entry) {
        this.dataService.refreshEntryInfo(entry);
    }

}

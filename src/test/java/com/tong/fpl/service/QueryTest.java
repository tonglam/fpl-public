package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.letletme.EntryEventData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

public class QueryTest extends FplApplicationTests {

    @Autowired
    private IQuerySerivce querySerivce;

    @ParameterizedTest
    @CsvSource({"47,3697"})
    void qryEntryEvent(int event, int entry) {
        EntryEventData entryEventData = this.querySerivce.qryEntryEvent(event, entry);
        System.out.println(1);
    }

}

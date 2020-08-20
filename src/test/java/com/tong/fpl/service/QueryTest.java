package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class QueryTest extends FplApplicationTests {

    @Autowired
    private IQuerySerivce querySerivce;

    @ParameterizedTest
    @CsvSource({"20200726"})
    void qryDayChangePlayerValue(String changeDate) {
        List<PlayerValueData> playerValueDataList = this.querySerivce.qryDayChangePlayerValue(changeDate);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1920, 1, 1404"})
    void qryEntryEvent(String season, int event, int entry) {
        EntryEventData entryEventData = this.querySerivce.qryEntryEventResult(season, event, entry);
        System.out.println(1);
    }

}

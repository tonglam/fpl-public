package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.StaticRes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Create by tong on 2021/8/29
 */
public class InterfaceTest extends FplApplicationTests {

    @Autowired
    private IInterfaceService interfaceService;

    @Test
    void getWangBootstrapStatic() {
        Optional<StaticRes> res = this.interfaceService.getWangBootstrapStatic();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1713"})
    void getEntry(int entry) {
        Optional<EntryRes> getEntry = this.interfaceService.getEntry(entry);
        System.out.println(1);
    }


}

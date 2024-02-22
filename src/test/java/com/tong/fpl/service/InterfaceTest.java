package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.response.StaticRes;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2021/9/30
 */
public class InterfaceTest extends FplApplicationTests {

    @Autowired
    private IInterfaceService interfaceService;

    @Test
    void getBootstrapStatic() {
        Optional<StaticRes> data = this.interfaceService.getBootstrapStatic();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource("1289837")
    void getEntryInfoListFromClassic(int classicId) {
        List<EntryInfoData> list = this.interfaceService.getEntryInfoListFromClassic(classicId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"122806"})
    void getPlayerPicture(int code) {
        Optional<InputStream> a = this.interfaceService.getPlayerPicture(code);
        System.out.println(1);
    }

}

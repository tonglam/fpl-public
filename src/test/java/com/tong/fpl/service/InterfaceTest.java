package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.response.StaticRes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Create by tong on 2021/9/30
 */
public class InterfaceTest extends FplApplicationTests {

    @Autowired
    private IInterfaceService interfaceService;

    /**
     * @apiNote fantasy
     */
    @Test
    void getBootstrapStatic() {
        Optional<StaticRes> data = this.interfaceService.getBootstrapStatic();
        System.out.println(1);
    }

    /**
     * @apiNote fpl-data
     */

}

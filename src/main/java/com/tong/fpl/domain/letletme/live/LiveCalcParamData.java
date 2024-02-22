package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Create by tong on 2021/9/8
 */
@Data
@Accessors(chain = true)
public class LiveCalcParamData {

    private int event;
    private Map<Integer, Integer> elementMap; //  position -> element
    private String chip;
    private int captain;
    private int viceCaptain;

}

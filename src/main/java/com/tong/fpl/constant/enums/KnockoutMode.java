package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * Create by tong on 2020/6/29
 */
@Getter
@AllArgsConstructor
public enum KnockoutMode {
    No_knockout("0"), Single_round("1"), Home_away("2");

    private final String value;

    public static KnockoutMode getKnockoutModeFromValue(String value) {
        return Stream.of(KnockoutMode.values())
                .filter(o -> StringUtils.equals(o.getValue(), value))
                .findFirst()
                .orElse(KnockoutMode.No_knockout);
    }

}

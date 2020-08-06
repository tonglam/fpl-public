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
public enum GroupMode {
    No_group("0"), Points_race("1"), Battle_race("2");

    private final String value;

    public static GroupMode getGroupModeFromValue(String value) {
        return Stream.of(GroupMode.values())
                .filter(o -> StringUtils.equals(o.getValue(), value))
                .findFirst()
                .orElse(GroupMode.No_group);
    }

}

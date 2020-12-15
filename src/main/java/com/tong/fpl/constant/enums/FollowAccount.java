package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * Create by tong on 2020/12/15
 */
@Getter
@AllArgsConstructor
public enum FollowAccount {

    Offiaccount_2021(4074865);

    private final int entry;

    public static int getFollowAccountEntry(String name, String season) {
        return Stream.of(FollowAccount.values())
                .filter(o -> StringUtils.equals(o.name(), name + "_" + season))
                .map(FollowAccount::getEntry)
                .findFirst()
                .orElse(0);
    }

}

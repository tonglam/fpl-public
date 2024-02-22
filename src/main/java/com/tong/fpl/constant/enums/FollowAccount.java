package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * 每赛季开始前更新
 * <p>
 * Create by tong on 2020/12/15
 */
@Getter
@AllArgsConstructor
public enum FollowAccount {

    Offiaccount(4074865),
    Magnus_Carlsen(76862),
    FPLGeneral(1546),
    FPLtips(1667),
    Fpl_Salah(825),
    Ben_Crellin(132073),
    Ben_Dinnery(1572168);

    private final int entry;

    public static int getFollowAccountEntry(String name) {
        if (name.contains("-")) {
            return getFollowAccountEntry(name.replaceAll("-", "_"));
        }
        return Stream.of(FollowAccount.values())
                .filter(o -> StringUtils.equals(o.name(), name))
                .map(FollowAccount::getEntry)
                .findFirst()
                .orElse(0);
    }

}

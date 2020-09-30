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
public enum TournamentMode {
    Normal("0", "普通联赛"), Zj("1", "浙江联赛");

    private final String value;
    private final String modeName;

    public static TournamentMode getTournamnetModeByValue(String value) {
        return Stream.of(TournamentMode.values())
                .filter(o -> StringUtils.equals(o.getValue(), value))
                .findFirst()
                .orElse(TournamentMode.Normal);
    }

}

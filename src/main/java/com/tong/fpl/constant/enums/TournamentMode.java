package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2020/6/29
 */
@Getter
@AllArgsConstructor
public enum TournamentMode {
    Normal("0", "普通联赛"), Zj("1", "浙江联赛"), Manual("2", "制定联赛");

    private final String value;
    private final String modeName;

}

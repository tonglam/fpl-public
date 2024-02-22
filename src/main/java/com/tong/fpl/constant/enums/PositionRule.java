package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2020/5/20
 */
@Getter
@AllArgsConstructor
public enum PositionRule {

    MIN_NUM_GKP(1), MIN_NUM_DEF(3), MIN_NUM_FWD(1), MIN_PLAYERS(11);

    private final int num;

}

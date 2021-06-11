package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Create by tong on 2020/5/20
 */
@Getter
@AllArgsConstructor
public enum Position {

    GKP(1), DEF(2), MID(3), FWD(4), SUB(5);

    private final int elementType;

    public static String getNameFromElementType(int elementType) {
        return Arrays.stream(Position.values())
                .filter(o -> o.getElementType() == elementType)
                .map(Position::name)
                .findFirst()
                .orElse(null);
    }

}

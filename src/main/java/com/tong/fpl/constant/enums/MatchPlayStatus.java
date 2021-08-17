package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Create by tong on 2020/9/15
 */
@Getter
@AllArgsConstructor
public enum MatchPlayStatus {

    Next_Event(-1), Playing(0), Finished(1), Not_Start(2), Event_Not_Finished(3), Blank(4);

    private final int status;

    public static boolean legalStatusName(String status) {
        return Arrays.stream(MatchPlayStatus.values()).anyMatch(o -> StringUtils.equalsIgnoreCase(status, o.name()));
    }

}

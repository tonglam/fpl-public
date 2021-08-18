package com.tong.fpl.constant.enums;

import com.tong.fpl.utils.CommonUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create by tong on 2021/8/17
 */
@Getter
@AllArgsConstructor
public enum Season {

    Season_1920("1920"), Season_2021("2021"), Season_2122("2122");

    private final String seasonValue;

    public static boolean legalSeason(String season) {
        return Arrays.stream(Season.values()).anyMatch(o -> StringUtils.equals(season, o.getSeasonValue()));
    }

    public static List<String> getHistorySeason() {
        return Arrays.stream(Season.values())
                .map(Season::getSeasonValue)
                .filter(o -> !StringUtils.equalsIgnoreCase(CommonUtils.getCurrentSeason(), o))
                .collect(Collectors.toList());
    }

}

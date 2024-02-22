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

    Season_1617("1617"), Season_1718("1718"), Season_1819("1819"),
    Season_1920("1920"), Season_2021("2021"), Season_2122("2122"),
    Season_2223("2223"), Season_2324("2324");

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

    public static List<String> getAllSeason() {
        return Arrays.stream(Season.values())
                .map(Season::getSeasonValue)
                .collect(Collectors.toList());
    }

}

package com.tong.fpl.utils;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Create by tong on 2020/1/19
 */
public class DictUtils {

    private static Map<Integer, String> teamMap = Maps.newHashMap();
    private static Map<Integer, String> teamShortMap = Maps.newHashMap();
    private static Map<Integer, String> elementTypeMap = Maps.newHashMap();

    static {
        teamMap.put(1, "Arsenal");
        teamMap.put(2, "Aston Villa");
        teamMap.put(3, "Bournemouth");
        teamMap.put(4, "Brighton");
        teamMap.put(5, "Burnley");
        teamMap.put(6, "Chelsea");
        teamMap.put(7, "Crystal Palace");
        teamMap.put(8, "Everton");
        teamMap.put(9, "Leicester");
        teamMap.put(10, "Liverpool");
        teamMap.put(11, "Man City");
        teamMap.put(12, "Man Utd");
        teamMap.put(13, "Newcastle");
        teamMap.put(14, "Norwich");
        teamMap.put(15, "Sheffield Utd");
        teamMap.put(16, "Southampton");
        teamMap.put(17, "Spurs");
        teamMap.put(18, "Watford");
        teamMap.put(19, "West Ham");
        teamMap.put(20, "Wolves");
    }

    static {
        teamMap.put(1, "ARS");
        teamMap.put(2, "AVL");
        teamMap.put(3, "BOU");
        teamMap.put(4, "BHA");
        teamMap.put(5, "BUR");
        teamMap.put(6, "CHE");
        teamMap.put(7, "CRY");
        teamMap.put(8, "EVE");
        teamMap.put(9, "LEI");
        teamMap.put(10, "LIV");
        teamMap.put(11, "MCI");
        teamMap.put(12, "MUN");
        teamMap.put(13, "NEW");
        teamMap.put(14, "NOR");
        teamMap.put(15, "SHU");
        teamMap.put(16, "SOU");
        teamMap.put(17, "TOT");
        teamMap.put(18, "WAT");
        teamMap.put(19, "WHU");
        teamMap.put(20, "WOL");
    }

    static {
        elementTypeMap.put(1, "GKP");
        elementTypeMap.put(2, "DEF");
        elementTypeMap.put(3, "MID");
        elementTypeMap.put(4, "FWD");
    }

    public static String getTeamNameByteamId(int teamId) {
        if (teamId > 20) {
            return null;
        }
        return teamMap.get(teamId);
    }

    public static String getElementTypeName(int elementType) {
        if (!elementTypeMap.containsKey(elementType)) {
            return "";
        }
        return elementTypeMap.get(elementType);
    }

}

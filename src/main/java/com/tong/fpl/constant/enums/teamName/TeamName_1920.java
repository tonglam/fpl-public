package com.tong.fpl.constant.enums.teamName;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Create by tong on 2020/7/9
 */
@Getter
@AllArgsConstructor
public enum TeamName_1920 {

    Arsenal(1, "ARS"),
    AstonVilla(2, "AVL"),
    Bournemouth(3, "BOU"),
    Brighton(4, "BHA"),
    Burnley(5, "BUR"),
    Chelsea(6, "CHE"),
    CrystalPalace(7, "CRY"),
    Everton(8, "EVE"),
    Leicester(9, "LEI"),
    Liverpool(10, "LIV"),
    ManCity(11, "MCI"),
    ManUtd(12, "MUN"),
    Newcastle(13, "NEW"),
    Norwich(14, "NOR"),
    SheffieldUtd(15, "SHU"),
    Southampton(16, "SOU"),
    Spurs(17, "TOT"),
    Watford(18, "WAT"),
    WestHam(19, "WHU"),
    Wolves(20, "WOL");

    private final int teamId;
    private final String shortName;

    public static TeamName_1920 getTeamNameFromId(int teamId) {
        return Stream.of(TeamName_1920.values())
                .filter(o -> o.getTeamId() == teamId)
                .findFirst()
                .orElse(null);
    }

}

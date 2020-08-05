package com.tong.fpl.constant.enums.teamName;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2020/7/9
 */
@Getter
@AllArgsConstructor
public enum TeamName_2021 {
	Arsenal(1, "ARS"),
	AstonVilla(2, "AVL"),
	Brighton(3, "BHA"),
	Burnley(4, "BUR"),
	Chelsea(5, "CHE"),
	CrystalPalace(6, "CRY"),
	Everton(7, "EVE"),
	Fulham(8, "Ful"),
	Leeds(9, "Lee"),
	Leicester(10, "LEI"),
	Liverpool(11, "LIV"),
	ManCity(12, "MCI"),
	ManUtd(13, "MUN"),
	Newcastle(14, "NEW"),
	SheffieldUtd(15, "SHU"),
	Southampton(16, "SOU"),
	Spurs(17, "TOT"),
	Wesborm(18, "Wes"),
	WestHam(19, "WHU"),
	Wolves(20, "WOL");

	private final int teamId;
	private final String shortName;
}

package com.tong.fpl.constant.enums.teamName;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by tong on 2020/7/9
 */
@Getter
@AllArgsConstructor
public enum TeamName_1920 {
	Arsenal(1, "Arsenal", "ARS"),
	AstonVilla(2, "Aston Villa", "AVL"),
	Bournemouth(3, "Bournemouth", "BOU"),
	Brighton(4, "Brighton", "BHA"),
	Burnley(5, "Burnley", "BUR"),
	Chelsea(6, "Chelsea", "CHE"),
	CrystalPalace(7, "Crystal Palace", "CRY"),
	Everton(8, "Everton", "EVE"),
	Leicester(9, "Leicester", "LEI"),
	Liverpool(10, "Liverpool", "LIV"),
	ManCity(11, "Man City", "MCI"),
	ManUtd(12, "Man Utd", "MUN"),
	Newcastle(13, "Newcastle", "NEW"),
	Norwich(14, "Norwich", "NOR"),
	SheffieldUtd(15, "Sheffield Utd", "SHU"),
	Southampton(16, "Southampton", "SOU"),
	Spurs(17, "Spurs", "TOT"),
	Watford(18, "Watford", "WAT"),
	WestHam(19, "West Ham", "WHU"),
	Wolves(20, "Wolves", "WOL");

	private final int teamId;
	private final String mame;
	private final String shortName;
}

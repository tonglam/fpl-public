package com.tong.fpl.domain.letletme.scout;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/12/8
 */
@Data
@Accessors(chain = true)
public class ScoutPlayerData {

	private int element;
	private String webName;
	private int elementType;
	private String elementTypeName;
	private int teamId;
	private String teamName;
	private String teamShortName;
	private double price;
	private int fixtureEvent1;
	private String againstTeam1ShortName;
	private int difficulty1;
	private boolean wasHome1;
	private int fixtureEvent2;
	private String againstTeam2ShortName;
	private int difficulty2;
	private boolean wasHome2;
	private int fixtureEvent3;
	private String againstTeam3ShortName;
	private int difficulty3;
	private boolean wasHome3;
	private int totalPoints;
	private String pointsPerGame;
	private String selectedByPercent;

}

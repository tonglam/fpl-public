package com.tong.fpl.domain.letletme.element;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/10/11
 */
@Data
@Accessors(chain = true)
public class ElementAgainstInfoData {

    private int code;
    private String webName;
    private int elementType;
    private String elementTypeName;
    private int teamCode;
    private String teamName;
    private String teamShortName;
    private int totalPlayed;
    private int totalMinutes;
    private int totalGoalsScored;
    private int totalAssists;
    private int totalCleanSheets;
    private int totalGoalsConceded;
    private int totalOwnGoals;
    private int totalPenaltiesSaved;
    private int totalPenaltiesMissed;
    private int totalYellowCards;
    private int totalRedCards;
    private int totalSaves;
    private int totalBonus;
    private int totalBps;
    private int totalPoints;
    private double averagePoints;
    private double averageMinutes;

}

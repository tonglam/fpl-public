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
    private double averageSaves;
    private int totalBonus;
    private double averageBonus;
    private int totalBps;
    private double averageBps;
    private int totalPoints;
    private double averageMinutes;
    private double averagePoints;

}

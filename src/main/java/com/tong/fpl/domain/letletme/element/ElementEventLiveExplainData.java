package com.tong.fpl.domain.letletme.element;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/9/8
 */
@Data
@Accessors(chain = true)
public class ElementEventLiveExplainData {

    private int element;
    private String webName;
    private int elementType;
    private String elementTypeName;
    private int teamId;
    private String teamShortName;
    private String selectedByPercent;
    private int event;
    private int totalPoints;
    private int bps;
    private int bonus;
    private int minutes;
    private int minutesPoints;
    private int goalsScored;
    private int goalsScoredPoints;
    private int assists;
    private int assistsPoints;
    private int cleanSheets;
    private int cleanSheetsPoints;
    private int goalsConceded;
    private int goalsConcededPoints;
    private int ownGoals;
    private int ownGoalsPoints;
    private int penaltiesSaved;
    private int penaltiesSavedPoints;
    private int penaltiesMissed;
    private int penaltiesMissedPoints;
    private int yellowCards;
    private int yellowCardsPoints;
    private int redCards;
    private int redCardsPoints;
    private int saves;
    private int savesPoints;

}

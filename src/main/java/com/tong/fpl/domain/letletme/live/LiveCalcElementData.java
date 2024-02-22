package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/9/7
 */
@Data
@Accessors(chain = true)
public class LiveCalcElementData {

    private int element;
    private int event;
    private int livePoints;
    private int liveBonus;
    private int bps;
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

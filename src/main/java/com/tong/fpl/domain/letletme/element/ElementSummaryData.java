package com.tong.fpl.domain.letletme.element;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/10/21
 */
@Data
@Accessors(chain = true)
public class ElementSummaryData {

    private String season;
    private int element;
    private int fixtureId;
    private int event;
    private int code;
    private double price;
    private String webName;
    private int elementType;
    private String elementTypeName;
    private int teamId;
    private int teamCode;
    private String teamName;
    private String teamShortName;
    private int teamScore;
    private int againstTeamId;
    private int againstTeamCode;
    private String againstTeamName;
    private String againstTeamShortName;
    private int againstTeamScore;
    private boolean wasHome;
    private int selected;
    private int totalPoints;
    private int minutes;
    private int goalsScored;
    private int assists;
    private int cleanSheets;
    private int goalsConceded;
    private int ownGoals;
    private int penaltiesSaved;
    private int penaltiesMissed;
    private int yellowCards;
    private int redCards;
    private int saves;
    private int bonus;
    private int bps;
    private String influence;
    private String creativity;
    private String threat;
    private String ictIndex;
    private int transfersBalance;
    private int transfersIn;
    private int transfersOut;

}

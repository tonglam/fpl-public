package com.tong.fpl.domain.letletme.element;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/10/12
 */
@Data
@Accessors(chain = true)
public class ElementAgainstRecordData {
    
    private String season;
    private int event;
    private int element;
    private int code;
    private String webName;
    private double price;
    private int elementType;
    private String elementTypeName;
    private int teamHId;
    private String teamHName;
    private String teamHShortName;
    private int teamHScore;
    private int teamAId;
    private String teamAName;
    private String teamAShortName;
    private int teamAScore;
    private String kickoffDate;
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
    private int points;

}

package com.tong.fpl.domain.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/8/20
 */
@Data
@Accessors(chain = true)
public class PlayerDetailData {

    private int element;
    private String season;
    private int event;
    private int eventPoints;
    private double selectedByPercent;
    private int chanceOfPlayingNextRound;
    private int chanceOfPlayingThisRound;
    private String form;
    private String pointsPerGame;
    private boolean inDreamteam;
    private String news;
    private String newsAdded;
    private int transfersInEvent;
    private int transfersOutEvent;
    private int transfersIn;
    private int transfersOut;
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
    private int cornersAndIndirectFreekicksOrder;
    private int directFreekicksOrder;
    private int penaltiesOrder;

}

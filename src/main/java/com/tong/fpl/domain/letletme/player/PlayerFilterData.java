package com.tong.fpl.domain.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/25
 */
@Data
@Accessors(chain = true)
public class PlayerFilterData {

    private int id;
    private int code;
    private String webName;
    private String teamShortName;
    private String elementTypeName;
    private double price;
    private int points;
    private double selectedByPercent;
    private int chanceOfPlayingNextRound;
    private int chanceOfPlayingThisRound;
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
    private int bonus;
    private int bps;
    private String influence;
    private String creativity;
    private String threat;
    private String ictIndex;

}

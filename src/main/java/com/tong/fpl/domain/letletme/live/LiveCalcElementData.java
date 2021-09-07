package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/9/7
 */
@Data
@Accessors(chain = true)
public class LiveCalcElementData {

    private int event;
    private int livePoints;
    private int livebonus;
    private int playStatus;
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
    private int bps;

}

package com.tong.fpl.domain.letletme.team;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/5
 */
@Data
@Accessors(chain = true)
public class TeamDetailData {

    private int teamId;
    private String season;
    private int win;
    private int lose;
    private int draw;
    private String form;
    private int goalsScored;
    private int assists;
    private int cleanSheets;
    private int goalsConceded;
    private int yellowCards;
    private int redCards;
    private int penaltiesSaved;
    private int penaltiesMissed;
    private int saves;
    private int bonus;

}

package com.tong.fpl.domain.letletme.team;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/10/14
 */
@Data
@Accessors(chain = true)
public class TeamAgainstInfoData {

    private int teamId;
    private int teamCode;
    private String teamName;
    private String teamShortName;
    private int againstId;
    private int againstCode;
    private String againstName;
    private String againstShortName;
    private int played;
    private int win;
    private int draw;
    private int lose;
    private int goalScoreed;
    private int goalsConceded;
    private double averageGoalScoreed;
    private double averageGoalsConceded;
    private List<TeamAgainstSeasonInfoData> recordDataList;

}

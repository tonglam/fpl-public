package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/3
 */
@Data
@Accessors(chain = true)
public class TournamentGroupData {

    private int tournamentId;
    private String groupMode;
    private int groupId;
    private int groupIndex;
    private int entry;
    private String entryName;
    private String playerName;
    private int groupPoints;
    private int groupRank;
    private int play;
    private int win;
    private int draw;
    private int lose;
    private boolean qualified;
    private int overallPoints;
    private int overallRank;
    private int startGw;
    private int endGw;
    private TournamentPointsGroupEventResultData pointsGroupEventResult;

}

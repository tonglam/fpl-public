package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/10/27
 */
@Data
@Accessors(chain = true)
public class TournamentGroupChampionCountData {

    private int rank;
    private int entry;
    private String entryName;
    private String playerName;
    private int overallPoints;
    private int overallRank;
    private int championNum;
    private int runnerUpNum;
    private int secondRunnerUpNum;
    private int totalNum;

}

package com.tong.fpl.domain.letletme.summary.league;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/5/27
 */
@Data
@Accessors(chain = true)
public class LeagueSeasonEntryData {

    private int leagueId;
    private String leagueType;
    private String leagueName;
    private int entry;
    private String entryName;
    private String playerName;
    // summary
    private int overallPoints;
    private int pointsRank;
    private double value;
    private int valueRank;
    private int totalTransfers;
    private int transfersRank;
    private int totalTransfersCost;
    private int transfersCostRank;
    private int totalBenchPoints;
    private int benchPointsRank;
    private int totalAutoSubsPoints;
    private int autoSubsPointsRank;
    

}

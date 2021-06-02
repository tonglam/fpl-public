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
    private int overallRank;
    private int leagueRank;
    private double value;
    private int valueRank;
    private int transfers;
    private int transfersRank;
    private int transfersCost;
    private int transfersCostRank;
    private int benchPoints;
    private int benchPointsRank;
    private int autoSubsPoints;
    private int autoSubsPointsRank;
    // captain
    private int captainPoints;
    private int captainRank;
    private int mostCaptainPoints;
    private int mostCaptainPointsRank;
    private int tcCaptainPoints;
    private int tcCaptainPointsRank;
    // transfers
    private int mostTransfersProfit;
    private int mostTransfersProfitRank;
    private int leastTransfersProfit;
    private int leastTransfersProfitRank;
    private int mostTransfersCost;
    private int mostTransfersCostRank;
    // score
    private int gkpTotalPoints;
    private int gkpTotalPointsRank;
    private int gkpTotalNum;
    private int gkpTotalNumRank;
    private int defTotalPoints;
    private int defTotalPointsRank;
    private int defTotalNum;
    private int defTotalNumRank;
    private int midTotalPoints;
    private int midTotalPointsRank;
    private int midTotalNum;
    private int midTotalNumRank;
    private int fwdTotalPoints;
    private int fwdTotalPointsRank;
    private int fwdTotalNum;
    private int fwdTotalNumRank;

}

package com.tong.fpl.domain.letletme.summary.league;

import com.tong.fpl.domain.letletme.summary.entry.EntryAboveHundredData;
import com.tong.fpl.domain.letletme.summary.entry.EntrySeasonInfoData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/5/24
 */
@Data
@Accessors(chain = true)
public class LeagueSeasonSummaryData {

    private int leagueId;
    private String leagueType;
    // overall points
    private List<EntrySeasonInfoData> topRank;
    private int entryOverallRank;
    private int entryLeagueRank;
    // value
    private List<EntrySeasonInfoData> topValue;
    private double entryValue;
    private int entryValueRank;
    // transfers
    private List<EntrySeasonInfoData> topTransfers;
    private int entryTransfers;
    private int entryTransfersRank;
    // cost
    private List<EntrySeasonInfoData> topCost;
    private int entryCost;
    private int entryCostRank;
    // bench points
    private List<EntrySeasonInfoData> topBench;
    private int entryBenchPoints;
    private int entryBenchPointsRank;
    // autoSubs points
    private List<EntrySeasonInfoData> topAutoSubs;
    private int entryAutoSubsPoints;
    private int entryAutoSubsPointsRank;
    // above hundred
    private List<EntryAboveHundredData> topAboveHundred;
    private int entryAboveHundredTimes;
    private int entryAboveHundredRank;

}

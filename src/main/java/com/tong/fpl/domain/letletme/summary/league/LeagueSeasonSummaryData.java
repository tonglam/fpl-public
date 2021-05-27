package com.tong.fpl.domain.letletme.summary.league;

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
    private String leagueName;
    private double averageOverallPoints;
    private double topAverageOverallPoints;
    private List<EntrySeasonInfoData> topRank;
    private double averageValue;
    private double topAverageValue;
    private List<EntrySeasonInfoData> topValue;
    private double averageCost;
    private double topAverageCost;
    private List<EntrySeasonInfoData> topCost;
    private double averageBenchPoints;
    private double topAverageBenchPoints;
    private List<EntrySeasonInfoData> topBench;
    private double averageAutoSubsPoints;
    private double topAverageAutoSubsPoints;
    private List<EntrySeasonInfoData> topAutoSubs;

}

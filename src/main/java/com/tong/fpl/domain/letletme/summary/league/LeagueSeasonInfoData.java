package com.tong.fpl.domain.letletme.summary.league;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/6/3
 */
@Data
@Accessors(chain = true)
public class LeagueSeasonInfoData {

    private String leagueName;
    private double averageOverallPoints;
    private double topAverageOverallPoints;
    private double averageValue;
    private double topAverageValue;
    private double averageCost;
    private double topAverageCost;
    private double averageBenchPoints;
    private double topAverageBenchPoints;
    private double averageAutoSubsPoints;
    private double topAverageAutoSubsPoints;

}

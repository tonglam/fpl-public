package com.tong.fpl.domain.letletme.summary.league;

import com.tong.fpl.domain.letletme.summary.entry.EntrySeasonCaptainData;
import com.tong.fpl.domain.letletme.summary.entry.EntrySelectedCaptainData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/5/27
 */
@Data
@Accessors(chain = true)
public class LeagueSeasonCaptainData {

    private int leagueId;
    private String leagueType;
    private String leagueName;
    private int totalCaptain; // 队长总数
    private int totalCaptainPoints;  // 队长总得分
    private int totalViceCaptainPoints;  // 副队长总得分
    private double averageCaptainPoints; // 平均队长得分
    private double averageViceCaptainPoints; // 平均副队长得分
    private List<EntrySelectedCaptainData> mostPointsCaptain; // 得分最多队长
    private List<EntrySelectedCaptainData> mostSelectedCaptain; // 队长选择最多
    private List<EntrySelectedCaptainData> mostTcSelectedCaptain; // tc选择最多队长
    private List<EntrySeasonCaptainData> bestCaptainEntry;// 队长得分最多球队
    private List<EntrySeasonCaptainData> worstCaptainEntry;// 队长得分最少球队
    private List<EntrySeasonCaptainData> mostPointsByPercentEntry; // 队长得分占比最多球队
    private List<EntrySeasonCaptainData> leastPointsByPercentEntry; // 队长得分占比最少球队

}

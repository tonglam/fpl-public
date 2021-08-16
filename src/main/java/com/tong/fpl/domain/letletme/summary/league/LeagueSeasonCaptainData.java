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

    private String leagueName;
    private int totalCaptainNum; // 队长总数
    private double averageCaptainNum; // 平均队长数量
    private int totalCaptainPoints;  // 队长总得分
    private double averageCaptainPoints; // 平均队长得分
    private int entryCaptainTotalPoints; // 球队队长总分
    private int entryCaptainTotalPointsRank; // 球队队长总分排名
    private double entryAverageCaptainPoints; // 球队队长平均分
    private int entryAverageCaptainPointsRank; // 球队队长平均分排名
    private List<EntrySelectedCaptainData> mostPointsCaptain; // 得分最多队长
    private List<EntrySelectedCaptainData> mostSelectedCaptain; // 队长选择最多
    private List<EntrySelectedCaptainData> mostTcSelectedCaptain; // tc选择最多队长
    private List<EntrySeasonCaptainData> bestCaptainEntry;// 队长得分最多球队
    private List<EntrySeasonCaptainData> worstCaptainEntry;// 队长得分最少球队
    private List<EntrySeasonCaptainData> mostPointsByPercentEntry; // 队长得分占比最多球队
    private List<EntrySeasonCaptainData> leastPointsByPercentEntry; // 队长得分占比最少球队
    private int entryMostCaptainPoints; // 球队队长最高分
    private int entryMostCaptainPointsRank;// 球队队长最高分排名
    private boolean entryTcCaptainPlayed; // 球队tc队长是否已使用
    private int entryTcCaptainPoints; // 球队tc队长得分
    private int entryTcCaptainPointsRank; // 球队tc队长排名
    private String entryCaptainPointsByPercent; // 球队队长得分占比
    private int entryCaptainPointsByPercentRank; // 球队队长得分占比排名

}

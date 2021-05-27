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
    private double averageCaptainPoints; // 联赛平均队长得分
    private double averageEventCaptainPoints; // 联赛周平均队长得分
    private List<EntrySelectedCaptainData> mostPointsCaptain; // 联赛得分最多队长
    private List<EntrySelectedCaptainData> mostPointsViceCaptain; // 联赛得分最多副队长
    private List<EntrySelectedCaptainData> mostSelectedCaptain; // 联赛队长选择最多
    private List<EntrySelectedCaptainData> mostSelectedViceCaptain; // 联赛副队长选择最多
    private List<EntrySelectedCaptainData> mostTcSelectedCaptain; // 联赛tc选择最多队长
    private List<EntrySelectedCaptainData> leastTcSelectedCaptain; // 联赛tc选择最多队长
    private List<EntrySeasonCaptainData> bestCaptainEntry;// 联赛队长得分最多球队
    private List<EntrySeasonCaptainData> worstCaptainEntry;// 联赛队长得分最少球队
    private List<EntrySeasonCaptainData> mostPointsByPercent; // 联赛队长得分占比最多球队
    private List<EntrySeasonCaptainData> leastPointsByPercent; // 联赛队长得分占比最少球队

}

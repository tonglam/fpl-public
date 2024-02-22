package com.tong.fpl.domain.letletme.summary.entry;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/5/24
 */
@Data
@Accessors(chain = true)
public class EntrySeasonCaptainData {

    private int entry;
    private String entryName;
    private String playerName;
    private int totalPoints; // 队长总得分
    private String totalPointsByPercent; // 队长总得分占比
    private int viceTotalPoints; // 自动替补队长总得分
    private String viceTotalPointsByPercent; // 自动替补队长总得分占比
    private int mostPoints; // 队长得分最高
    private String mostPointsWebName; // 队长得分最高球员
    private int mostPointsEvent; // 队长得分最高周
    private int leastPoints; // 队长得分最低
    private String leastPointsWebName; // 队长得分最低球员
    private int leastPointsEvent; // 队长得分最低周
    private boolean tcPlayed; // 是否已开三倍卡
    private int tcPoints; // 三倍队长得分
    private String tcPointsWebName; // 三倍队长球员
    private int tcEvent; // 三倍队长周
    private List<EntrySelectedCaptainData> mostSelected; // 队长选择最多
    private List<EntrySelectedCaptainData> captainList; // 队长列表

}

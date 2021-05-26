package com.tong.fpl.domain.letletme.summary;

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
    private int maxPoints; // 队长得分最高
    private String maxPointsWebName; // 队长得分最高球员
    private int maxPointsEvent; // 队长得分最高周
    private int minPoints; // 队长得分最低
    private String minPointsWebName; // 队长得分最低球员
    private int minPointsEvent; // 队长得分最低周
    private int tcPoints; // 三倍队长得分
    private String tcPointsWebName; // 三倍队长球员
    private int tcEvent; // 三倍队长周
    private List<EntrySelectedCaptainData> mostSelected; // 队长选择最多

}

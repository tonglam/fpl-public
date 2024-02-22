package com.tong.fpl.domain.letletme.summary.entry;

import com.tong.fpl.domain.letletme.entry.EntryAutoSubsData;
import com.tong.fpl.domain.letletme.entry.EntryBenchData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/5/24
 */
@Data
@Accessors(chain = true)
public class EntrySeasonSummaryData {

    private int entry;
    private String entryName;
    private String playerName;
    private int highestScore; // 最高分
    private int highestEvent; // 最高分周
    private int lowestScore; // 最低分
    private int lowestEvent; // 最低分周
    private int highestOverallRank; // 最高排名
    private int highestOverallRankEvent; // 最高排名周
    private int lowestOverallRank; // 最低排名
    private int lowestOverallRankEvent; // 最低排名周
    private List<EntryBelowAverageData> belowAverages; // 低于平均分
    private List<EntryAboveHundredData> aboveHundred; // 破百
    private List<EntryBenchData> highestBench; // 最高板凳得分
    private List<EntryAutoSubsData> highestAutoSubs; // 最高自动替补
    private List<EntryChipData> chips; // 开卡结果

}

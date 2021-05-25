package com.tong.fpl.domain.letletme.summary;

import com.tong.fpl.domain.letletme.entry.EntryEventAutoSubsData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

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
    private Map<Integer, Integer> belowAverageEvents; // 低于平均分
    private int highestBenchPoints; // 最高板凳得分
    private int highestBenchPointsEvent; // 最高板凳得分周
    private int highestAutoSubsPoints; // 最高自动替补得分
    private List<EntryEventAutoSubsData> highestAutoSubs; // 最高自动替补

}

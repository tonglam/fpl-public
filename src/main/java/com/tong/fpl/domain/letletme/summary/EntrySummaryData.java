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
public class EntrySummaryData {

    private int highestScore;
    private int highestEvent;
    private int lowestScore;
    private int lowestEvent;
    private int highestOverallRank;
    private int highestOverallRankEvent;
    private int lowestOverallRank;
    private int lowestOverallRankEvent;
    private Map<Integer, Integer> belowAverageEvents;
    private int highestBenchPoints;
    private int highestBenchPointsEvent;
    private List<EntryEventAutoSubsData> highestAutoSubsPoints;

}

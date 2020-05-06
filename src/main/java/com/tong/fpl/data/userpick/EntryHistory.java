package com.tong.fpl.data.userpick;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Create by tong on 2020/3/11
 */
@Data
public class EntryHistory {
    private int event;
    private int point;
    @JsonProperty("total_points")
    private int totalPoints;
    private int rank;
    @JsonProperty("rank_sort")
    private int rankSort;
    @JsonProperty("overall_rank")
    private int overallRank;
    private int bank;
    private int value;
    @JsonProperty("event_transfers")
    private int eventTransfers;
    @JsonProperty("event_transfers_cost")
    private int eventTransfersCost;
    @JsonProperty("points_on_bench")
    private int pointsOnBench;
}

package com.tong.fpl.domain.letletme.summary.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/5/24
 */
@Data
@Accessors(chain = true)
public class EntrySeasonInfoData {

    private int event;
    private int entry;
    private String entryName;
    private String playerName;
    private int overallPoints;
    private int overallRank;
    private double value;
    private double bank;
    private double teamValue;
    private int totalTransfers;
    private int totalTransfersCost;
    private int totalBenchPoints;
    private int totalAutoSubsPoints;

}

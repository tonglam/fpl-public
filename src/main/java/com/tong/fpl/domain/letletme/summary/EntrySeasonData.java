package com.tong.fpl.domain.letletme.summary;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/5/24
 */
@Data
@Accessors(chain = true)
public class EntrySeasonData {

    private int entry;
    private String entryName;
    private String playerName;
    private int overallPoints;
    private int overallRank;
    private int totalTransfers;
    private double value;
    private double bank;
    private double teamValue;
    private EntrySummaryData summaryData;
    private EntryTransfersData transfersData;
    private EntryCaptainData captainData;
    private EntryScoreData scoreData;

}

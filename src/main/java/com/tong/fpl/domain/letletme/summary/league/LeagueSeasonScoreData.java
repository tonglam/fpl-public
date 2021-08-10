package com.tong.fpl.domain.letletme.summary.league;

import com.tong.fpl.domain.letletme.summary.entry.EntryElementTypeScoreData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Create by tong on 2021/5/27
 */
@Data
@Accessors(chain = true)
public class LeagueSeasonScoreData {

    private String leagueName;
    private int totalOverallPoints;
    private double averageOverallPoints;
    // gkp
    private int gkpTotalPoints;
    private String gkpTotalPointsByPercent;
    private LinkedHashMap<String, String> mostSelectedGkpByPercent;
    private double averageEntryGkpTotalNum;
    private double averageEntryGkpTotalPoints;
    private List<EntryElementTypeScoreData> mostEntryGkpPoints;
    private int entryGkpTotalPoints;
    private int entryGkpTotalPointsRank;
    private int entryGkpTotalNum;
    private int entryGkpTotalNumRank;
    // def
    private int defTotalPoints;
    private String defTotalPointsByPercent;
    private LinkedHashMap<String, String> mostSelectedDefByPercent;
    private double averageEntryDefTotalNum;
    private double averageEntryDefTotalPoints;
    private List<EntryElementTypeScoreData> mostEntryDefPoints;
    private int entryDefTotalPoints;
    private int entryDefTotalPointsRank;
    private int entryDefTotalNum;
    private int entryDefTotalNumRank;
    // mid
    private int midTotalPoints;
    private String midTotalPointsByPercent;
    private LinkedHashMap<String, String> mostSelectedMidByPercent;
    private double averageEntryMidTotalNum;
    private double averageEntryMidTotalPoints;
    private List<EntryElementTypeScoreData> mostEntryMidPoints;
    private int entryMidTotalPoints;
    private int entryMidTotalPointsRank;
    private int entryMidTotalNum;
    private int entryMidTotalNumRank;
    // fwd
    private int fwdTotalPoints;
    private String fwdTotalPointsByPercent;
    private LinkedHashMap<String, String> mostSelectedFwdByPercent;
    private double averageEntryFwdTotalNum;
    private double averageEntryFwdTotalPoints;
    private List<EntryElementTypeScoreData> mostEntryFwdPoints;
    private int entryFwdTotalPoints;
    private int entryFwdTotalPointsRank;
    private int entryFwdTotalNum;
    private int entryFwdTotalNumRank;
    // formation
    private LinkedHashMap<String, String> mostSelectedFormation; // formation -> percent

}

package com.tong.fpl.domain.letletme.summary.league;

import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;

/**
 * Create by tong on 2021/5/27
 */
@Data
@Accessors(chain = true)
public class LeagueSeasonScoreData {

    private int leagueId;
    private String leagueType;
    private String leagueName;
    private int totalOverallPoints;
    private double averageOverallPoints;
    // gkp
    private int gkpTotalPoints;
    private String gkpTotalPointsByPercent;
    private LinkedHashMap<String, String> mostSelectedGkpByPercent;
    private double averageEntryGkpTotalNum;
    private double averageEntryGkpTotalPoints;
    private LinkedHashMap<EntryInfoData, Integer> mostEntryGkpPoints;
    // def
    private int defTotalPoints;
    private String defTotalPointsByPercent;
    private LinkedHashMap<String, String> mostSelectedDefByPercent;
    private double averageEntryDefTotalNum;
    private double averageEntryDefTotalPoints;
    private LinkedHashMap<EntryInfoData, Integer> mostEntryDefPoints;
    // mid
    private int midTotalPoints;
    private String midTotalPointsByPercent;
    private LinkedHashMap<String, String> mostSelectedMidByPercent;
    private double averageEntryMidTotalNum;
    private double averageEntryMidTotalPoints;
    private LinkedHashMap<EntryInfoData, Integer> mostEntryMidPoints;
    // fwd
    private int fwdTotalPoints;
    private String fwdTotalPointsByPercent;
    private LinkedHashMap<String, String> mostSelectedFwdByPercent;
    private double averageEntryFwdTotalNum;
    private double averageEntryFwdTotalPoints;
    private LinkedHashMap<EntryInfoData, Integer> mostEntryFwdPoints;
    // formation
    private LinkedHashMap<String, String> mostSelectedFormation; // formation -> percent

}

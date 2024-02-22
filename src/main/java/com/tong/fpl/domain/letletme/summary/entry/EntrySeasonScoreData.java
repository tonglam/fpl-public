package com.tong.fpl.domain.letletme.summary.entry;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;

/**
 * Create by tong on 2021/5/24
 */
@Data
@Accessors(chain = true)
public class EntrySeasonScoreData {

    private int entry;
    private String entryName;
    private String playerName;
    private int overallPoints;
    // gkp
    private int gkpTotalPoints;
    private String gkpTotalPointsByPercent;
    private int gkpTotalNum;
    private LinkedHashMap<String, Long> mostSelectedGkp;
    // def
    private int defTotalPoints;
    private String defTotalPointsByPercent;
    private int defTotalNum;
    private LinkedHashMap<String, Long> mostSelectedDef;
    // mid
    private int midTotalPoints;
    private String midTotalPointsByPercent;
    private int midTotalNum;
    private LinkedHashMap<String, Long> mostSelectedMid;
    // fwd
    private int fwdTotalPoints;
    private String fwdTotalPointsByPercent;
    private int fwdTotalNum;
    private LinkedHashMap<String, Long> mostSelectedFwd;
    // formation
    private LinkedHashMap<String, Long> mostSelectedFormation;

}

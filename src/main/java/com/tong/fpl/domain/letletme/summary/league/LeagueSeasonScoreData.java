package com.tong.fpl.domain.letletme.summary.league;

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
    private double averageGkpTotalPoints;
    private String averageGkpTotalPointsByPercent;
    private double averageGkpTotalNum;
    private LinkedHashMap<String, Long> mostSelectedGkp;

    // def
    private int defTotalPoints;
    private String defTotalPointsByPercent;
    private double averageDefTotalPoints;
    private String averageDefTotalPointsByPercent;
    private double averageDefTotalNum;
    private LinkedHashMap<String, Long> mostSelectedDef;

    // mid
    private int midTotalPoints;
    private String midTotalPointsByPercent;
    private double averageMidTotalPoints;
    private String averageMidTotalPointsByPercent;
    private double averageMidTotalNum;
    private LinkedHashMap<String, Long> mostSelectedMid;

    // fwd
    private int fwdTotalPoints;
    private String fwdTotalPointsByPercent;
    private double averageFwdTotalPoints;
    private String averageFwdTotalPointsByPercent;
    private double averageFwdTotalNum;
    private LinkedHashMap<String, Long> mostSelectedFwd;

    // formation
    private LinkedHashMap<String, Long> mostSelectedFormation;

}

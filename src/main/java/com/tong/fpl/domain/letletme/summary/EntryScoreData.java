package com.tong.fpl.domain.letletme.summary;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/5/24
 */
@Data
@Accessors(chain = true)
public class EntryScoreData {

    private int gkpTotalPoints;
    private String gkpTotalPointsByPercent;
    private int defTotalPoints;
    private String defTotalPointsByPercent;
    private int midTotalPoints;
    private String midTotalPointsByPercent;
    private int fwdTotalPoints;
    private String fwdTotalPointsByPercent;
    private int mostSelectedGkp;
    private String mostSelectedGkpName;
    private int mostSelectedDef;
    private String mostSelectedDefName;
    private int mostSelectedMid;
    private String mostSelectedMidName;
    private int mostSelectedFwd;
    private String mostSelectedFwdName;
    private String mostSelectedFormation;

}

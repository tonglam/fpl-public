package com.tong.fpl.domain.letletme.league;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/12/30
 */
@Data
@Accessors(chain = true)
public class LeagueEventScoringData {

	private int benchTotalPoints;
	private int autoSubsTotalPoints;
	private String autoSubsTotalPointsByPercent;
	private int gkpTotalPoints;
	private String gkpTotalPointsByPercent;
	private int defTotalPoints;
	private String defTotalPointsByPercent;
	private int midTotalPoints;
	private String midTotalPointsByPercent;
	private int fwdTotalPoints;
	private String fwdTotalPointsByPercent;
	private int captainTotalPoints;
	private String captainTotalPointsByPercent;
	private int mostSelectedGkp;
	private String mostSelectedGkpName;
	private int mostSelectedDef;
	private String mostSelectedDefName;
	private int mostSelectedMid;
	private String mostSelectedMidName;
	private int mostSelectedFwd;
	private String mostSelectedFwdName;
	private int mostSelectedCaptain;
	private String mostSelectedCaptainName;
	private String mostSelectedFormation;

}

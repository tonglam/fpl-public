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
	private int mostSelectedGkp;
	private int gkpTotalPoints;
	private int mostSelectedDef;
	private int defTotalPoints;
	private int mostSelectedMid;
	private int midTotalPoints;
	private int mostSelectedFwd;
	private int fwdTotalPoints;
	private String mostSelectedFormation;

}

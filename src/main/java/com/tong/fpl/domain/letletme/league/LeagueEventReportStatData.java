package com.tong.fpl.domain.letletme.league;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/11/10
 */
@Data
@Accessors(chain = true)
public class LeagueEventReportStatData {

	private int entry;
	private String entryName;
	private String playerName;
	private int overallPoints;
	private int overallRank;
	private int teamValue;
	private int bank;
	private LeagueEventCaptainData captainData;

}

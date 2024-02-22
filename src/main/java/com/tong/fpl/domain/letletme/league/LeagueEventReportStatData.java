package com.tong.fpl.domain.letletme.league;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/11/10
 */
@Data
@Accessors(chain = true)
public class LeagueEventReportStatData {

	private int rank;
	private int entry;
	private String entryName;
	private String playerName;
	private int overallPoints;
	private int overallRank;
	private int teamValue;
	private int bank;
	private int transfers;
	private int transfersPlayed;
	private int transfersCost;
	private int captainTotalPoints;
	private int transferTotalNetPoints;
	private LeagueEventCaptainData captainData;
	private LeagueEventTransferData transferData;
	private LeagueEventScoringData scoringData;

}

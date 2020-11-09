package com.tong.fpl.domain.letletme.league;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/2
 */
@Data
@Accessors(chain = true)
public class LeagueEventReportData {

	private int entry;
	private String entryName;
	private String playerName;
	private int overallPoints;
	private int overallRank;
	private int teamValue;
	private int bank;
	private int event;
	private int eventPoints;
	private int eventTransfers;
	private int eventTransfersCost;
	private int eventNetPoints;
	private int eventBenchPoints;
	private int eventRank;
	private String eventChip;
	private int captain;
	private String captainWebName;
	private int captainPoints;
	private boolean captainBlank;
	private String captainSelected;
	private String captainPointsByPercent;
	private int viceCaptain;
	private String viceCaptainWebName;
	private int viceCaptainPoints;
	private boolean viceCaptainBlank;
	private String viceCaptainSelected;
	private String viceCaptainPointsByPercent;
	private int highestScore;
	private String highestScoreWebName;
	private int highestScorePoints;
	private boolean highestScoreBlank;
	private String highestScoreSelected;
	private String highestScorePointsByPercent;


}

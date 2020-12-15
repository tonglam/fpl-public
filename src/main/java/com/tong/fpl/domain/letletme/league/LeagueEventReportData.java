package com.tong.fpl.domain.letletme.league;

import com.tong.fpl.domain.letletme.entry.EntryEventTransferData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/9/2
 */
@Data
@Accessors(chain = true)
public class LeagueEventReportData {

	private int rank;
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
	private String captainSelectedInLeague;
	private String captainPointsByPercent;
	private int viceCaptain;
	private String viceCaptainWebName;
	private int viceCaptainPoints;
	private boolean viceCaptainBlank;
	private String viceCaptainSelected;
	private String viceCaptainSelectedInLeague;
	private String viceCaptainPointsByPercent;
	private int highestScore;
	private String highestScoreWebName;
	private int highestScorePoints;
	private boolean highestScoreBlank;
	private String highestScoreSelected;
	private String highestScoreSelectedInLeague;
	private String highestScorePointsByPercent;
	private int transferInTotalPoints;
	private int transferOutTotalPoints;
	private int transferPoints;
	private int transferNetPoints;
	private int transferInTotalValue;
	private int transferOutTotalValue;
	private int transferValue;
	private List<EntryEventTransferData> entryEventTransferList;

}

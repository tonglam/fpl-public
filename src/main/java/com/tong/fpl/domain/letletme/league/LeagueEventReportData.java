package com.tong.fpl.domain.letletme.league;

import com.tong.fpl.domain.letletme.entry.EntryEventAutoSubsData;
import com.tong.fpl.domain.letletme.entry.EntryEventTransfersData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
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
	private int event;
	private String entryName;
	private String playerName;
	private int overallPoints;
	private int overallRank;
	private int teamValue;
	private int bank;
	private int eventPoints;
	private int eventTransfers;
	private int eventTransfersCost;
	private int eventTransfersPlayed;
	private int eventNetPoints;
	private int eventBenchPoints;
	private int eventAutoSubPoints;
	private String eventAutoSubPointsByPercent;
	private int eventRank;
	private String eventChip;
	// captain
	private int captain;
	private int playedCaptain;
	private String captainWebName;
	private int captainPoints;
	private boolean captainBlank;
	private String captainSelected;
	private String captainEffectiveOwnerShipRate;
	private String captainPointsByPercent;
	private int highestScore;
	private String highestScoreWebName;
	private int highestScorePoints;
	private boolean highestScoreBlank;
	private String highestScoreSelected;
	private String highestScoreEffectiveOwnerShipRate;
	private String highestScorePointsByPercent;
	// transfers
	private int transferInTotalPoints;
	private int transferInPlayedTotalPoints;
	private int transferOutTotalPoints;
	private int transferPoints;
	private int transferPlayedPoints;
	private int transferNetPoints;
	private int transferInTotalValue;
	private int transferOutTotalValue;
	private int transferValue;
	private List<EntryEventTransfersData> entryEventTransferList;
	// scoring
	private int gkpPoints;
	private String gkpPointsByPercent;
	private int defPoints;
	private String defPointsByPercent;
	private int midPoints;
	private String midPointsByPercent;
	private int fwdPoints;
	private String fwdPointsByPercent;
	private int playedNum;
	private int autoSubNum;
	private String formation;
	private List<EntryPickData> entryEventPickList;
	private List<EntryPickData> entryEventBenchList;
	private List<EntryEventAutoSubsData> entryEventAutoSubsList;

}

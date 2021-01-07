package com.tong.fpl.domain.letletme.live;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/7/13
 */
@Data
@Accessors(chain = true)
public class LiveCalcData {

	private int rank;
	private int event;
	private int entry;
	private String entryName;
	private String playerName;
	private String region;
	private int startedEvent;
	private int overallPoints;
	private int overallRank;
	private int bank;
	private int teamValue;
	private int totalTransfers;
	private int lastOverallPoints;
	private int lastOverallRank;
	private int lastTeamValue;
	private String chip;
	private int livePoints;
	private int transferCost;
	private int liveNetPoints;
	private int liveTotalPoints;
	private int played;
	private int toPlay;
	private String captainName;
	private List<ElementEventResultData> pickList;

}

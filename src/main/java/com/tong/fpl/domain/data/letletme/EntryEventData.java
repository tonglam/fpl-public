package com.tong.fpl.domain.data.letletme;

import com.tong.fpl.domain.data.userpick.Pick;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/8/3
 */
@Data
@Accessors(chain = true)
public class EntryEventData {

	private int entry;
	private String entryName;
	private String playerName;
	private String region;
	private int event;
	private int eventPoints;
	private int eventTransfers;
	private int eventTransfersCost;
	private int eventNetPoints;
	private int eventBenchPoints;
	private int eventRank;
	private String eventChip;
	private List<Pick> eventPicks;
	private boolean eventFinished;
	private int overallPoints;
	private int overallRank;
	private int bank;
	private int teamValue;
	private int totalTransfers;

}

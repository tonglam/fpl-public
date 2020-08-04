package com.tong.fpl.domain.data.letletme;

import com.tong.fpl.domain.data.userpick.Pick;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/8/3
 */
@Data
public class EntryEventResultData {

	private int event;
	private int eventPoints;
	private int eventTransfers;
	private int eventTransfersCost;
	private int eventNetPoints;
	private int eventBenchPoints;
	private int eventRank;
	private String eventChip;
	private boolean eventFinished;
	private List<Pick> eventPicks;

}

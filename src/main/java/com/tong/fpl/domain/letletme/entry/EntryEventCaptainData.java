package com.tong.fpl.domain.letletme.entry;

import com.tong.fpl.domain.letletme.element.ElementCaptainData;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/2
 */
@Data
@Accessors(chain = true)
public class EntryEventCaptainData {

	private int event;
	private int entry;
	private String entryName;
	private String playerName;
	private int transfers;
	private int points;
	private int transfersCost;
	private int netPoints;
	private int benchPoints;
	private int rank;
	private String chip;
	private ElementCaptainData captainData;
	private ElementCaptainData viceCaptainData;
	private ElementCaptainData highestScoreData;

}

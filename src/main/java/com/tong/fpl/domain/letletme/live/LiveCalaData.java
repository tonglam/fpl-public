package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/7/13
 */
@Data
@Accessors(chain = true)
public class LiveCalaData {

	private int event;
	private int entry;
	private List<ElementLiveData> pickList;
	private String chip;
	private int livePoints;
	private int transferCost;
	private int liveNetPoints;
	private boolean eventFinished;

}

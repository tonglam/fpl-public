package com.tong.fpl.domain.data.eventLive;

import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/3/11
 */
@Data
public class Element {

	private int id;
	private ElementStat stats;
	private List<ElementExplain> explain;

}

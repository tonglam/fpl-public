package com.tong.fpl.domain.data.eventLive;

import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/3/11
 */
@Data
public class ElementExplain {

	private int fixture;
	private List<ExplainStat> stats;

}

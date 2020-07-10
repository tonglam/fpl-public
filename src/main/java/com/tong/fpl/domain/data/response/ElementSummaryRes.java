package com.tong.fpl.domain.data.response;

import com.tong.fpl.domain.data.elementSummary.ElementFixtures;
import com.tong.fpl.domain.data.elementSummary.ElementHistory;
import com.tong.fpl.domain.data.elementSummary.ElementHistoryPast;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/7/7
 */
@Data
public class ElementSummaryRes {

	private List<ElementFixtures> fixtures;
	private List<ElementHistory> history;
	private List<ElementHistoryPast> historyPast;

}

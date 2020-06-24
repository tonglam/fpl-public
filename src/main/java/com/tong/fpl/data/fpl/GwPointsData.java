package com.tong.fpl.data.fpl;

import com.tong.fpl.data.userpick.Pick;
import lombok.Data;

import java.util.List;

/**
 * Create by tong on 2020/3/10
 */
@Data
public class GwPointsData {
    private int event;
    private int entry;
    private int gwPoint;
    private int eventCost;
    private int netPoint;
    private int totalPoints;
    private int overallRank;
    private String activeChips;
	private List<Pick> picks;
}

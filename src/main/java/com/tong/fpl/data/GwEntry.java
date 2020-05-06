package com.tong.fpl.data;

import lombok.Data;

/**
 * Create by tong on 2020/3/10
 */
@Data
public class GwEntry {
    private int event;
    private int entry;
    private String entryName;
    private String playerName;
    private int gwPoint;
    private int eventCost;
    private int netPoint;
    private int totalPoints;
    private int overallRank;
    private String activeChips;
}

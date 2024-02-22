package com.tong.fpl.domain.letletme.summary.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/6/2
 */
@Data
@Accessors(chain = true)
public class EntryAboveHundredData {

    private int event;
    private int entry;
    private String entryName;
    private String playerName;
    private int point;
    private int transfers;
    private int cost;
    private int netPoints;
    private String chip;

}

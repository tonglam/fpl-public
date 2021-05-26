package com.tong.fpl.domain.letletme.summary.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/5/25
 */
@Data
@Accessors(chain = true)
public class EntryChipData {

    private int event;
    private String name;
    private int eventPoints;
    private int profit;

}

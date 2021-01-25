package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/8/3
 */
@Data
@Accessors(chain = true)
public class EntryEventResultData {

    private int entry;
    private int event;
    private int transfers;
    private int points;
    private int transfersCost;
    private int netPoints;
    private int benchPoints;
    private int rank;
    private String chip;
    private int teamValue;
    private int bank;
    private List<EntryPickData> picks;

}

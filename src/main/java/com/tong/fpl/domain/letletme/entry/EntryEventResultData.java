package com.tong.fpl.domain.letletme.entry;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/8/3
 */
@Data
@Accessors(chain = true)
public class EntryEventResultData {

    private int event;
    private int entry;
    private int transfers;
    private int points;
    private int transfersCost;
    private int netPoints;
    private int benchPoints;
    private int rank;
    private String chip;
    private double value;
    private double bank;
    private double teamValue;
    private int overallPoints;
    private int overallRank;
    private List<EntryPickData> picks;
    private String captainName;
    private List<ElementEventResultData> pickList;

}

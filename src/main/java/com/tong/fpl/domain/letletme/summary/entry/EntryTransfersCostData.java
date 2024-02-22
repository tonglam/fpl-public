package com.tong.fpl.domain.letletme.summary.entry;

import com.tong.fpl.domain.letletme.entry.EntryEventTransfersData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/5/26
 */
@Data
@Accessors(chain = true)
public class EntryTransfersCostData {

    private int event;
    private int transfers;
    private int points;
    private int cost;
    private int netPoints;
    private int profit;
    private List<EntryEventTransfersData> transfersList;

}

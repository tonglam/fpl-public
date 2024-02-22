package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/1/11
 */
@Data
@Accessors(chain = true)
public class EntryEventSimulateTransfersData {

    private int event;
    private int entry;
    private int operator;
    private int teamValue;
    private int bank;
    private int freeTransfers;
    private int transfersCost;
    private List<EntryPickData> lineup;

}

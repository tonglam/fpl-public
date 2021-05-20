package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/1/28
 */
@Data
@Accessors(chain = true)
public class EntryEventSimulatePickData {

    private int event;
    private int entry;
    private int operator;
    private List<EntryPickData> lineup;

}

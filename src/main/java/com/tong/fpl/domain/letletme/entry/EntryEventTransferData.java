package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/11/10
 */
@Data
@Accessors(chain = true)
public class EntryEventTransferData {

    private int entry;
    private int event;
    private int elementIn;
    private String elementInName;
    private int elementInType;
    private String elementInTypeName;
    private int elementInCost;
    private int elementInPoints;
    private int elementOut;
    private String elementOutName;
    private int elementOutType;
    private String elementOutTypeName;
    private int elementOutCost;
    private int elementOutPoints;
    private String time;

}

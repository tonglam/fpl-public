package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/11/10
 */
@Data
@Accessors(chain = true)
public class EntryEventTransfersData {

    private int event;
    private int entry;
    private int elementIn;
    private String elementInWebName;
    private int elementInType;
    private String elementInTypeName;
    private int elementInTeamId;
    private String elementInTeamName;
    private String elementInTeamShortName;
    private double elementInCost;
    private int elementInPoints;
    private boolean elementInPlayed;
    private int elementOut;
    private String elementOutWebName;
    private int elementOutTeamId;
    private String elementOutTeamName;
    private String elementOutTeamShortName;
    private int elementOutType;
    private String elementOutTypeName;
    private double elementOutCost;
    private int elementOutPoints;
    private String time;

}

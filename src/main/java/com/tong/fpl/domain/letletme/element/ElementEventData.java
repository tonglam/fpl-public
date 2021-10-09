package com.tong.fpl.domain.letletme.element;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/9/9
 */
@Data
@Accessors(chain = true)
public class ElementEventData {

    private int event;
    private int element;
    private int code;
    private String webName;
    private int elementType;
    private String elementTypeName;
    private int teamId;
    private String teamShortName;
    private int points;
    private int totalPoints;
    private String selectedByPercent;
    private int transfersInEvent;
    private int transfersOutEvent;

}

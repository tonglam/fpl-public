package com.tong.fpl.domain.letletme.event;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/9/9
 */
@Data
@Accessors(chain = true)
public class EventDreamTeamData {

    private int event;
    private int element;
    private int code;
    private String webName;
    private int elementType;
    private String elementTypeName;
    private int teamId;
    private String teamShortName;
    private int points;
    private String selectedByPercent;

}

package com.tong.fpl.domain.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/23
 */
@Data
@Accessors(chain = true)
public class PlayerValueData {

    private int element;
    private String webName;
    private int teamId;
    private String teamName;
    private String teamShortName;
    private int elementType;
    private String elementTypeName;
    private int event;
    private double value;
    private String changeDate;
    private String changeType;
    private double lastValue;

}

package com.tong.fpl.domain.web;

import lombok.Data;

@Data
public class PlayerValueData {

    private int element;
    private String webName;
    private int elementType;
    private String elementTypeName;
    private int event;
    private int value;
    private int lastValue;
    private String changeDate;
    private String changeType;
    private String selected;
    private String lastSelected;

}

package com.tong.fpl.domain.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/8/20
 */
@Data
@Accessors(chain = true)
public class PlayerInfoData {

    private int element;
    private int code;
    private String webName;
    private int elementType;
    private String elementTypeName;
    private int teamId;
    private String teamName;
    private String teamShortName;
    private double price;
    private double startPrice;
    private int points;
    private String selectedByPercent;

}

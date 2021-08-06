package com.tong.fpl.domain.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/7/29
 */
@Data
@Accessors(chain = true)
public class PlayerSummaryData {

    private int element;
    private int code;
    private double price;
    private int elementType;
    private String elementTypeName;
    private String webName;
    private int teamId;
    private String teamName;
    private String teamShortName;
    private PlayerDetailData detailData;
    private List<PlayerFixtureData> fixtureList;

}

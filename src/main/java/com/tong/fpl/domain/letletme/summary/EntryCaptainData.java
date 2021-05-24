package com.tong.fpl.domain.letletme.summary;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Create by tong on 2021/5/24
 */
@Data
@Accessors(chain = true)
public class EntryCaptainData {

    private int totalPoints;
    private String totalPointsByPercent;
    private int viceTotalPoints;
    private int maxPointsEvent;
    private int maxPoints;
    private String maxPointsWebName;
    private int minPointsEvent;
    private int minPoints;
    private String minPointsWebName;
    private int tcEvent;
    private int tcPoints;
    private String tcPointsWebName;
    private Map<String, Integer> mostSelected;

}

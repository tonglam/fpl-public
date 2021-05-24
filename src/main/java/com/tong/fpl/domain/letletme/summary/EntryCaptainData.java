package com.tong.fpl.domain.letletme.summary;

import lombok.Data;
import lombok.experimental.Accessors;

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
    private int mostSelected;
    private String mostSelectedWebName;
    private int mostSelectedTimes;
    private int blankTimes;
    private int hitTimes;

}

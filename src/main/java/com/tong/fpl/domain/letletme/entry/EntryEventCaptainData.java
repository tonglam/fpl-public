package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/6/3
 */
@Data
@Accessors(chain = true)
public class EntryEventCaptainData {

    private int event;
    private int entry;
    private int captain;
    private int viceCaptain;
    private int playedCaptain;
    private int playedCaptainPoints;
    private int captainPoints;
    private int points;
    private String chip;
    private int overallPoints;

}

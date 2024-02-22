package com.tong.fpl.domain.special;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2022/2/26
 */
@Data
@Accessors(chain = true)
public class EventGroupResultData {

    private int groupId;
    private String groupName;
    private int entry;
    private String entryName;
    private String playerName;
    private int points;
    private int totalGoalScored;
    private int totalGoalsConceded;
    private String chip;
    private int groupPoints;

}

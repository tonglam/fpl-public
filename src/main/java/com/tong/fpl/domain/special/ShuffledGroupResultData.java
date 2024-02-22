package com.tong.fpl.domain.special;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2022/2/26
 */
@Data
@Accessors(chain = true)
public class ShuffledGroupResultData {

    private int shuffledGroupId;
    private int entry;
    private String entryName;
    private String playerName;
    private int points;
    private int playedCaptain;
    private String captainName;
    private int totalGoalScored;
    private int totalGoalsConceded;
    private String chip;
    private int groupRank;
    private int groupPoints;

}

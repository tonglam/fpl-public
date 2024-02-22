package com.tong.fpl.domain.special;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Created by tong on 2022/02/25
 */
@Data
@Accessors(chain = true)
public class EventResultData {

    private int groupId;
    private int shuffledGroupId;
    private int event;
    private int entry;
    private String entryName;
    private String playerName;
    private int points;
    private int netPoints;
    private String chip;
    private Integer playedCaptain;
    private String captainName;
    private int captainPoints;
    private int totalGoalScored;
    private int totalGoalsConceded;
    private List<EntryPickData> pickResultList;
    private List<ElementEventResultData> elementEventResultList;
    private int groupRank;
    private int groupPoints;

}

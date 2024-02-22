package com.tong.fpl.domain.letletme.groupTournament;

import com.tong.fpl.domain.special.EventResultData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 5/11/2023
 */
@Data
@Accessors(chain = true)
public class GroupTournamentResultData {

    private int groupId;
    private String groupName;
    private int captainId;
    private int captainPoints;
    private List<Integer> entryList;
    private List<EventResultData> eventResultList;
    private int totalGroupPoints;
    private int totalGroupCost;
    private int rank;
    private String remarks;
    private String updateTime;

}

package com.tong.fpl.domain.special;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2022/2/25
 */
@Data
@Accessors(chain = true)
public class GroupResultData {

    private int groupId;
    private String groupName;
    private int totalPoints;
    private int totalGroupPoints;
    private List<EventResultData> groupEntryList;
    private int rank;

}

package com.tong.fpl.domain.special;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2022/2/26
 */
@Data
@Accessors(chain = true)
public class GroupRankResultData {

    private int rank;
    private String groupName;
    private int totalGroupPoints;
    private int totalPoints;
    private int totalCaptainPoints;
    private String details;

}

package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/30
 */
@Data
@Accessors(chain = true)
public class EntryHistoryInfoData {

    private int entry;
    private String season;
    private int totalPoints;
    private int overallRank;

}

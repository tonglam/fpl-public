package com.tong.fpl.domain.special;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/02/25
 */
@Data
@Accessors(chain = true)
public class EntryInfoData {

    private int groupId;
    private int shuffledGroupId;
    private int entry;
    private String entryName;
    private String playerName;
    private int overallPoints;
    private int overallRank;

}

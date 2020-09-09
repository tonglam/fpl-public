package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/8/3
 */
@Data
@Accessors(chain = true)
public class EntryEventData {

    private int entry;
    private String entryName;
    private String playerName;
    private String region;
    private int overallPoints;
    private int overallRank;
    private int bank;
    private int teamValue;
    private int totalTransfers;
    private List<EntryEventResultData> eventResultList;

}

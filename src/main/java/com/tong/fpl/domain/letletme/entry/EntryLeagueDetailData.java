package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/5/14
 */
@Data
@Accessors(chain = true)
public class EntryLeagueDetailData {

    private int leagueId;
    private String leagueType;
    private String name;
    private int startEvent;
    private int entryRank;
    private int entryLastRank;

}

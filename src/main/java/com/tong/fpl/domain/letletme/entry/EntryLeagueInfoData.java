package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/8/29
 */
@Data
@Accessors(chain = true)
public class EntryLeagueInfoData {

    private int entry;
    private int leagueId;
    private String type;
    private String leagueType;
    private String leagueName;
    private int entryRank;
    private int entryLastRank;
    private int startEvent;

}

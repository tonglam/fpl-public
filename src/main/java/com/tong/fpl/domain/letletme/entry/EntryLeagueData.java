package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/5/14
 */
@Data
@Accessors(chain = true)
public class EntryLeagueData {

    private int entry;
    private int event;
    private List<EntryLeagueInfoData> classic;
    private List<EntryLeagueInfoData> h2h;
    private List<EntryLeagueInfoData> publicLeague;
    private List<EntryCupData> cup;

}

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
    private List<EntryLeagueInfoData> classic;
    private List<EntryLeagueInfoData> publicClassic;
    private List<EntryLeagueInfoData> h2h;
    private List<EntryLeagueInfoData> publicH2h;
    private List<EntryCupData> cup;

}

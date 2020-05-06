package com.tong.fpl.data.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tong.fpl.data.entry.Standings;
import com.tong.fpl.data.leaguesClassic.ClassicInfo;
import com.tong.fpl.data.leaguesClassic.NewEntries;
import lombok.Data;

/**
 * Create by tong on 2020/3/10
 */
@Data
public class LeagueClassicRes {
    private ClassicInfo league;
    @JsonProperty("new_entries")
    private NewEntries newEntries;
    private Standings standings;
}

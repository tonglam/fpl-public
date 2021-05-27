package com.tong.fpl.domain.letletme.summary.league;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/5/27
 */
@Data
@Accessors(chain = true)
public class LeagueSeasonScoreData {

    private int leagueId;
    private String leagueType;
    private String leagueName;

}

package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/26
 */
@Data
@Accessors(chain = true)
public class LiveMatchData {

    private int homeTeamId;
    private String homeTeamName;
    private String homeTeamShortName;
    private int homeScore;
    private int awayTeamId;
    private String awayTeamName;
    private String awayTeamShortName;
    private int awayScore;
    private String kickoffTime;

}

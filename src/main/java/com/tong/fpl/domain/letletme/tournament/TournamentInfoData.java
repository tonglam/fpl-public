package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/8/30
 */
@Data
@Accessors(chain = true)
public class TournamentInfoData {

    private int id;
    private String name;
    private String creator;
    private int adminerEntry;
    private String season;
    private String leagueType;
    private int leagueId;

}

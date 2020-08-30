package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;

/**
 * Create by tong on 2020/8/30
 */
@Data
public class TournamentQueryParam {

    private String name;
    private String creator;
    private int leagueId;
    private long current;
    private long size;

}

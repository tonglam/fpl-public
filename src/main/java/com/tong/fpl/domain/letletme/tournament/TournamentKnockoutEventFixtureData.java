package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/10
 */
@Data
@Accessors(chain = true)
public class TournamentKnockoutEventFixtureData {

    private int matchId;
    private int playAgainstId;
    private int homeEntry;
    private int homeEntryPoints;
    private int homeEntryRank;
    private int awayEntry;
    private int awayEntryPoints;
    private int awayEntryRank;
    private String showMessage;

}

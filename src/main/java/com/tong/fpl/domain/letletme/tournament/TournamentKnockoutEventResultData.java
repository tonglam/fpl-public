package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/9
 */
@Data
@Accessors(chain = true)
public class TournamentKnockoutEventResultData {

    private int homeEntry;
    private String homeEntryName;
    private int homeEntryNetPoints;
    private int homeEntryCost;
    private int homeEntryRank;
    private int awayEntry;
    private String awayEntryName;
    private int awayEntryNetPoints;
    private int awayEntryCost;
    private int awayEntryRank;
    private String score;

}

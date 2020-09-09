package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/9
 */
@Data
@Accessors(chain = true)
public class TournamentBattleGroupEventResultData {

    private int tournamentId;
    private int groupId;
    private int event;
    private int homeEntry;
    private String homeEntryName;
    private int homeEntryNetPoints;
    private int homeEntryRank;
    private int awayEntry;
    private String awayEntryName;
    private int awayEntryNetPoints;
    private int awayEntryRank;
    private String score;

}

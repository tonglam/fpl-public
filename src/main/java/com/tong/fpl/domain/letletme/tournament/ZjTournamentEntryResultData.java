package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/8/30
 */
@Data
@Accessors(chain = true)
public class ZjTournamentEntryResultData {

    private int tournamentId;
    private int entry;
    private String entryName;
    private String playerName;
    private int event;
    private int eventPoints;
    private int eventCost;
    private int eventNetPoints;
    private String eventChip;
    private int pointsGroupPoints;
    private int battleGroupPoints;
    private int pkPoints;
    private int tournamentPoints;

}

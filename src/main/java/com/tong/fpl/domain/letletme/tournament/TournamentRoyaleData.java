package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 18/8/2023
 */
@Data
@Accessors(chain = true)
public class TournamentRoyaleData {

    private int id;
    private int tournamentId;
    private int event;
    private int eventEliminatedNum;
    private int nextEventEliminatedNum;
    private String eventEliminatedEntries;
    private String waitingEliminatedEntries;
    private String allEliminatedEntries;
    private String createTime;
    private String updateTime;

}

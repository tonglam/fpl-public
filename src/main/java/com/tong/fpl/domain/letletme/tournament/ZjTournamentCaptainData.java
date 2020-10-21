package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/10/6
 */
@Data
@Accessors(chain = true)
public class ZjTournamentCaptainData {

    private int tournamentId;
    private int groupId;
    private int captainEntry;
    private String phaseTwoDeadline;
    private String pkDeadline;

}

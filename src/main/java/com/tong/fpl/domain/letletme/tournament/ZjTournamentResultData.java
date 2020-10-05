package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/8/30
 */
@Data
@Accessors(chain = true)
public class ZjTournamentResultData {

    private int tournamentId;
    private int event;
    private int groupId;
    private String groupName;
    private int step;
    private int phaseOneTotalPoints;
    private int phaseOneGroupPoints;
    private int phaseTwoTotalPoints;
    private int phaseTwoGroupPoints;
    private int pkTotalPoints;
    private int pkPoints;
    private int tournamentTotalPoints;
    private int tournamentPoints;

}

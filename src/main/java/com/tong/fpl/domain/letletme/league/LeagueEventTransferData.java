package com.tong.fpl.domain.letletme.league;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/11/10
 */
@Data
@Accessors(chain = true)
public class LeagueEventTransferData {

    private int transferInTotalPoints;
    private int transferInPlayedTotalPoints;
    private int transferOutTotalPoints;
    private int transferPoints;
    private int transferPlayedPoints;
    private int transferNetPoints;
    private int transferInTotalValue;
    private int transferOutTotalValue;
    private int transferValue;

}

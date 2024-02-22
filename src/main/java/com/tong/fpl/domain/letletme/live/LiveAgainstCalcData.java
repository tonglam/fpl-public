package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2024/1/30
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class LiveAgainstCalcData extends LiveCalcData {

    private int round;
    private int playAgainstId;
    private int matchId;
    private int nextMatchId;

}

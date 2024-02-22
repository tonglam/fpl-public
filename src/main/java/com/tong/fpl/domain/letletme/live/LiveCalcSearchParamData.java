package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/9/8
 */
@Data
@Accessors(chain = true)
public class LiveCalcSearchParamData {

    private int event;
    private int tournamentId;
    private List<Integer> elementList;
    private boolean lineup = true;

}

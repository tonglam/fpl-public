package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2023/8/15
 */
@Data
@Accessors(chain = true)
public class LiveTournamentCalcData {

    private List<LiveCalcData> liveCalcDataList;
    private String leagueType;
    private int eventEliminatedNum;
    private List<Integer> eventEliminatedList;
    private List<LiveCalcData> waitingEliminatedList;
    private List<LiveCalcData> eliminatedList;

}

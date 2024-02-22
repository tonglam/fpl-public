package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/8/16
 */
@Data
@Accessors(chain = true)
public class SearchLiveTournamentCalcData {

    private List<String> webNameList;
    private int selectNum;
    private String selectByPercent;
    private List<LiveCalcData> liveCalcDataList;

}

package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2024/1/30
 */
@Data
@Accessors(chain = true)
public class LiveKnockoutCalcData {

    private List<LiveKnockoutResultData> liveAgainstDataList;

}

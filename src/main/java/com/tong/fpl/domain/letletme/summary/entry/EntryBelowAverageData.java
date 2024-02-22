package com.tong.fpl.domain.letletme.summary.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/5/26
 */
@Data
@Accessors(chain = true)
public class EntryBelowAverageData {

    private int event;
    private int points;
    private int averagePoints;

}

package com.tong.fpl.domain.letletme.summary.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/5/26
 */
@Data
@Accessors(chain = true)
public class EntrySelectedCaptainData {

    private int element;
    private String webName;
    private int event;
    private int points;
    private long times;
    private int totalPoints;
    private String totalPointsByPercent;

}

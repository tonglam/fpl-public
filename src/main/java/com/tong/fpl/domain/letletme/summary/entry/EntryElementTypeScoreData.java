package com.tong.fpl.domain.letletme.summary.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/6/4
 */
@Data
@Accessors(chain = true)
public class EntryElementTypeScoreData {

    private int entry;
    private String entryName;
    private String playerName;
    private int totalPoints;

}

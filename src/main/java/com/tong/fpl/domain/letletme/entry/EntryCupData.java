package com.tong.fpl.domain.letletme.entry;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/2/24
 */
@Data
@Accessors(chain = true)
public class EntryCupData {

    private int event;
    private int entry;
    private String entryName;
    private String playerName;
    private int eventPoints;
    private int againstEntry;
    private String againstEntryName;
    private String againstPlayerName;
    private int againstEventPoints;
	private String result;

}

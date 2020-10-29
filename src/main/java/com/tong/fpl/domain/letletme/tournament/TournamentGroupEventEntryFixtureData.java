package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/10
 */
@Data
@Accessors(chain = true)
public class TournamentGroupEventEntryFixtureData {

    private int homeEntry;
	private int homeEntryNetPoints;
	private int awayEntry;
	private int awayEntryNetPoints;
	private String showMessage;

}

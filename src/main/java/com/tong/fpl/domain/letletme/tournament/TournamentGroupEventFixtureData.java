package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/10
 */
@Data
@Accessors(chain = true)
public class TournamentGroupEventFixtureData {

	private int homeEntry;
	private int homeEntryPoints;
	private int awayEntry;
	private int awayEntryPoints;
	private String showMessage;

}

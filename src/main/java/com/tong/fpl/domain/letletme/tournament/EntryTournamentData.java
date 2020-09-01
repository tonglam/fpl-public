package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/1
 */
@Data
@Accessors(chain = true)
public class EntryTournamentData {

	private int entry;
	private int tournamentId;
	private String name;
	private String creator;
	private String season;
	private String leagueType;
	private int leagueId;
	private String createTime;

}

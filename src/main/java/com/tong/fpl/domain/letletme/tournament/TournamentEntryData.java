package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/1
 */
@Data
@Accessors(chain = true)
public class TournamentEntryData {

	private int entry;
	private int tournamentId;
	private String name;
	private String creator;
	private String season;
	private String leagueType;
	private int leagueId;
	private String tournamentMode;
	private String groupMode;
	private String knockoutMode;
	private String stage;
	private String createTime;

}

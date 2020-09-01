package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/8/30
 */
@Data
@Accessors(chain = true)
public class TournamentInfoData {

	private int id;
	private String name;
	private int totalTeam;
	private String creator;
	private int adminerEntry;
	private String season;
	private String leagueType;
	private int leagueId;
	private String groupMode;
	private String teamPerGroup;
	private String groupStartGw;
	private String groupEndGw;
	private int groupQualifiers;
	private String groupFillAverage;
	private int groupNum;
	private String knockoutMode;
	private String knockoutStartGw;
	private String knockoutEndGw;
	private int knockoutTeam;
	private int knockoutRounds;
	private String createTime;

}

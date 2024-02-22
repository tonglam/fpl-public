package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/9
 */
@Data
@Accessors(chain = true)
public class TournamentPointsGroupEventResultData {

	private int tournamentId;
	private int groupId;
	private int event;
	private int entry;
	private String entryName;
	private String playerName;
	private int groupRank;
	private int points;
	private int cost;
	private int netPoints;
	private int rank;
	private int benchPoints;
	private String chip;

}

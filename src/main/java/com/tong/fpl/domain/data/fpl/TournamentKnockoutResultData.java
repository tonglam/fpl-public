package com.tong.fpl.domain.data.fpl;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/7/6
 */
@Data
@Accessors(chain = true)
public class TournamentKnockoutResultData {

	private int tournamentId;
	private int event;
	private int playAgainstId;
	private int matchId;
	private int matchWinner;
	private int winnerRank;
	private int roundWinner;
	private int nextMatchId;
	private int nextRoundHomeEntry;
	private int nextRoundAwayEntry;

}

package com.tong.fpl.domain.data.letletme;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/7/6
 */
@Data
@Accessors(chain = true)
public class TournamentKnockoutResultData {

	private int matchId;
	private int event;
	private int round;
	private int playAgainstId;
	private int matchWinner;
	private int winnerRank;
	private int roundWinner;

}

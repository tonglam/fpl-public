package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/3
 */
@Data
@Accessors(chain = true)
public class TournamentKnockoutData {

	private int tournamentId;
	private int round;
	private int matchId;
	private int nextMatchId;
	private int homeEntry;
	private int awayEntry;
	private int roundWinner;

}

package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/3
 */
@Data
@Accessors(chain = true)
public class TournamentKnockoutData {

	private int id;
	private int event;
	private int tournamentId;
	private int round;
	private int matchId;
	private int nextMatchId;
	private int homeEntry;
    private String homeEntryName;
	private String homePlayerName;
    private int homeEntryNetPoints;
    private int homeEntryGoalsScored;
	private int homeEntryGoalsConceded;
    private int awayEntry;
    private int awayEntryNetPoints;
    private String awayEntryName;
    private String awayPlayerName;
    private int awayEntryNeyPoints;
    private int awayEntryGoalsScored;
	private int awayEntryGoalsConceded;
	private double homeEntryWinningNum;
	private double awayEntryWinningNum;
	private int matchWinner;
	private int roundWinner;
	private String roundWinnerName;

}

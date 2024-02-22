package com.tong.fpl.domain.letletme.live;

import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutEventResultData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 30/1/2024
 */
@Data
@Accessors(chain = true)
public class LiveKnockoutResultData {

    private int round;
    private int matchId;
    private int nextMatchId;
    private int playAgainstId;
    private int homeEntry;
    private String homeEntryName;
    private String homePlayerName;
    private int homeEntryWinningNum;
    private int awayEntry;
    private String awayEntryName;
    private String awayPlayerName;
    private int awayEntryWinningNum;
    private String nextOpponents;
    private List<TournamentKnockoutEventResultData> liveAgainstDataList;
    private int roundWinner;

}

package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/7/6
 */
@Data
@Accessors(chain = true)
public class TournamentKnockoutBracketData {

    List<TournamentKnockoutResultData> teams;
    List<List<TournamentKnockoutResultData>> results;

}

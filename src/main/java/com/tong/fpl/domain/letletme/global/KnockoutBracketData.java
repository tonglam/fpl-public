package com.tong.fpl.domain.letletme.global;

import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/7/6
 */
@Data
@Accessors(chain = true)
public class KnockoutBracketData {

    List<TournamentKnockoutResultData> teams;
    List<List<TournamentKnockoutResultData>> results;

}

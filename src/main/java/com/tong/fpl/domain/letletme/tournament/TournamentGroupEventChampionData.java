package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/10/27
 */
@Data
@Accessors(chain = true)
public class TournamentGroupEventChampionData {

    private int tournamentId;
    private String tournamentName;
    private List<TournamentPointsGroupEventResultData> eventChampionResultList;
    private List<TournamentPointsGroupEventResultData> eventRunnerUpResultList;
    private List<TournamentPointsGroupEventResultData> eventSecondRunnerUpResultList;
    private List<TournamentGroupChampionCountData> championCountList;

}

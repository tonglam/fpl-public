package com.tong.fpl.domain.letletme.tournament;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/9/10
 */
@Data
@Accessors(chain = true)
public class TournamentGroupFixtureData {

    private int tournamentId;
    private int event;
    private List<TournamentGroupEventGroupFixtureData> groupEventFixtureList;

}

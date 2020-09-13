package com.tong.fpl.domain.letletme.live;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/7/13
 */
@Data
@Accessors(chain = true)
public class TournamentLiveData {

    private int tournamentId;
    private List<LiveCalaData> liveList;

}

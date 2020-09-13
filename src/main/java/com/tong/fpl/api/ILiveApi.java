package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;

/**
 * Create by tong on 2020/8/3
 */
public interface ILiveApi {

    TableData<LiveCalaData> qryEntryLivePoints(int entry);

    TableData<LiveCalaData> qryTournamentLivePoints(int tournamentId);

}

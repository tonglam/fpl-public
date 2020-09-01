package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.table.TableData;
import com.tong.fpl.domain.letletme.tournament.EntryTournamentData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;

/**
 * Create by tong on 2020/8/28
 */
public interface ITableQueryService {

	TableData<PlayerInfoData> qryPagePlayerDataList(long current, long size);

	TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param);

	TableData<EntryTournamentData> qryEntryTournamenList(TournamentQueryParam param);

}

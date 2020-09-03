package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.table.TableData;
import com.tong.fpl.domain.letletme.tournament.EntryTournamentData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;

/**
 * Create by tong on 2020/8/28
 */
public interface ITableQueryService {

    TableData<PlayerInfoData> qryPagePlayerDataList(long page, long limit);

    TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param);

    TableData<EntryTournamentData> qryEntryTournamentList(int entry);

    TableData<EntryInfoData> qryPageEntryInfoByTournament(String season, int tournamentId, long page, long limit);

    TableData<EntryEventCaptainData> qryEntryCaptainList(String season, int entry, long page, long limit);

    TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId);

}

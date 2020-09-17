package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.*;

/**
 * Create by tong on 2020/8/28
 */
public interface ITableQueryService {

    TableData<PlayerInfoData> qryPagePlayerDataList(long page, long limit);

    TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param);

    TableData<TournamentEntryData> qryEntryTournamentList(int entry);

    TableData<EntryInfoData> qryEntryInfoByTournament(String season, int tournamentId);

    TableData<EntryEventCaptainData> qryEntryCaptainList(String season, int entry);

    TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId);

    TableData<PlayerInfoData> qryPlayerList(String season);

    TableData<TournamentPointsGroupEventResultData> qryPointsGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

    TableData<TournamentBattleGroupEventResultData> qryBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit);

    TableData<LiveCalaData> qryEntryLivePoints(int entry);

    TableData<LiveCalaData> qryTournamentLivePoints(int tournamentId);

    TableData<EntryEventResultData> qryEntryResultList(int entry);

    TableData<EntryPickData> qryEntryEventResult(int event, int entry);

    TableData<ElementEventResultData> qryElementEventResult(int event, int element);

    TableData<TournamentInfoData> qryEntryPointsGroupTournamentList(int entry);

    TableData<TournamentGroupData> qryTournamentResultList(int tournamentId, int event);

}

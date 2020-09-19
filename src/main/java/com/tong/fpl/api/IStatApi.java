package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;

import java.util.List;

/**
 * Create by tong on 2020/9/2
 */
public interface IStatApi {

    TableData<EntryInfoData> qryEntryInfoByTournament(String season, int tournamentId);

    TableData<EntryEventCaptainData> qryEntryCaptainList(String season, int entry);

    TableData<PlayerInfoData> qryPlayerList(String season);

    List<String> qryTeamSelectStatList();

    TableData<LeagueStatData> qryTeamSelectStatByName(String leagueName, int event);

}

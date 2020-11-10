package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;

/**
 * Create by tong on 2020/8/15
 */
public interface IMyFplApi {

    /**
     * @apiNote entry
     */
    EntryInfoData qryEntryInfo(int entry);

    TableData<EntryEventResultData> qryEntryResultList(int entry);

    TableData<EntryPickData> qryEntryEventResult(int event, int entry);

    /**
     * @apiNote pick
     */
    TableData<PlayerInfoData> qryPlayerDataList(long page, long limit);

    /**
     * @apiNote league
     */
    TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param);

    String qryLeagueNameByIdAndType(int leagueId, String leagueType);

    TableData<LeagueEventReportStatData> qryLeagueReportStat(int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryLeagueEventReportList(int leagueId, String leagueType, int event);

}

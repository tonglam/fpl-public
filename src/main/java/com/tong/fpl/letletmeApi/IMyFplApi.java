package com.tong.fpl.letletmeApi;

import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
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
    TableData<PlayerInfoData> qryPlayerDataList(int page, int limit);

    /**
     * @apiNote league
     */
    TableData<TournamentInfoData> qryTournamentList(TournamentQueryParam param);

    String qryLeagueNameByIdAndType(int leagueId, String leagueType);

    /**
     * @apiNote leagueCaptain
     */
    TableData<LeagueEventReportStatData> qryLeagueCaptainReportStat(int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryLeagueCaptainEventReportList(int event, int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryEntryCaptainEventReportList(int leagueId, String leagueType, int entry);

    /**
     * @apiNote leagueTransfers
     */
    TableData<LeagueEventReportStatData> qryLeagueTransfersReportStat(int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryLeagueTransfersEventReportList(int event, int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryEntryTransfersEventReportList(int leagueId, String leagueType, int entry);

    /**
     * @apiNote leagueScoring
     */
    TableData<LeagueEventReportStatData> qryLeagueScoringReportStat(int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryLeagueScoringEventReportList(int event, int leagueId, String leagueType);

    TableData<LeagueEventReportData> qryEntryScoringEventReportList(int leagueId, String leagueType, int entry);

    /**
     * @apiNote common
     */
    int getCurrentEvent();

}

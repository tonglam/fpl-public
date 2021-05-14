package com.tong.fpl.letletmeApi.impl;

import cn.hutool.core.bean.BeanUtil;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;
import com.tong.fpl.letletmeApi.IMyFplApi;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.ITableQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/8/15
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyFplApiImpl implements IMyFplApi {

    private final IQueryService queryService;
    private final ITableQueryService tableQueryService;

    /**
     * @implNote entry
     */
    @Override
    public EntryInfoData qryEntryInfo(int entry) {
        return BeanUtil.copyProperties(this.queryService.qryEntryInfo(entry), EntryInfoData.class);
    }

    @Override
    public TableData<EntryEventResultData> qryEntryResultList(int entry) {
        return this.tableQueryService.qryEntryResultList(entry);
    }

    @Override
    public TableData<EntryPickData> qryEntryEventResult(int event, int entry) {
        return this.tableQueryService.qryEntryEventResult(event, entry);
    }

    /**
     * @implNote pick
     */
    @Override
    public TableData<PlayerInfoData> qryPlayerDataList(int page, int limit) {
        return this.tableQueryService.qryPagePlayerDataList(page, limit);
    }

    /**
     * @implNote league
     */
    @Override
    public TableData<TournamentInfoData> qryTournamentList(TournamentQueryParam param) {
        return this.tableQueryService.qryTournamentList(param);
    }

    @Override
    public String qryLeagueNameByIdAndType(int leagueId, String leagueType) {
        return this.queryService.qryLeagueNameByIdAndType(leagueId, leagueType);
    }

    /**
     * @implNote leagueCaptain
     */
    @Override
    public TableData<LeagueEventReportStatData> qryLeagueCaptainReportStat(int leagueId, String leagueType) {
        return this.tableQueryService.qryLeagueCaptainReportStat(leagueId, leagueType);
    }

    @Override
    public TableData<LeagueEventReportData> qryLeagueCaptainEventReportList(int event, int leagueId, String leagueType) {
        return this.tableQueryService.qryLeagueCaptainEventReportList(event, leagueId, leagueType);
    }

    @Override
    public TableData<LeagueEventReportData> qryEntryCaptainEventReportList(int leagueId, String leagueType, int entry) {
        return this.tableQueryService.qryEntryCaptainEventReportList(leagueId, leagueType, entry);
    }

    /**
     * @implNote leagueTransfers
     */
    @Override
    public TableData<LeagueEventReportStatData> qryLeagueTransfersReportStat(int leagueId, String leagueType) {
        return this.tableQueryService.qryLeagueTransfersReportStat(leagueId, leagueType);
    }

    @Override
    public TableData<LeagueEventReportData> qryLeagueTransfersEventReportList(int event, int leagueId, String leagueType) {
        return this.tableQueryService.qryLeagueTransfersEventReportList(event, leagueId, leagueType);
    }

    @Override
    public TableData<LeagueEventReportData> qryEntryTransfersEventReportList(int leagueId, String leagueType, int entry) {
        return this.tableQueryService.qryEntryTransfersEventReportList(leagueId, leagueType, entry);
    }

    /**
     * @implNote leagueScoring
     */
    @Override
    public TableData<LeagueEventReportStatData> qryLeagueScoringReportStat(int leagueId, String leagueType) {
        return this.tableQueryService.qryLeagueScoringReportStat(leagueId, leagueType);
    }

    @Override
    public TableData<LeagueEventReportData> qryLeagueScoringEventReportList(int event, int leagueId, String leagueType) {
        return this.tableQueryService.qryLeagueScoringEventReportList(event, leagueId, leagueType);
    }

    @Override
    public TableData<LeagueEventReportData> qryEntryScoringEventReportList(int leagueId, String leagueType, int entry) {
        return this.tableQueryService.qryEntryScoringEventReportList(leagueId, leagueType, entry);
    }

    /**
     * @implNote common
     */
    @Override
    public int getCurrentEvent() {
        return this.queryService.getCurrentEvent();
    }

}

package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiSummary;
import com.tong.fpl.domain.letletme.summary.entry.*;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonCaptainData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonEntryData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonScoreData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonSummaryData;
import com.tong.fpl.service.ISummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/5/25
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiSummaryImpl implements IApiSummary {

    private final ISummaryService summaryService;

    /**
     * @implNote entry
     */
    @Override
    public EntrySeasonInfoData qryEntrySeasonInfo(int entry) {
        return this.summaryService.qryEntrySeasonInfo(entry);
    }

    @Override
    public EntrySeasonSummaryData qryEntrySeasonSummary(int entry) {
        return this.summaryService.qryEntrySeasonSummary(entry);
    }

    @Override
    public EntrySeasonCaptainData qryEntrySeasonCaptain(int entry) {
        return this.summaryService.qryEntrySeasonCaptain(entry);
    }

    @Override
    public EntrySeasonTransfersData qryEntrySeasonTransfers(int entry) {
        return this.summaryService.qryEntrySeasonTransfers(entry);
    }

    @Override
    public EntrySeasonScoreData qryEntrySeasonScore(int entry) {
        return this.summaryService.qryEntrySeasonScore(entry);
    }

    /**
     * @implNote league
     */
    @Override
    public LeagueSeasonSummaryData qryLeagueSeasonSummary(int leagueId, String leagueType) {
        return this.summaryService.qryLeagueSeasonSummary(leagueId, leagueType);
    }

    @Override
    public LeagueSeasonCaptainData qryLeagueSeasonCaptain(int leagueId, String leagueType) {
        return this.summaryService.qryLeagueSeasonCaptain(leagueId, leagueType);
    }

    @Override
    public LeagueSeasonScoreData qryLeagueSeasonScore(int leagueId, String leagueType) {
        return this.summaryService.qryLeagueSeasonScore(leagueId, leagueType);
    }

    @Override
    public LeagueSeasonEntryData qryLeagueSeasonEntry(int leagueId, String leagueType, int entry) {
        return this.summaryService.qryLeagueSeasonEntry(leagueId, leagueType, entry);
    }

}

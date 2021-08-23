package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiSummary;
import com.tong.fpl.domain.letletme.summary.entry.*;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonCaptainData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonInfoData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonScoreData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonSummaryData;
import com.tong.fpl.service.IEventDataService;
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
    private final IEventDataService eventDataService;

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

    @Override
    public void refreshEntryEventSummary(int event, int entry) {
        this.eventDataService.refreshEntryEventSummary(event, entry);
    }

    /**
     * @implNote league
     */
    @Override
    public LeagueSeasonInfoData qryLeagueSeasonInfo(String leagueName) {
        return this.summaryService.qryLeagueSeasonInfo(leagueName);
    }

    @Override
    public LeagueSeasonSummaryData qryLeagueSeasonSummary(String leagueName, int entry) {
        return this.summaryService.qryLeagueSeasonSummary(leagueName, entry);
    }

    @Override
    public LeagueSeasonCaptainData qryLeagueSeasonCaptain(String leagueName, int entry) {
        return this.summaryService.qryLeagueSeasonCaptain(leagueName, entry);
    }

    @Override
    public LeagueSeasonScoreData qryLeagueSeasonScore(String leagueName, int entry) {
        return this.summaryService.qryLeagueSeasonScore(leagueName, entry);
    }

}

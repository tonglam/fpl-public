package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiSummary;
import com.tong.fpl.domain.event.RefreshLeagueSummaryEventData;
import com.tong.fpl.domain.letletme.element.ElementEventData;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.global.MapData;
import com.tong.fpl.domain.letletme.scout.PopularScoutData;
import com.tong.fpl.domain.letletme.summary.entry.*;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonCaptainData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonInfoData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonScoreData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonSummaryData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IDataService;
import com.tong.fpl.service.IRefreshService;
import com.tong.fpl.service.ISummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/25
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiSummaryImpl implements IApiSummary {

    private final ApplicationContext context;
    private final IApiQueryService apiQueryService;
    private final ISummaryService summaryService;
    private final IDataService dataService;
    private final IRefreshService refreshService;

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
    public void refreshEntrySummary(int event, int entry) {
        this.refreshService.refreshEntrySummary(event, entry);
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

    @Override
    public void refreshLeagueSummary(int event, String leagueName, int entry) {
        this.context.publishEvent(new RefreshLeagueSummaryEventData(this, event, leagueName, entry));
    }

    /**
     * @implNote overall
     */
    @Override
    public EventOverallResultData qryEventOverallResult(int event) {
        return this.apiQueryService.qryEventOverallResult(event);
    }

    @Override
    public List<ElementEventData> qryEventDreamTeam(int event) {
        return this.apiQueryService.qryEventDreamTeam(event);
    }

    @Override
    public List<ElementEventData> qryEventEliteElements(int event) {
        return this.apiQueryService.qryEventEliteElements(event);
    }

    @Override
    public Map<String, List<ElementEventData>> qryEventOverallTransfers(int event) {
        return this.apiQueryService.qryEventOverallTransfers(event);
    }

    @Override
    public void refreshEventOverallSummary(int event) {
        this.refreshService.refreshEventOverallSummary(event);
    }

    /**
     * @implNote scout
     */
    @Override
    public void insertEventSourceScout(int event, String source, List<MapData<Integer>> scoutDataList) {
        this.dataService.insertEventSourceScout(event, source, scoutDataList);
    }

    @Override
    public PopularScoutData qryEventSourceScoutResult(int event, String source) {
        return this.apiQueryService.qryEventSourceScoutResult(event, source);
    }

    @Override
    public List<PopularScoutData> qryOverallEventScoutResult(int event) {
        return this.apiQueryService.qryOverallEventScoutResult(event);
    }

    @Override
    public void refreshEventSourceScoutResult(int event) {
        this.refreshService.refreshEventSourceScoutResult(event);
    }

}

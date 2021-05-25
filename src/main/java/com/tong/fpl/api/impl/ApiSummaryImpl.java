package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiSummary;
import com.tong.fpl.domain.letletme.summary.*;
import com.tong.fpl.service.IApiQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/5/25
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiSummaryImpl implements IApiSummary {

    private final IApiQueryService apiQueryService;

    @Override
    public EntrySeasonInfoData qryEntrySeasonInfo(int entry) {
        return this.apiQueryService.qryEntrySeasonInfo(entry);
    }

    @Override
    public EntrySeasonSummaryData qrySeasonEntrySummary(int entry) {
        return this.apiQueryService.qrySeasonEntrySummary(entry);
    }

    @Override
    public EntrySeasonCaptainData qryEntrySeasonCaptain(int entry) {
        return this.apiQueryService.qryEntrySeasonCaptain(entry);
    }

    @Override
    public EntrySeasonTransfersData qryEntrySeasonTransfers(int entry) {
        return this.apiQueryService.qryEntrySeasonTransfers(entry);
    }

    @Override
    public EntrySeasonScoreData qryEntrySeasonScore(int entry) {
        return this.apiQueryService.qryEntrySeasonScore(entry);
    }
}

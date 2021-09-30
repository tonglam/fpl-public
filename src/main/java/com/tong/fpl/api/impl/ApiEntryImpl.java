package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiEntry;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IRefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiEntryImpl implements IApiEntry {

    private final IApiQueryService apiQueryService;
    private final IRefreshService refreshService;

    @Override
    public EntryInfoData qryEntryInfo(int entry) {
        return this.apiQueryService.qryEntryInfo(entry);
    }

    @Override
    public List<EntryInfoData> fuzzyQueryEntry(EntryQueryParam param) {
        return this.apiQueryService.fuzzyQueryEntry(param);
    }

    @Override
    public void refreshEntryInfo(int entry) {
        this.refreshService.refreshEntryInfo(entry);
    }

    @Override
    public EntryLeagueData qryEntryLeagueInfo(int entry) {
        return this.apiQueryService.qryEntryLeagueInfo(entry);
    }

    @Override
    public EntryHistoryData qryEntryHistoryInfo(int entry) {
        return this.apiQueryService.qryEntryHistoryInfo(entry);
    }

    @Override
    public EntryEventResultData qryEntryEventResult(int event, int entry) {
        return this.apiQueryService.qryEntryEventResult(event, entry);
    }

    @Override
    public void refreshEntryEventResult(int event, int entry) {
        this.refreshService.refreshEntryEventResult(event, entry);
    }

    @Override
    public List<EntryEventTransfersData> qryEntryEventTransfers(int event, int entry) {
        return this.apiQueryService.qryEntryEventTransfers(event, entry);
    }

    @Override
    public List<EntryEventTransfersData> qryEntryAllTransfers(int entry) {
        return this.apiQueryService.qryEntryAllTransfers(entry);
    }

    @Override
    public void refreshEntryEventTransfers(int event, int entry) {
        this.refreshService.refreshEntryEventTransfers(event, entry);
    }

}

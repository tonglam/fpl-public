package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiEntry;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IEventDataService;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
    private final IEventDataService eventDataService;

    @Override
    public List<EntryInfoData> fuzzyQueryEntry(EntryQueryParam param) {
        return this.apiQueryService.fuzzyQueryEntry(param);
    }

    @Override
    public EntryInfoData qryEntryInfo(int entry) {
        return this.apiQueryService.qryEntryInfo(entry);
    }

    @Override
    public EntryLeagueInfoData qryEntryLeagueInfo(int entry) {
        return this.apiQueryService.qryEntryLeagueInfo(entry);
    }

    @Override
    public EntryHistoryInfoData qryEntryHistoryInfo(int entry) {
        return this.apiQueryService.qryEntryHistoryInfo(entry);
    }

    @Override
    public EntryEventResultData qryEntryEventResult(int event, int entry) {
        return this.apiQueryService.qryEntryEventResult(event, entry);
    }

    @Override
    public void refreshEntryEventResult(int event, int entry) {
        this.eventDataService.upsertEntryEventResult(event, entry);
        String key = StringUtils.joinWith("::", "api::qryEntryEventResult", event, entry);
        RedisUtils.removeCacheByKey(key);
    }

    @Override
    public List<EntryEventTransfersData> qryEntryEventTransfers(int event, int entry) {
        return this.apiQueryService.qryEntryEventTransfers(event, entry);
    }

    @Override
    public void refreshEntryEventTransfers(int event, int entry) {
        this.eventDataService.updateEntryEventTransfers(event, entry);
        String key = StringUtils.joinWith("::", "api::qryEntryEventTransfers", event, entry);
        RedisUtils.removeCacheByKey(key);
    }

}

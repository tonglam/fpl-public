package com.tong.fpl.api.impl;

import com.tong.fpl.api.ICommonApi;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/2/26
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommonApiImpl implements ICommonApi {

    private final IQueryService queryService;

    @Override
    public int getCurrentEvent() {
        return this.queryService.getCurrentEvent();
    }

    @Override
    public int getNextEvent() {
        return this.queryService.getNextEvent();
    }

    @Override
    public String getUtcDeadlineByEvent(int event) {
        return this.queryService.getUtcDeadlineByEvent(event);
    }

    @Override
    public EntryInfoData qryEntryInfoData(int entry) {
        return this.queryService.qryEntryInfoData(entry);
    }

    @Override
    public LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType) {
        return this.queryService.qryPlayerInfoByElementType(elementType);
    }

    @Override
    public PlayerDetailData qryPlayerDetailData(int element) {
        return this.queryService.qryPlayerDetailData(element);
    }

    @Override
    public Map<Object, Object> getScoutMap() {
        return this.queryService.getScoutMap();
    }

}

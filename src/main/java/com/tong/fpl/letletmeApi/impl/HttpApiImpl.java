package com.tong.fpl.letletmeApi.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.common.collect.Lists;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.letletme.entry.EntryEventData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerQueryParam;
import com.tong.fpl.letletmeApi.IHttpApi;
import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/7/20
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HttpApiImpl implements IHttpApi {

    private final IQueryService queryService;

    @Override
    public EntryEventData qryEntryResult(String season, int entry) {
        EntryEventData entryEventData = new EntryEventData();
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(season, entry);
        if (entryInfoData == null) {
            return entryEventData;
        }
        BeanUtil.copyProperties(entryInfoData, entryEventData, CopyOptions.create().ignoreNullValue());
        entryEventData.setEventResultList(this.queryService.qryEntryResult(season, entry));
        return entryEventData;
    }

    @Override
    public EntryEventData qryEntryEventResult(String season, int event, int entry) {
        EntryEventData entryEventData = new EntryEventData();
        EntryEventResultData entryEventResultData = this.queryService.qryEntryEventResult(season, event, entry);
        if (entryEventResultData != null) {
            entryEventData.setEventResultList(Lists.newArrayList(entryEventResultData));
        }
        return entryEventData;
    }

    @Override
    public List<EventLiveEntity> qryEventLiveAll(String season, int element) {
        List<EventLiveEntity> list = Lists.newArrayList();
        int current = this.queryService.getCurrentEvent();
        IntStream.rangeClosed(1, current).forEach(event -> {
            list.add(this.queryService.qryEventLiveByElement(event, element));
        });
        return list;
    }

    @Override
    public EventLiveEntity qryEventLive(String season, int event, int element) {
        return this.queryService.qryEventLiveByElement(season, event, element);
    }

    @Override
    public PlayerData qryPlayerData(PlayerQueryParam queryParam) throws Exception {
        int element = this.getElementByQueryParam(queryParam);
        if (element == 0) {
            return new PlayerData();
        }
        return this.queryService.qryPlayerData(element);
    }

    private int getElementByQueryParam(PlayerQueryParam queryParam) throws Exception {
        if (queryParam.getElement() > 0) {
            return queryParam.getElement();
        }
        if (queryParam.getCode() > 0) {
            return this.queryService.qryPlayerElementByCode(queryParam.getCode());
        }
        if (StringUtils.isNoneBlank(queryParam.getWebName())) {
            return this.queryService.qryPlayerElementByWebName(queryParam.getWebName());
        }
        return 0;
    }

    @Override
    public List<PlayerInfoData> qryAllPlayers(String season) {
        return this.queryService.qryAllPlayers(season);
    }

    @Override
    public String getUtcDeadlineByEvent(int event) {
        return this.queryService.getUtcDeadlineByEvent(event);
    }

    @Override
    public int getCurrentEvent() {
        return this.queryService.getCurrentEvent();
    }

    @Override
    public int getNextEvent() {
        return this.queryService.getNextEvent();
    }

}

package com.tong.fpl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.PlayerData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.PlayerEntity;

import java.util.List;

/**
 * Create by tong on 2020/7/31
 */
public interface IQuerySerivce {

    List<PlayerValueData> qryDayChangePlayerValue(String changeDate);

    EntryEventData qryEntryResult(String season, int entry);

    EntryEventData qryEntryEventResult(String season, int event, int entry);

    List<EventLiveEntity> qryEventLiveAll(String season, int element);

    List<EventLiveEntity> qryEventLive(String season, int event, int element);

    Page<PlayerData> qryPlayerDataList(long current, long size);

    PlayerEntity qryPlayerInfo(String season, int element);

}

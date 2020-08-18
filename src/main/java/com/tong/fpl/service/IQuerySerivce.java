package com.tong.fpl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.PlayerData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;
import com.tong.fpl.domain.entity.EventLiveEntity;

import java.util.List;

/**
 * Create by tong on 2020/7/31
 */
public interface IQuerySerivce {

    List<PlayerValueData> qryDayChangePlayerValue(String changeDate);

    EntryEventData qryEntryResult(int entry);

    EntryEventData qryEntryEventResult(int event, int entry);

    List<EventLiveEntity> qryEventLiveAll(int element);

    List<EventLiveEntity> qryEventLive(int event, int element);

	Page<PlayerData> qryPlayerDataList(long current, long size);

}

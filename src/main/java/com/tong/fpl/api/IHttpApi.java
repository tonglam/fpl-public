package com.tong.fpl.api;

import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.PlayerEntity;

import java.util.List;

/**
 * Create by tong on 2020/7/20
 */
public interface IHttpApi {

    List<PlayerValueData> qryDayChangePlayerValue(String changeDate);

    void insertPlayerValue();

    EntryEventData qryEntryResult(String season, int entry);

    EntryEventData qryEntryEventResult(String season, int event, int entry);

    List<EventLiveEntity> qryEventLiveAll(String season, int element);

    List<EventLiveEntity> qryEventLive(String season, int event, int element);

    PlayerEntity qryPlayerInfo(String season, int element);

}

package com.tong.fpl.api;

import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;
import com.tong.fpl.domain.entity.EventLiveEntity;

import java.util.List;

/**
 * Create by tong on 2020/7/20
 */
public interface IHttpApi {

	List<PlayerValueData> qryDayChangePlayerValue(String changeDate);

	void insertPlayerValue();

	EntryEventData qryEntryResult(int entry);

	EntryEventData qryEntryEventResult(int event, int entry);

	List<EventLiveEntity> qryEventLiveAll(int element);

	List<EventLiveEntity> qryEventLive(int event, int element);

}

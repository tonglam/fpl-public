package com.tong.fpl.api.impl;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IStaticSerive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2020/7/20
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HttpApiImpl implements IHttpApi {

	private final IQuerySerivce querySerivce;
	private final IStaticSerive staticSerive;

	@Override
	public List<PlayerValueData> qryDayChangePlayerValue(String changeDate) {
		return this.querySerivce.qryDayChangePlayerValue(changeDate);
	}

	@Override
	public void insertPlayerValue() {
		this.staticSerive.insertPlayerValue();
	}

	@Override
	public EntryEventData qryEntryResult(int entry) {
		return this.querySerivce.qryEntryResult(entry);
	}

	@Override
	public EntryEventData qryEntryEventResult(int event, int entry) {
		return this.querySerivce.qryEntryEventResult(event, entry);
	}

	@Override
	public List<EventLiveEntity> qryEventLive(int element) {
		return this.querySerivce.qryEventLive(element);
	}

}

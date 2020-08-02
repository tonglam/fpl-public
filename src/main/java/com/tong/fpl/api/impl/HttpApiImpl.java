package com.tong.fpl.api.impl;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;
import com.tong.fpl.service.IQuerySerivce;
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

	private final IQuerySerivce qryDayChangePlayerValue;

	@Override
	public List<PlayerValueData> qryDayChangePlayerValue(String changeDate) {
		return this.qryDayChangePlayerValue.qryDayChangePlayerValue(changeDate);
	}

	@Override
	public EntryEventData qryEntryEvent(int event, int entry) {
		return this.qryDayChangePlayerValue.qryEntryEvent(event, entry);
	}

}

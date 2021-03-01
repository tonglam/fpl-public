package com.tong.fpl.api.impl;

import com.tong.fpl.api.ICommonApi;
import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2021/2/26
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommonApiImpl implements ICommonApi {

	private final IQueryService queryService;

	@Override
	public int getNextEvent() {
		return this.queryService.getNextEvent();
	}

	@Override
	public String getUtcDeadlineByEvent(int event) {
		return this.queryService.getUtcDeadlineByEvent(event);
	}

}

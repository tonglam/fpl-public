package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiScout;
import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiScoutImpl implements IApiScout {

	private final IQueryService queryService;

	@Override
	public Map<Object, Object> getScoutMap() {
		return this.queryService.getScoutMap();
	}

}

package com.tong.fpl.api.impl;

import com.tong.fpl.api.IScoutApi;
import com.tong.fpl.service.IApiQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScoutApiImpl implements IScoutApi {

	private final IApiQueryService apiQueryService;

	@Override
	public Map<String, String> qryScoutEntry() {
		return this.apiQueryService.qryScoutEntry();
	}

}

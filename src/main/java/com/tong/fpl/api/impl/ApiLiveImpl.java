package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiLive;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiLiveImpl implements IApiLive {

	private final IQueryService queryService;

	@Override
	public Map<String, List<PlayerFixtureData>> qryTeamLiveFixture(String shortName) {
		return this.queryService.qryTeamFixture(shortName);
	}

}

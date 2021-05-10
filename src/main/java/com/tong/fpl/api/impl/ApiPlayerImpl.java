package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiPlayer;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiPlayerImpl implements IApiPlayer {

	private final IQueryService queryService;

	@Override
	public LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType) {
		return this.queryService.qryPlayerInfoByElementType(elementType);
	}

	@Override
	public PlayerDetailData qryPlayerDetailData(int element) {
		return this.queryService.qryPlayerDetailData(element);
	}

	@Override
	public Map<String, List<PlayerFixtureData>> qryTeamFixture(String shortName) {
		return this.queryService.qryTeamFixture(shortName);
	}

}

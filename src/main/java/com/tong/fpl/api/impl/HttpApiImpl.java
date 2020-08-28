package com.tong.fpl.api.impl;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.domain.data.letletme.api.EntryEventData;
import com.tong.fpl.domain.data.letletme.player.PlayerData;
import com.tong.fpl.domain.data.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.data.letletme.player.PlayerQueryParam;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IRedisCacheSerive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
	private final IRedisCacheSerive redisCacheSerive;

	@Override
	public EntryEventData qryEntryResult(String season, int entry) {
		return this.querySerivce.qryEntryResult(season, entry);
	}

	@Override
	public EntryEventData qryEntryEventResult(String season, int event, int entry) {
		return this.querySerivce.qryEntryEventResult(season, event, entry);
	}

	@Override
	public List<EventLiveEntity> qryEventLiveAll(String season, int element) {
		return this.querySerivce.qryEventLiveAll(season, element);
	}

	@Override
	public List<EventLiveEntity> qryEventLive(String season, int event, int element) {
		return this.querySerivce.qryEventLive(season, event, element);
	}

	@Override
	public PlayerData qryPlayerData(PlayerQueryParam queryParam) throws Exception {
		int element = this.getElementByQueryParam(queryParam);
		if (element == 0) {
			return new PlayerData();
		}
		return this.querySerivce.qryPlayerData(element);
	}

	private int getElementByQueryParam(PlayerQueryParam queryParam) throws Exception {
		if (queryParam.getElement() > 0) {
			return queryParam.getElement();
		}
		if (queryParam.getCode() > 0) {
			return this.querySerivce.qryPlayerElementByCode(queryParam.getCode());
		}
		if (StringUtils.isNoneBlank(queryParam.getWebName())) {
			return this.querySerivce.qryPlayerElementByWebName(queryParam.getWebName());
		}
		return 0;
	}

	@Override
	public List<PlayerInfoData> qryAllPlayers(String season) {
		return this.querySerivce.qryAllPlayers(season);
	}

	@Override
	public String qryDeadlineByEvent(int event) {
		return this.redisCacheSerive.getDeadlineByEvent(event);
	}

	@Override
	public int getCurrentEvent() {
		return this.redisCacheSerive.getCurrentEvent();
	}

}

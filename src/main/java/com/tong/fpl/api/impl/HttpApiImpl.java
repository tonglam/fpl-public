package com.tong.fpl.api.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.google.common.collect.Lists;
import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.letletme.entry.EntryEventData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerQueryParam;
import com.tong.fpl.service.IQuerySerivce;
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

	@Override
	public EntryEventData qryEntryResult(String season, int entry) {
		EntryEventData entryEventData = new EntryEventData();
		EntryInfoEntity entryInfoEntity = this.querySerivce.qryEntryInfo(season, entry);
		if (entryInfoEntity == null) {
			return entryEventData;
		}
		BeanUtil.copyProperties(entryInfoEntity, entryEventData, CopyOptions.create().ignoreNullValue());
		entryEventData.setEventResultList(this.querySerivce.qryEntryResult(season, entry));
		return entryEventData;
	}

	@Override
	public EntryEventData qryEntryEventResult(String season, int event, int entry) {
		EntryEventData entryEventData = new EntryEventData();
		EntryInfoEntity entryInfoEntity = this.querySerivce.qryEntryInfo(season, entry);
		if (entryInfoEntity == null) {
			return entryEventData;
		}
		EntryEventResultData entryEventResultData = this.querySerivce.qryEntryEventResult(season, event, entry);
		if (entryEventResultData != null) {
			entryEventData.setEventResultList(Lists.newArrayList(entryEventResultData));
		}
		return entryEventData;
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
		return this.querySerivce.getDeadlineByEvent(event);
	}

	@Override
	public int getCurrentEvent() {
		return this.querySerivce.getCurrentEvent();
	}

	@Override
	public int getNextEvent() {
		return this.querySerivce.getNextEvent();
	}

}

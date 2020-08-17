package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.EntryEventResultData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.PlayerValueEntity;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2020/7/31
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueryServiceImpl implements IQuerySerivce {

	private final PlayerService playerService;
	private final PlayerValueService playerValueService;
	private final EventLiveService eventLiveService;
	private final EntryInfoService entryInfoService;
	private final EntryEventResultService entryEventResultService;

	@Override
	public List<PlayerValueData> qryDayChangePlayerValue(String changeDate) {
		List<PlayerValueData> playerValueDataList = Lists.newArrayList();
		this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
				.eq(PlayerValueEntity::getChangeDate, changeDate))
				.forEach(o -> {
					PlayerValueData playerValueData = new PlayerValueData();
					BeanUtil.copyProperties(o, playerValueData);
					playerValueData.setWebName(this.playerService.getById(o.getElement()).getWebName());
					playerValueData.setElementTypeName(Position.getNameFromElementType(o.getElementType()).name());
					playerValueDataList.add(playerValueData);
				});
		return playerValueDataList;
	}

	@Override
	public EntryEventData qryEntryResult(int entry) {
		return this.qryEntryEventResultData(entry);
	}

	@Override
	public EntryEventData qryEntryEventResult(int event, int entry) {
		return this.qryEntryEventResultData(event, entry);
	}

	@Override
	public List<EventLiveEntity> qryEventLiveAll(int element) {
		return this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getElement, element));
	}

	@Override
	public List<EventLiveEntity> qryEventLive(int event, int element) {
		return this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
				.eq(EventLiveEntity::getEvent, event).eq(EventLiveEntity::getElement, element));
	}

	private EntryEventData qryEntryEventResultData(int entry) {
		return this.qryEntryEventResultData(0, entry);
	}

	private EntryEventData qryEntryEventResultData(int event, int entry) {
		EntryEventData entryEventData = new EntryEventData();
		// entry_info
		EntryInfoEntity entryInfoEntity = this.entryInfoService.getOne(new QueryWrapper<EntryInfoEntity>().lambda().
				eq(EntryInfoEntity::getEntry, entry));
		if (entryInfoEntity == null) {
			return entryEventData;
		}
		BeanUtil.copyProperties(entryInfoEntity, entryEventData);
		// entry_event_result
		entryEventData.setEventResultDatas(this.setEntryEventResult(event, entry));
		return entryEventData;
	}

	private List<EntryEventResultData> setEntryEventResult(int event, int entry) {
		List<EntryEventResultEntity> entryEventResultList;
		if (event == 0) {
			entryEventResultList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
					.eq(EntryEventResultEntity::getEntry, entry));
		} else {
			entryEventResultList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
					.eq(EntryEventResultEntity::getEvent, event).eq(EntryEventResultEntity::getEntry, entry));
		}
		List<EntryEventResultData> entryEventResultDataList = Lists.newArrayList();
		entryEventResultList.forEach(entryEventResultEntity -> {
			EntryEventResultData entryEventResultData = new EntryEventResultData();
			BeanUtil.copyProperties(entryEventResultEntity, entryEventResultData);
			entryEventResultData.setEventPicks(CommonUtils.getPickListFromPicks(entryEventResultEntity.getEventPicks()));
			entryEventResultDataList.add(entryEventResultData);
		});
		return entryEventResultDataList;
	}

}

package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.entity.PlayerValueEntity;
import com.tong.fpl.domain.web.PlayerValueData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.db.EntryEventResultService;
import com.tong.fpl.service.db.PlayerValueService;
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

	private final PlayerValueService playerValueService;
	private final EntryEventResultService entryEventResultService;

	@Override
	public List<PlayerValueData> qryDayChangePlayerValue(String changeDate) {
		List<PlayerValueData> playerValueDataList = Lists.newArrayList();
		this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
				.eq(PlayerValueEntity::getChangeDate, changeDate))
				.forEach(o -> {
					PlayerValueData playerValueData = new PlayerValueData();
					BeanUtil.copyProperties(o, playerValueData);
					playerValueData.setWebName("");
					playerValueData.setElementTypeName("");
				});
		return playerValueDataList;
	}

	@Override
	public EntryEventResultEntity qryEntryEvent(int event, int entry) {
		return this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
				.eq(EntryEventResultEntity::getEvent, entry).eq(EntryEventResultEntity::getEntry, entry));
	}


}

package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.ScoutEntity;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.service.IScoutService;
import com.tong.fpl.service.db.EventLiveService;
import com.tong.fpl.service.db.PlayerService;
import com.tong.fpl.service.db.ScoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/12/9
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScoutServiceImpl implements IScoutService {

	private final PlayerService playerService;
	private final EventLiveService eventLiveService;
	private final ScoutService scoutService;

	@Override
	public void upsertEventScout(ScoutData scoutData) {
		Map<Integer, Integer> elementTeamMap = this.playerService.list()
				.stream()
				.collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getTeamId));
		// upsert
		ScoutEntity scoutEntity = this.scoutService.getOne(new QueryWrapper<ScoutEntity>().lambda()
				.eq(ScoutEntity::getEvent, scoutData.getEvent())
				.eq(ScoutEntity::getEntry, scoutData.getEntry()));
		if (scoutEntity == null) {
			scoutEntity = new ScoutEntity()
					.setEvent(scoutData.getEvent())
					.setEntry(scoutData.getEntry())
					.setScoutName(scoutData.getScoutName())
					.setGkp(scoutData.getGkp())
					.setGkpTeamId(elementTeamMap.getOrDefault(scoutData.getGkp(), 0))
					.setGkpPoints(0)
					.setDef(scoutData.getDef())
					.setDefTeamId(elementTeamMap.getOrDefault(scoutData.getDef(), 0))
					.setDefPoints(0)
					.setMid(scoutData.getMid())
					.setMidTeamId(elementTeamMap.getOrDefault(scoutData.getMid(), 0))
					.setMidPoints(0)
					.setFwd(scoutData.getFwd())
					.setFwdTeamId(elementTeamMap.getOrDefault(scoutData.getFwd(), 0))
					.setFwdPoints(0)
					.setCaptain(scoutData.getCaptain())
					.setCaptainTeamId(elementTeamMap.getOrDefault(scoutData.getCaptain(), 0))
					.setCaptainPoints(0)
					.setReason(StringUtils.isBlank(scoutData.getReason()) ? "" : scoutData.getReason())
					.setEventPoints(0)
					.setTotalPoints(0);
			this.scoutService.save(scoutEntity);
		} else {
			scoutEntity
					.setGkp(scoutData.getGkp())
					.setGkpTeamId(elementTeamMap.getOrDefault(scoutData.getGkp(), 0))
					.setDef(scoutData.getDef())
					.setGkpTeamId(elementTeamMap.getOrDefault(scoutData.getDef(), 0))
					.setMid(scoutData.getMid())
					.setGkpTeamId(elementTeamMap.getOrDefault(scoutData.getMid(), 0))
					.setFwd(scoutData.getFwd())
					.setGkpTeamId(elementTeamMap.getOrDefault(scoutData.getFwd(), 0))
					.setCaptain(scoutData.getCaptain())
					.setReason(StringUtils.isBlank(scoutData.getReason()) ? "" : scoutData.getReason());
			this.scoutService.updateById(scoutEntity);
		}
	}

	@Override
	public void updateEventScoutResult(int event) {
		List<ScoutEntity> list = Lists.newArrayList();
		Map<Integer, Integer> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
				.eq(EventLiveEntity::getEvent, event))
				.stream()
				.collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
		Map<Integer, Integer> lastEventPointsMap = this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
				.eq(ScoutEntity::getEvent, event - 1))
				.stream()
				.collect(Collectors.toMap(ScoutEntity::getEntry, ScoutEntity::getTotalPoints));
		this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
				.eq(ScoutEntity::getEvent, event))
				.forEach(o -> {
					o
							.setGkpPoints(eventLiveMap.getOrDefault(o.getGkp(), 0))
							.setDefPoints(eventLiveMap.getOrDefault(o.getDef(), 0))
							.setMidPoints(eventLiveMap.getOrDefault(o.getMid(), 0))
							.setFwdPoints(eventLiveMap.getOrDefault(o.getFwd(), 0))
							.setCaptainPoints(eventLiveMap.getOrDefault(o.getCaptain(), 0));
					int eventPoints = o.getGkpPoints() + o.getDefPoints() + o.getMidPoints() + o.getFwdPoints() + o.getCaptainPoints();
					o.setEventPoints(eventPoints).setTotalPoints(eventPoints + lastEventPointsMap.getOrDefault(o.getEntry(), 0));
					list.add(o);
				});
		this.scoutService.updateBatchById(list);
	}

}

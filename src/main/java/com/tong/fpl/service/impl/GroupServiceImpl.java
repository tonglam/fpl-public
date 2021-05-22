package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.ScoutEntity;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.service.IGroupService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.db.EventLiveService;
import com.tong.fpl.service.db.PlayerService;
import com.tong.fpl.service.db.ScoutService;
import com.tong.fpl.utils.RedisUtils;
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
public class GroupServiceImpl implements IGroupService {

    private final IQueryService queryService;
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
                    .setGkpPoints(0)
                    .setDef(scoutData.getDef())
                    .setDefPoints(0)
                    .setMid(scoutData.getMid())
                    .setMidPoints(0)
                    .setFwd(scoutData.getFwd())
                    .setFwdPoints(0)
                    .setCaptain(scoutData.getCaptain())
                    .setCaptainPoints(0)
                    .setReason(StringUtils.isBlank(scoutData.getReason()) ? "" : scoutData.getReason())
                    .setEventPoints(0)
                    .setTotalPoints(0);
            scoutEntity
                    .setGkpTeamId(elementTeamMap.getOrDefault(scoutData.getGkp(), 0))
                    .setDefTeamId(elementTeamMap.getOrDefault(scoutData.getDef(), 0))
                    .setMidTeamId(elementTeamMap.getOrDefault(scoutData.getMid(), 0))
                    .setFwdTeamId(elementTeamMap.getOrDefault(scoutData.getFwd(), 0))
                    .setCaptainTeamId(elementTeamMap.getOrDefault(scoutData.getCaptain(), 0));
            this.scoutService.save(scoutEntity);
        } else {
            if (scoutData.getGkp() > 0) {
                scoutEntity
                        .setGkp(scoutData.getGkp())
                        .setGkpTeamId(elementTeamMap.getOrDefault(scoutData.getGkp(), 0));
            }
            if (scoutData.getDef() > 0) {
                scoutEntity
                        .setDef(scoutData.getDef())
                        .setDefTeamId(elementTeamMap.getOrDefault(scoutData.getDef(), 0));
            }
            if (scoutData.getMid() > 0) {
                scoutEntity
                        .setMid(scoutData.getMid())
                        .setMidTeamId(elementTeamMap.getOrDefault(scoutData.getMid(), 0));
            }
            if (scoutData.getFwd() > 0) {
                scoutEntity
                        .setFwd(scoutData.getFwd())
                        .setFwdTeamId(elementTeamMap.getOrDefault(scoutData.getFwd(), 0));
            }
            if (scoutData.getCaptain() > 0) {
                scoutEntity
                        .setCaptain(scoutData.getCaptain())
                        .setCaptainTeamId(elementTeamMap.getOrDefault(scoutData.getCaptain(), 0));
            }
            scoutEntity.setReason(StringUtils.isBlank(scoutData.getReason()) ? "" : scoutData.getReason());
            this.scoutService.updateById(scoutEntity);
        }
    }

    @Override
    public void updateEventScoutResult(int event) {
        List<ScoutEntity> list = Lists.newArrayList();
        Multimap<Integer, ScoutEntity> entryPointsMap = HashMultimap.create();
        this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
                .lt(ScoutEntity::getEvent, event))
                .forEach(o -> entryPointsMap.put(o.getEntry(), o));
        Map<Integer, Integer> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
                .eq(ScoutEntity::getEvent, event))
                .forEach(o -> {
                    o
                            .setGkpPoints(eventLiveMap.getOrDefault(o.getGkp(), 0))
                            .setDefPoints(eventLiveMap.getOrDefault(o.getDef(), 0))
                            .setMidPoints(eventLiveMap.getOrDefault(o.getMid(), 0))
                            .setFwdPoints(eventLiveMap.getOrDefault(o.getFwd(), 0))
                            .setCaptainPoints(eventLiveMap.getOrDefault(o.getCaptain(), 0));
                    o.setEventPoints(o.getGkpPoints() + o.getDefPoints() + o.getMidPoints() + o.getFwdPoints() + o.getCaptainPoints());
                    o.setTotalPoints(o.getEventPoints() +
                            entryPointsMap.get(o.getEntry())
                                    .stream()
                                    .mapToInt(ScoutEntity::getEventPoints)
                                    .sum()
                    );
                    list.add(o);
                });
        this.scoutService.updateBatchById(list);
        if (event == this.queryService.getCurrentEvent()) {
            String key = StringUtils.join("api::qryEventScoutResult::", event);
            RedisUtils.removeCacheByKey(key);
        }
        RedisUtils.removeCacheByKey("api::qryEventScoutResult::0");
    }

}

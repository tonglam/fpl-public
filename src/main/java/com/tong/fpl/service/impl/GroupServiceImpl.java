package com.tong.fpl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tong.fpl.constant.enums.FollowAccount;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryEventSimulatePickData;
import com.tong.fpl.domain.letletme.entry.EntryEventSimulateTransfersData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.service.IGroupService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.JsonUtils;
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
    private final EntryEventSimulatePickService entryEventSimulatePickService;
    private final EntryEventSimulateTransfersService entryEventSimulateTransfersService;

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
                    .setTransfers(0)
                    .setLeftTransfers(-1)
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
            scoutEntity
                    .setTransfers(0)
                    .setLeftTransfers(-1);
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

    @Override
    public void upsertEventScoutSimulatePick(EntryEventSimulatePickData entryEventSimulatePickData) {
        EntryEventResultData entryEventResultData = this.queryService.qryEntryEventResult(entryEventSimulatePickData.getEvent(), FollowAccount.Offiaccount.getEntry());
        if (entryEventResultData == null) {
            return;
        }
        EntryEventSimulatePickEntity entryEventSimulatePickEntity = new EntryEventSimulatePickEntity()
                .setEntry(entryEventSimulatePickData.getEntry())
                .setEvent(entryEventSimulatePickData.getEvent())
                .setOperator(entryEventSimulatePickData.getOperator())
                .setLineup(JsonUtils.obj2json(entryEventSimulatePickData.getLineup()));
        EntryEventSimulatePickEntity entryEventSimulatePick = this.entryEventSimulatePickService.getOne(new QueryWrapper<EntryEventSimulatePickEntity>().lambda()
                .eq(EntryEventSimulatePickEntity::getEntry, entryEventSimulatePickData.getEntry())
                .eq(EntryEventSimulatePickEntity::getEvent, entryEventSimulatePickData.getEvent())
                .eq(EntryEventSimulatePickEntity::getOperator, entryEventSimulatePickData.getOperator()));
        if (entryEventSimulatePick == null) {
            this.entryEventSimulatePickService.save(entryEventSimulatePickEntity);
        } else {
            entryEventSimulatePickEntity.setId(entryEventSimulatePick.getId());
            this.entryEventSimulatePickService.updateById(entryEventSimulatePickEntity);
        }
    }

    @Override
    public void upsertEventScoutSimulateTransfers(EntryEventSimulateTransfersData entryEventSimulateTransfersData) {
        // prepare
        EntryEventResultData entryEventResultData = this.queryService.qryEntryEventResult(entryEventSimulateTransfersData.getEvent() - 1, FollowAccount.Offiaccount.getEntry());
        if (entryEventResultData == null) {
            return;
        }
        List<Integer> entryPickList = entryEventResultData.getPicks()
                .stream()
                .map(EntryPickData::getElement)
                .collect(Collectors.toList());
        List<Integer> lineupList = entryEventSimulateTransfersData.getLineup()
                .stream()
                .map(EntryPickData::getElement)
                .collect(Collectors.toList());
        List<Integer> transfersIns = Lists.newArrayList();
        lineupList.forEach(o -> {
            if (!entryPickList.contains(o)) {
                transfersIns.add(o);
            }
        });
        String transfersIn = "";
        for (Integer o :
                transfersIns) {
            if (StringUtils.isEmpty(transfersIn)) {
                transfersIn = o + "";
                continue;
            }
            transfersIn = StringUtils.joinWith(",", transfersIn, o);
        }
        List<Integer> transfersOuts = Lists.newArrayList();
        entryPickList.forEach(o -> {
            if (!lineupList.contains(o)) {
                transfersOuts.add(o);
            }
        });
        String transfersOut = "";
        for (Integer o :
                transfersOuts) {
            if (StringUtils.isEmpty(transfersOut)) {
                transfersOut = o + "";
                continue;
            }
            transfersOut = StringUtils.joinWith(",", transfersOut, o);
        }
        // upsert
        EntryEventSimulateTransfersEntity entryEventSimulateTransfersEntity = new EntryEventSimulateTransfersEntity()
                .setEntry(entryEventSimulateTransfersData.getEntry())
                .setEvent(entryEventSimulateTransfersData.getEvent())
                .setOperator(entryEventSimulateTransfersData.getOperator())
                .setTeamValue(entryEventSimulateTransfersData.getTeamValue())
                .setBank(entryEventSimulateTransfersData.getBank())
                .setFreeTransfers(entryEventSimulateTransfersData.getFreeTransfers())
                .setTransfers(transfersIns.size())
                .setTransfersCost(entryEventSimulateTransfersData.getTransfersCost())
                .setTransfersIn(transfersIn)
                .setTransfersOut(transfersOut)
                .setLineup(JsonUtils.obj2json(entryEventSimulateTransfersData.getLineup()));
        EntryEventSimulateTransfersEntity entryEventSimulateTransfers = this.entryEventSimulateTransfersService.getOne(new QueryWrapper<EntryEventSimulateTransfersEntity>().lambda()
                .eq(EntryEventSimulateTransfersEntity::getEntry, entryEventSimulateTransfersData.getEntry())
                .eq(EntryEventSimulateTransfersEntity::getEvent, entryEventSimulateTransfersData.getEvent())
                .eq(EntryEventSimulateTransfersEntity::getOperator, entryEventSimulateTransfersData.getOperator()));
        if (entryEventSimulateTransfers == null) {
            this.entryEventSimulateTransfersService.save(entryEventSimulateTransfersEntity);
        } else {
            entryEventSimulateTransfersEntity.setId(entryEventSimulateTransfers.getId());
            this.entryEventSimulateTransfersService.updateById(entryEventSimulateTransfersEntity);
        }
    }

}

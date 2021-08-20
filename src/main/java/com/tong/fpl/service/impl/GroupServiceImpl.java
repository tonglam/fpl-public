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
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IGroupService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.db.EntryEventSimulatePickService;
import com.tong.fpl.service.db.EntryEventSimulateTransfersService;
import com.tong.fpl.service.db.EventLiveService;
import com.tong.fpl.service.db.ScoutService;
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

    private final IApiQueryService apiQueryService;
    private final IQueryService queryService;
    private final EventLiveService eventLiveService;
    private final ScoutService scoutService;
    private final EntryEventSimulatePickService entryEventSimulatePickService;
    private final EntryEventSimulateTransfersService entryEventSimulateTransfersService;

    @Override
    public String upsertEventScout(ScoutData scoutData) {
        // check basic params
        int event = scoutData.getEvent();
        int entry = scoutData.getEntry();
        if (event <= 0 || event > 38 || entry <= 0) {
            return "请检查参数";
        }
        Map<String, String> scoutMap = this.apiQueryService.qryScoutEntry();
        if (StringUtils.isEmpty(scoutData.getScoutName()) || !scoutMap.containsValue(scoutData.getScoutName())) {
            return "请检查球探名称";
        }
        // check element
        int gkp = scoutData.getGkp();
        int def = scoutData.getDef();
        int mid = scoutData.getMid();
        int fwd = scoutData.getFwd();
        int captain = scoutData.getCaptain();
        if (gkp <= 0 || def <= 0 || mid <= 0 || fwd <= 0 || captain <= 0) {
            return "请检查提交的球员";
        }
        PlayerEntity gkpInfo = this.queryService.getPlayerByElement(gkp);
        PlayerEntity defInfo = this.queryService.getPlayerByElement(def);
        PlayerEntity midInfo = this.queryService.getPlayerByElement(mid);
        PlayerEntity fwdInfo = this.queryService.getPlayerByElement(fwd);
        PlayerEntity captainInfo = this.queryService.getPlayerByElement(captain);
        if (gkpInfo == null || defInfo == null || midInfo == null || fwdInfo == null || captainInfo == null) {
            return "请检查提交的球员";
        }
        // check price
        if ((gkpInfo.getPrice() + defInfo.getPrice() + midInfo.getPrice() + fwdInfo.getPrice()) > 280) { // 写死28m
            return "提交的球员超出预算";
        }
        // check transfers
        int transfers = scoutData.getTransfers();
        int leftTransfers = scoutData.getLeftTransfers();
        int eventLeftTransfers = this.apiQueryService.qryEventScoutLeftTransfers(event, entry);
        if (eventLeftTransfers == -1) {
            leftTransfers = eventLeftTransfers;
        } else {
            ScoutEntity lastScoutEntity = this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
                    .eq(ScoutEntity::getEntry, scoutData.getEntry())
                    .orderByAsc(ScoutEntity::getEvent))
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (lastScoutEntity == null) {
                transfers = 0;
                leftTransfers = -1;
            } else if (leftTransfers + transfers > eventLeftTransfers) {
                return "换人超过名额";
            }
        }
        // upsert
        ScoutEntity scoutEntity = this.scoutService.getOne(new QueryWrapper<ScoutEntity>().lambda()
                .eq(ScoutEntity::getEvent, scoutData.getEvent())
                .eq(ScoutEntity::getEntry, scoutData.getEntry()));
        if (scoutEntity == null) {
            scoutEntity = new ScoutEntity()
                    .setEvent(scoutData.getEvent())
                    .setEntry(scoutData.getEntry())
                    .setScoutName(scoutData.getScoutName())
                    .setTransfers(transfers)
                    .setLeftTransfers(leftTransfers)
                    .setGkp(scoutData.getGkp())
                    .setGkpTeamId(gkpInfo.getTeamId())
                    .setGkpPoints(0)
                    .setDef(scoutData.getDef())
                    .setDefTeamId(defInfo.getTeamId())
                    .setDefPoints(0)
                    .setMid(scoutData.getMid())
                    .setMidTeamId(midInfo.getTeamId())
                    .setMidPoints(0)
                    .setFwd(scoutData.getFwd())
                    .setFwdTeamId(fwdInfo.getTeamId())
                    .setFwdPoints(0)
                    .setCaptain(scoutData.getCaptain())
                    .setCaptainTeamId(captainInfo.getTeamId())
                    .setCaptainPoints(0)
                    .setReason(StringUtils.isBlank(scoutData.getReason()) ? "" : scoutData.getReason())
                    .setEventPoints(0)
                    .setTotalPoints(0);
            this.scoutService.save(scoutEntity);
        } else {
            scoutEntity
                    .setTransfers(transfers)
                    .setLeftTransfers(leftTransfers)
                    .setGkp(scoutData.getGkp())
                    .setGkpTeamId(gkpInfo.getTeamId())
                    .setDef(scoutData.getDef())
                    .setDefTeamId(defInfo.getTeamId())
                    .setMid(scoutData.getMid())
                    .setMidTeamId(midInfo.getTeamId())
                    .setFwd(scoutData.getFwd())
                    .setFwdTeamId(fwdInfo.getTeamId())
                    .setCaptain(scoutData.getCaptain())
                    .setCaptainTeamId(captainInfo.getTeamId())
                    .setReason(StringUtils.isBlank(scoutData.getReason()) ? "" : scoutData.getReason());
            this.scoutService.updateById(scoutEntity);
        }
        return "提交成功";
    }

    @Override
    public void updateEventScoutResult(int event) {
        if (event <= 0) {
            return;
        }
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

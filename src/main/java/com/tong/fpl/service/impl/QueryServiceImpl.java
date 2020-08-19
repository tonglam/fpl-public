package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.constant.enums.teamName.TeamName_2021;
import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.EntryEventResultData;
import com.tong.fpl.domain.data.letletme.PlayerData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;
import com.tong.fpl.domain.entity.*;
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
                    playerValueData.setWebName(getPlayerWebName(o.getElement()));
                    playerValueData.setElementTypeName(Position.getNameFromElementType(o.getElementType()).name());
                    playerValueDataList.add(playerValueData);
                });
        return playerValueDataList;
    }

    private String getPlayerWebName(int element) {
        PlayerEntity playerEntity = this.playerService.getById(element);
        return playerEntity != null ? playerEntity.getWebName() : "";
    }

    @Override
    public EntryEventData qryEntryResult(String season, int entry) {
        return this.qryEntryEventResultData(season, entry);
    }

    @Override
    public EntryEventData qryEntryEventResult(String season, int event, int entry) {
        return this.qryEntryEventResultData(season, event, entry);
    }

    @Override
    public List<EventLiveEntity> qryEventLiveAll(String season, int element) {
        MybatisPlusConfig.season.set(season);
        List<EventLiveEntity> list = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getElement, element));
        MybatisPlusConfig.season.remove();
        return list;
    }

    @Override
    public List<EventLiveEntity> qryEventLive(String season, int event, int element) {
        MybatisPlusConfig.season.set(season);
        List<EventLiveEntity> list = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event).eq(EventLiveEntity::getElement, element));
        MybatisPlusConfig.season.remove();
        return list;
    }

    @Override
    public Page<PlayerData> qryPlayerDataList(long current, long size) {
        List<PlayerData> list = Lists.newArrayList();
        boolean searchTotal = false;
        if (current == 1) { // 第一页计算总数
            searchTotal = true;
        }
        Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
                new Page<>(current, size, searchTotal), new QueryWrapper<>());
        playerPage.getRecords().parallelStream().forEach(playerEntity -> {
            PlayerData playerData = new PlayerData();
            BeanUtil.copyProperties(playerEntity, playerData);
            playerData.setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType()).name());
            playerData.setTeamName(TeamName_2021.getTeamNameFromId(playerEntity.getTeamId()).name());
            // player_value
            PlayerValueEntity playerValueEntity = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
                    .eq(PlayerValueEntity::getElement, playerEntity.getElement())
                    .orderByDesc(PlayerValueEntity::getChangeDate))
                    .get(0);
            if (playerValueEntity != null) {
                BeanUtil.copyProperties(playerValueEntity, playerData);
            }
            list.add(playerData);
        });
        Page<PlayerData> page = new Page<>(current, size, playerPage.getTotal());
        page.setRecords(list);
        return page;
    }

    @Override
    public PlayerEntity qryPlayerInfo(String season, int element) {
        MybatisPlusConfig.season.set(season);
        PlayerEntity playerEntity = this.playerService.getById(element);
        MybatisPlusConfig.season.remove();
        return playerEntity;
    }

    private EntryEventData qryEntryEventResultData(String season, int entry) {
        return this.qryEntryEventResultData(season, 0, entry);
    }

    private EntryEventData qryEntryEventResultData(String season, int event, int entry) {
        EntryEventData entryEventData = new EntryEventData();
        // entry_info
        MybatisPlusConfig.season.set(season);
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getOne(new QueryWrapper<EntryInfoEntity>().lambda().
                eq(EntryInfoEntity::getEntry, entry));
        if (entryInfoEntity == null) {
            return entryEventData;
        }
        BeanUtil.copyProperties(entryInfoEntity, entryEventData);
        // entry_event_result
        entryEventData.setEventResultDatas(this.setEntryEventResult(event, entry));
        MybatisPlusConfig.season.remove();
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

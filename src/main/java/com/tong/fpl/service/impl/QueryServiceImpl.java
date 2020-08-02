package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;
import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.domain.entity.EntryInfoEntity;
import com.tong.fpl.domain.entity.PlayerValueEntity;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.db.EntryEventResultService;
import com.tong.fpl.service.db.EntryInfoService;
import com.tong.fpl.service.db.PlayerValueService;
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

    private final PlayerValueService playerValueService;
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
                    playerValueData.setWebName("");
                    playerValueData.setElementTypeName("");
                });
        return playerValueDataList;
    }

    @Override
    public EntryEventData qryEntryEvent(int event, int entry) {
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event).eq(EntryEventResultEntity::getEntry, entry));
        if (entryEventResultEntity == null) {
            return new EntryEventData();
        }
        EntryEventData entryEventData = new EntryEventData();
        BeanUtil.copyProperties(entryEventResultEntity, entryEventData);
        entryEventData.setEventPicks(CommonUtils.getPickListFromPicks(entryEventResultEntity.getEventPicks()));
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getOne(new QueryWrapper<EntryInfoEntity>().lambda().
                eq(EntryInfoEntity::getEntry, entry));
        BeanUtil.copyProperties(entryInfoEntity, entryEventData);
        return entryEventData;
    }


}

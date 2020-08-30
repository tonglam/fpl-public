package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.enums.HistorySeason;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.api.EntryEventData;
import com.tong.fpl.domain.letletme.api.EntryEventResultData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IRedisCacheSerive;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/7/31
 */
@Slf4j
@Valid
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueryServiceImpl implements IQuerySerivce {

    private final IRedisCacheSerive redisCacheSerive;
    private final PlayerService playerService;
    private final PlayerStatService playerStatService;
    private final EntryInfoService entryInfoService;
    private final EventLiveService eventLiveService;
    private final EntryEventResultService entryEventResultService;

    @Override
    public Map<Integer, PlayerEntity> qryAllPlayerList(String season) {
        MybatisPlusConfig.season.set(season);
        List<PlayerEntity> list = this.playerService.list();
        MybatisPlusConfig.season.remove();
        return list.stream().collect(Collectors.toMap(PlayerEntity::getElement, v -> v));
    }

    @Override
    public Map<Integer, PlayerStatEntity> qryAllPlayerStatList(String season) {
        MybatisPlusConfig.season.set(season);
        List<PlayerStatEntity> list = this.playerStatService.list();
        MybatisPlusConfig.season.remove();
        return list.stream().collect(Collectors.toMap(PlayerStatEntity::getElement, v -> v));
    }

    @Cacheable(value = "qryPlayerElementByCode", key = "#season+'::'+#code", cacheManager = "apiCacheManager")
    @Override
    public int qryPlayerElementByCode(String season, int code) {
        MybatisPlusConfig.season.set(season);
        PlayerEntity playerEntity = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getCode, code));
        MybatisPlusConfig.season.remove();
        return playerEntity == null ? 0 : playerEntity.getElement();
    }

    @Cacheable(value = "qryPlayerElementByWebName", key = "#season+'::'+#webName", cacheManager = "apiCacheManager")
    @Override
    public int qryPlayerElementByWebName(String season, String webName) throws Exception {
        MybatisPlusConfig.season.set(season);
        List<PlayerEntity> playerList = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda().eq(PlayerEntity::getWebName, webName));
        MybatisPlusConfig.season.remove();
        if (CollectionUtils.isEmpty(playerList)) {
            return 0;
        } else if (playerList.size() > 1) {
            throw new Exception("webname不止一个球员，请用element或code查询!");
        }
        return playerList.get(0) == null ? 0 : playerList.get(0).getElement();
    }

    @Cacheable(value = "qryAllPlayerList", key = "#season+'::'+#entry", cacheManager = "apiCacheManager")
    @Override
    public EntryEventData qryEntryResult(String season, int entry) {
        EntryEventData entryEventData = this.setEntryInfoData(season, entry);
        List<EntryEventResultData> list = Lists.newArrayList();
        IntStream.range(1, 39).forEach(event -> list.add(this.setEntryEventResult(season, event, entry)));
        entryEventData.setEventResultList(list);
        return entryEventData;
    }

    @Cacheable(value = "qryAllPlayerList", key = "#season+'::'+#event+'::'+#entry", cacheManager = "apiCacheManager")
    @Override
    public EntryEventData qryEntryEventResult(String season, int event, int entry) {
        EntryEventData entryEventData = this.setEntryInfoData(season, entry);
        List<EntryEventResultData> list = Lists.newArrayList();
        list.add(this.setEntryEventResult(season, event, entry));
        entryEventData.setEventResultList(list);
        return entryEventData;
    }

    private EntryEventData setEntryInfoData(String season, int entry) {
        EntryEventData entryEventData = new EntryEventData();
        MybatisPlusConfig.season.set(season);
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getOne(new QueryWrapper<EntryInfoEntity>().lambda().
                eq(EntryInfoEntity::getEntry, entry));
        MybatisPlusConfig.season.remove();
        if (entryInfoEntity == null) {
            return entryEventData;
        }
        BeanUtil.copyProperties(entryInfoEntity, entryEventData, CopyOptions.create().ignoreNullValue());
        return entryEventData;
    }

    private EntryEventResultData setEntryEventResult(String season, int event, int entry) {
        EntryEventResultData entryEventResultData = new EntryEventResultData();
        MybatisPlusConfig.season.set(season);
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event).eq(EntryEventResultEntity::getEntry, entry));
        MybatisPlusConfig.season.remove();
        if (entryEventResultEntity == null) {
            return entryEventResultData;
        }
        BeanUtil.copyProperties(entryEventResultEntity, entryEventResultData, CopyOptions.create().ignoreNullValue());
        entryEventResultData.setEventPicks(this.qryPickListFromPicks(season, entryEventResultEntity.getEventPicks()));
        return entryEventResultData;
    }

    @Cacheable(value = "qryEventLiveAll", key = "#season+'::'+#element", cacheManager = "apiCacheManager")
    @Override
    public List<EventLiveEntity> qryEventLiveAll(String season, int element) {
        MybatisPlusConfig.season.set(season);
        List<EventLiveEntity> list = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda().eq(EventLiveEntity::getElement, element));
        MybatisPlusConfig.season.remove();
        return list;
    }

    @Cacheable(value = "qryEventLive", key = "#season+'::'+#event+'::'+#element", cacheManager = "apiCacheManager")
    @Override
    public List<EventLiveEntity> qryEventLive(String season, int event, int element) {
        MybatisPlusConfig.season.set(season);
        List<EventLiveEntity> list = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event).eq(EventLiveEntity::getElement, element));
        MybatisPlusConfig.season.remove();
        return list;
    }

    @Cacheable(value = "qryPlayerData", key = "#element", cacheManager = "apiCacheManager")
    @Override
    public PlayerData qryPlayerData(int element) {
        PlayerEntity playerEntity = this.redisCacheSerive.getPlayerByElememt(element);
        if (playerEntity == null) {
            return null;
        }
        PlayerData playerData = new PlayerData();
        // info
        playerData.setInfoData(this.initPlayerInfo(playerEntity));
        // fixture, next 5 gw
        playerData.setFixtureDataList(this.setPlayerFixture(playerEntity.getTeamId()));
        // current season data
        playerData.setCurrentSeason(this.setSeasonData(CommonUtils.getCurrentSeason(), element));
        // history season data（use code as unique index）
        playerData.setHistorySeasonList(this.setHistorySeasonData(playerEntity.getCode()));
        return playerData;
    }

    @Cacheable(value = "initPlayerInfo", key = "#playerEntity.element", condition = "#playerEntity.element gt 0")
    @Override
    public PlayerInfoData initPlayerInfo(PlayerEntity playerEntity) {
        Map<Integer, String> teamNameMap = this.redisCacheSerive.getTeamNameMap();
        Map<Integer, String> positionMap = CommonUtils.getPositonMap();
        return new PlayerInfoData()
                .setElement(playerEntity.getElement())
                .setCode(playerEntity.getCode())
                .setWebName(playerEntity.getWebName())
                .setElementType(playerEntity.getElementType())
                .setElementTypeName(positionMap.get(playerEntity.getElementType()))
                .setTeamId(playerEntity.getTeamId())
                .setTeamName(teamNameMap.get(playerEntity.getTeamId()))
                .setPrice(playerEntity.getPrice() / 10.0);
    }

    private List<PlayerFixtureData> setPlayerFixture(int teamId) {
        List<PlayerFixtureData> playerFixtureList = Lists.newArrayList();
        int currentEvent = this.redisCacheSerive.getCurrentEvent();
        Map<Integer, String> teamNameMap = this.redisCacheSerive.getTeamNameMap();
        Map<Integer, String> teamShortNameMap = this.redisCacheSerive.getTeamShortNameMap();
        this.redisCacheSerive.getEventFixtureByTeamId(teamId).subList(currentEvent - 1, currentEvent + 4).forEach(o -> {
                    o.setAgainstTeamName(teamNameMap.get(o.getAgainstTeamId()));
                    o.setAgainstTeamShortName(teamShortNameMap.get(o.getAgainstTeamId()));
                    playerFixtureList.add(o);
                }
        );
        return playerFixtureList;
    }

    private PlayerDetailData setSeasonData(String season, int code) {
        // get history_element
        int element = this.qryPlayerElementByCode(season, code);
        PlayerDetailData playerDetailData = new PlayerDetailData().setSeason(season);
        PlayerStatEntity playerStatEntity = this.redisCacheSerive.getPlayerStatByElement(season, element);
        if (playerStatEntity == null) {
            return playerDetailData;
        }
        BeanUtil.copyProperties(playerStatEntity, playerDetailData, CopyOptions.create().ignoreNullValue());
        return playerDetailData;
    }

    private List<PlayerDetailData> setHistorySeasonData(int code) {
        List<PlayerDetailData> historySeasonList = Lists.newArrayList();
        Arrays.stream(HistorySeason.values()).forEach(o ->
                historySeasonList.add(this.setSeasonData(o.getSeason(), code)));
        return historySeasonList;
    }

    @Cacheable(value = "qryAllPlayers", key = "#season", cacheManager = "apiCacheManager")
    @Override
    public List<PlayerInfoData> qryAllPlayers(String season) {
        List<PlayerInfoData> list = Lists.newArrayList();
        this.qryAllPlayerList(season).values()
                .forEach(o -> list.add(initPlayerInfo(o)));
        return list;
    }

    @Override
    public List<Pick> qryPickListFromPicks(String season, @NotNull String picks) {
        List<Pick> pickList = JsonUtils.json2Collection(picks, List.class, Pick.class);
        if (CollectionUtils.isEmpty(pickList)) {
            return Lists.newArrayList();
        }
        Map<Integer, String> positonMap = CommonUtils.getPositonMap();
        pickList.forEach(pick -> {
            PlayerEntity playerEntity = this.redisCacheSerive.getPlayerByElememt(season, pick.getElement());
            if (playerEntity != null) {
                pick.setElementTypeName(positonMap.get(playerEntity.getElementType()))
                        .setWebName(playerEntity.getWebName());
            }
        });
        return pickList;
    }

}

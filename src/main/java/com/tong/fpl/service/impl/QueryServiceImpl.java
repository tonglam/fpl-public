package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.enums.HistorySeason;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.domain.data.letletme.api.EntryEventData;
import com.tong.fpl.domain.data.letletme.api.EntryEventResultData;
import com.tong.fpl.domain.data.letletme.player.*;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.service.ICacheSerive;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/7/31
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QueryServiceImpl implements IQuerySerivce {

    private final TeamService teamService;
    private final PlayerService playerService;
    private final PlayerValueService playerValueService;
    private final EventService eventService;
    private final EventFixtureService eventFixtureService;
    private final EventLiveService eventLiveService;
    private final EntryInfoService entryInfoService;
    private final EntryEventResultService entryEventResultService;
    private final ICacheSerive cacheSerive;

    @Override
    public TeamEntity qryTeamEntityByTeamId(int teamId) {
        return this.qryTeamEntityByTeamId(CommonUtils.getCurrentSeason(), teamId);
    }

    @Cacheable(cacheNames = "TeamEntity::id", keyGenerator = "entityKeyGenerator")
    @Override
    public TeamEntity qryTeamEntityByTeamId(String season, int teamId) {
        return this.teamService.getById(teamId);
    }

    @Override
    public EventEntity qryEventEntityByEvent(int event) {
        return this.qryEventEntityByEvent(CommonUtils.getCurrentSeason(), event);
    }

    @Cacheable(cacheNames = "EventEntity::id", keyGenerator = "entityKeyGenerator")
    @Override
    public EventEntity qryEventEntityByEvent(String season, int eventId) {
        return this.eventService.getById(eventId);
    }

    @Override
    public PlayerEntity qryPlayerEntityByElement(int element) {
        return this.qryPlayerEntityByElement(CommonUtils.getCurrentSeason(), element);
    }

    @Cacheable(cacheNames = "PlayerEntity::id", keyGenerator = "entityKeyGenerator")
    @Override
    public PlayerEntity qryPlayerEntityByElement(String season, int element) {
        return this.playerService.getById(element);
    }

    @Cacheable(value = "#changDate")
    @Override
    public List<PlayerValueData> qryDayChangePlayerValue(String changeDate) {
        List<PlayerValueData> playerValueDataList = Lists.newArrayList();
        this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
                .eq(PlayerValueEntity::getChangeDate, changeDate))
                .forEach(o -> {
                    PlayerValueData playerValueData = new PlayerValueData();
                    BeanUtil.copyProperties(o, playerValueData, CopyOptions.create().ignoreNullValue());
                    playerValueData.setWebName(getPlayerWebName(o.getElement()));
                    playerValueData.setElementTypeName(Position.getNameFromElementType(o.getElementType()).name());
                    playerValueDataList.add(playerValueData);
                });
        return playerValueDataList;
    }

    private String getPlayerWebName(int element) {
        return this.qryPlayerEntityByElement(element).getWebName();
    }

    @Override
    public EntryEventData qryEntryResult(String season, int entry) {
        return this.qryEntryEventResultData(season, entry);
    }

    @Cacheable(cacheNames = "EntryEventData")
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
    public PlayerData qryPlayerData(PlayerQueryParam playerQueryParam) throws Exception {
        PlayerEntity playerEntity = this.getPlayEntityFromQueryParam(playerQueryParam);
        if (playerEntity == null) {
            return new PlayerData();
        }
        return this.qryPlayerData(playerEntity);
    }

    private PlayerEntity getPlayEntityFromQueryParam(PlayerQueryParam playerQueryParam) throws Exception {
        PlayerEntity playerEntity = null;
        if (playerQueryParam.getElement() > 0) {
            playerEntity = this.playerService.getById(playerQueryParam.getElement());
        } else {
            if (playerQueryParam.getCode() > 0) {
                playerEntity = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda()
                        .eq(PlayerEntity::getCode, playerQueryParam.getCode()));
            } else {
                if (StringUtils.isNoneBlank(playerQueryParam.getWebName())) {
                    List<PlayerEntity> list = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda()
                            .eq(PlayerEntity::getWebName, playerQueryParam.getWebName()));
                    if (list.size() == 1) {
                        playerEntity = list.get(0);
                    } else {
                        throw new Exception("webname不止一个球员，请用element或code查询!");
                    }
                }
            }
        }
        return playerEntity;
    }

    @Override
    public Page<PlayerData> qryPagePlayerDataList(long current, long size) {
        List<PlayerData> list = Lists.newArrayList();
        boolean searchTotal = false;
        if (current == 1) { // 第一页计算总数
            searchTotal = true;
        }
        Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
                new Page<>(current, size, searchTotal), new QueryWrapper<>());
        playerPage.getRecords().forEach(playerEntity -> {
            // info
            PlayerData playerData = this.qryPlayerData(playerEntity);
            list.add(playerData);
        });
        Page<PlayerData> page = new Page<>(current, size, playerPage.getTotal());
        page.setRecords(list);
        return page;
    }

    private PlayerData qryPlayerData(PlayerEntity playerEntity) {
        // info
        PlayerData playerData = this.setPlayerInfo(playerEntity);
        // fixture, next 5 gw
        this.setPlayerFixture(playerData);
        // current season data
        this.setCurrentSeasonData(playerData, playerEntity);
        // history season data
        this.setAllHistorySeasonData(playerData);
        return playerData;
    }

    private PlayerData setPlayerInfo(PlayerEntity playerEntity) {
        PlayerData playerData = new PlayerData();
        playerData.setInfoData(new PlayerInfoData()
                .setElement(playerEntity.getElement())
                .setCode(playerEntity.getCode())
                .setWebName(playerEntity.getWebName())
                .setElementTypeName(Position.getNameFromElementType(playerEntity.getElementType()).name())
                .setTeamId(playerEntity.getTeamId())
                .setTeamName(this.getTeamNameByTeamId(playerEntity.getTeamId()))
                .setPrice(playerEntity.getPrice())
        );
        return playerData;
    }

    private void setPlayerFixture(PlayerData playerData) {
        int teamId = playerData.getInfoData().getTeamId();
        List<PlayerFixtureData> fixtureDataList = Lists.newArrayList();
        int currentEvent = CommonUtils.getCurrentEvent();
        IntStream.range(currentEvent, currentEvent + 5).forEach(event -> {
            List<EventFixtureEntity> eventFixtureEntityList = this.eventFixtureService.list(new QueryWrapper<EventFixtureEntity>().lambda()
                    .eq(EventFixtureEntity::getEvent, event)
                    .and(o -> o.eq(EventFixtureEntity::getTeamH, teamId).or(i -> i.eq(EventFixtureEntity::getTeamA, teamId)))
            );
            eventFixtureEntityList.forEach(eventFixtureEntity -> {
                boolean wasHome = eventFixtureEntity.getTeamH() == teamId;
                fixtureDataList.add(new PlayerFixtureData()
                        .setEvent(event)
                        .setAgainstTeam(wasHome ?
                                this.getTeamNameByTeamId(teamId) : this.getTeamNameByTeamId(eventFixtureEntity.getTeamA()))
                        .setAgainstTeamShortName(wasHome ?
                                this.getTeamShortNameByTeamId(teamId) : this.getTeamShortNameByTeamId(eventFixtureEntity.getTeamA()))
                        .setKickoffTime(eventFixtureEntity.getKickoffTime())
                        .setDifficulty(wasHome ? eventFixtureEntity.getTeamHDifficulty() : eventFixtureEntity.getTeamADifficulty())
                        .setWasHome(wasHome)
                        .setStarted(eventFixtureEntity.isStarted())
                        .setFinished(eventFixtureEntity.isFinished())
                );
            });
        });
        playerData.setFixtureDataList(fixtureDataList);
    }

    private String getTeamNameByTeamId(int teamId) {
        return this.qryTeamEntityByTeamId(teamId).getName();
    }

    private String getTeamShortNameByTeamId(int teamId) {
        return this.qryTeamEntityByTeamId(teamId).getShortName();
    }

    private void setCurrentSeasonData(PlayerData playerData, PlayerEntity playerEntity) {
        PlayerDetailData playerDetailData = new PlayerDetailData();
        BeanUtil.copyProperties(playerEntity, playerDetailData, CopyOptions.create().ignoreNullValue());
        PlayerValueEntity playerValueEntity = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
                .eq(PlayerValueEntity::getElement, playerEntity.getElement())
                .orderByDesc(PlayerValueEntity::getChangeDate))
                .get(0);
        if (playerValueEntity != null) {
            BeanUtil.copyProperties(playerValueEntity, playerDetailData, CopyOptions.create().ignoreNullValue());
        }
        playerData.setCurrentSeason(playerDetailData);
    }

    private void setAllHistorySeasonData(PlayerData playerData) {
        Arrays.stream(HistorySeason.values()).forEach(o -> {
            List<PlayerDetailData> historySeasonList = Lists.newArrayList();
            MybatisPlusConfig.season.set(o.getSeason());
            PlayerDetailData historyData = this.setHistorySeasonData(playerData);
            MybatisPlusConfig.season.remove();
            if (historyData != null) {
                historySeasonList.add(historyData);
            }
            playerData.setHistorySeasonList(historySeasonList);
        });
    }

    private PlayerDetailData setHistorySeasonData(PlayerData playerData) {
        PlayerEntity playerEntity = this.playerService.getOne(new QueryWrapper<PlayerEntity>().lambda()
                .eq(PlayerEntity::getCode, playerData.getInfoData().getCode()));
        if (playerEntity == null) {
            return null;
        }
        PlayerDetailData playerHistoryData = new PlayerDetailData();
        BeanUtil.copyProperties(playerEntity, playerHistoryData, CopyOptions.create().ignoreNullValue());
        PlayerValueEntity playerValueEntity = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
                .eq(PlayerValueEntity::getElement, playerEntity.getElement())
                .orderByDesc(PlayerValueEntity::getChangeDate))
                .get(0);
        if (playerValueEntity != null) {
            BeanUtil.copyProperties(playerValueEntity, playerHistoryData, CopyOptions.create().ignoreNullValue());
        }
        return playerHistoryData;
    }

    @Override
    public List<PlayerData> qryAllPlayers(String season) {
        List<PlayerData> list = Lists.newArrayList();
        List<PlayerEntity> playerEntityList = this.cacheSerive.getMultiValues("PlayerEntity::element::" + season + "*")
                .stream()
                .map(o -> (PlayerEntity) o)
                .sorted(Comparator.comparing(PlayerEntity::getElement))
                .collect(Collectors.toList());
        playerEntityList.forEach(playerEntity -> list.add(this.setPlayerInfo(playerEntity)));
        return list;
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
        BeanUtil.copyProperties(entryInfoEntity, entryEventData, CopyOptions.create().ignoreNullValue());
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
            BeanUtil.copyProperties(entryEventResultEntity, entryEventResultData, CopyOptions.create().ignoreNullValue());
            entryEventResultData.setEventPicks(CommonUtils.getPickListFromPicks(entryEventResultEntity.getEventPicks()));
            entryEventResultDataList.add(entryEventResultData);
        });
        return entryEventResultDataList;
    }

}

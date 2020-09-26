package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.service.ILiveService;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.ITableQueryService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/8/28
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TableQueryServiceImpl implements ITableQueryService {

    private final IQuerySerivce querySerivce;
    private final ILiveService liveService;
    private final PlayerService playerService;
    private final PlayerValueService playerValueService;
    private final EventLiveService eventLiveService;
    private final EntryInfoService entryInfoService;
    private final EntryEventResultService entryEventResultService;
    private final TournamentInfoService tournamentInfoService;
    private final TournamentEntryService tournamentEntryService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
    private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
    private final TeamSelectStatService teamSelectStatService;

    /**
     * @apiNote player
     */
    @Override
    public TableData<PlayerInfoData> qryPlayerList(String season) {
        List<PlayerInfoData> list = this.querySerivce.qryAllPlayers(season);
        list = list.stream().sorted(Comparator.comparing(PlayerInfoData::getPrice).reversed()).collect(Collectors.toList());
        return new TableData<>(list);
    }

    @Cacheable(value = "qryPagePlayerDataList", key = "#page+'::'+#limit", unless = "#result == null")
    @Override
    public TableData<PlayerInfoData> qryPagePlayerDataList(long page, long limit) {
        List<PlayerInfoData> list = Lists.newArrayList();
        Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
                new Page<>(page, limit, this.setSearchTotal(page)), new QueryWrapper<>());
        playerPage.getRecords().forEach(o ->
                list.add(BeanUtil.copyProperties(this.querySerivce.initPlayerInfo(CommonUtils.getCurrentSeason(), o), PlayerInfoData.class)));
        Page<PlayerInfoData> pageResult = new Page<>(page, limit, playerPage.getTotal());
        pageResult.setRecords(list);
        return new TableData<>(pageResult);
    }

    @Cacheable(value = "qryPriceChangeList")
    public TableData<PlayerValueData> qryPriceChangeList() {
        // prepare
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        Map<Integer, String> teamNameMap = Maps.newHashMap();
        this.querySerivce.getTeamNameMap().forEach((k, v) -> teamNameMap.put(Integer.valueOf(k), v));
        Map<Integer, String> teamShortNameMap = Maps.newHashMap();
        this.querySerivce.getTeamShortNameMap().forEach((k, v) -> teamShortNameMap.put(Integer.valueOf(k), v));
        Map<Integer, String> positionMap = Maps.newHashMap();
        this.querySerivce.getPositionMap().forEach((k, v) -> positionMap.put(Integer.valueOf(k), v));
        // player value
        List<PlayerValueData> list = Lists.newArrayList();
        this.playerValueService.list().forEach(o -> {
            PlayerValueData playerValueData = new PlayerValueData();
            BeanUtil.copyProperties(o, playerValueData, CopyOptions.create().ignoreNullValue());
            PlayerEntity playerEntity = playerMap.get(o.getElement());
            if (playerEntity != null) {
                int teamId = playerEntity.getTeamId();
                playerValueData
                        .setWebName(playerEntity.getWebName())
                        .setTeamName(teamNameMap.getOrDefault(teamId, ""))
                        .setTeamShortName(teamShortNameMap.getOrDefault(teamId, ""))
                        .setElementTypeName(positionMap.getOrDefault(o.getElementType(), ""));
            }
            list.add(playerValueData);
        });
        return new TableData<>(list);
    }

    /**
     * @apiNote entry
     */
    // TODO: 2020/9/23
    @Override
    public TableData<EntryInfoData> qryEntryInfoByTournament(String season, int tournamentId) {
        return new TableData<>();
    }

    /**
     * @apiNote tournament
     */
    @Override
    public TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param) {
        List<TournamentInfoData> list = Lists.newArrayList();
        // get tournament info
        LambdaQueryWrapper<TournamentInfoEntity> queryWrapper = new QueryWrapper<TournamentInfoEntity>().lambda();
        if (param.getEntry() > 0) {
            List<Integer> tournamentIdList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                    .eq(TournamentEntryEntity::getEntry, param.getEntry()))
                    .stream()
                    .map(TournamentEntryEntity::getTournamentId)
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(tournamentIdList)) {
                queryWrapper.in(TournamentInfoEntity::getId, tournamentIdList);
            }
        }
        if (StringUtils.isNotBlank(param.getName())) {
            queryWrapper.eq(TournamentInfoEntity::getName, param.getName());
        } else {
            if (StringUtils.isNotBlank(param.getCreator())) {
                queryWrapper.eq(TournamentInfoEntity::getCreator, param.getCreator());
            } else if (param.getLeagueId() > 0) {
                queryWrapper.eq(TournamentInfoEntity::getLeagueId, param.getLeagueId());
            } else if (StringUtils.isNotBlank(param.getCreateTime())) {
                queryWrapper.gt(TournamentInfoEntity::getCreateTime, param.getCreateTime());
                queryWrapper.lt(TournamentInfoEntity::getCreateTime, LocalDate.parse(param.getCreateTime()).plusDays(1).format(DateTimeFormatter.ofPattern(Constant.DATE)));
            }
        }
        if (queryWrapper.getExpression().getNormal().size() == 0) {
            return new TableData<>();
        }
        queryWrapper.eq(TournamentInfoEntity::getState, 1);
        // return
        this.tournamentInfoService.list(queryWrapper).forEach(o -> {
            TournamentInfoData tournamentInfoData = new TournamentInfoData();
            BeanUtil.copyProperties(o, tournamentInfoData, CopyOptions.create().ignoreNullValue());
            tournamentInfoData.setGroupMode(GroupMode.valueOf(o.getGroupMode()).getModeName())
                    .setGroupStartGw(CommonUtils.setRealGw(o.getGroupStartGw()))
                    .setGroupEndGw(CommonUtils.setRealGw(o.getGroupEndGw()))
                    .setKnockoutMode(KnockoutMode.valueOf(o.getKnockoutMode()).getModeName())
                    .setKnockoutStartGw(CommonUtils.setRealGw(o.getKnockoutStartGw()))
                    .setKnockoutEndGw(CommonUtils.setRealGw(o.getKnockoutEndGw()))
                    .setGroupFillAverage(o.getGroupFillAverage() ? "是" : "否")
                    .setCreateTime(StringUtils.substringBefore(o.getCreateTime(), " "));
            list.add(tournamentInfoData);
        });
        return new TableData<>(list);
    }

    @Override
    public TableData<TournamentEntryData> qryEntryTournamentList(int entry) {
        List<TournamentEntryData> list = Lists.newArrayList();
        if (entry == 0) {
            return new TableData<>();
        }
        int currentEvent = this.querySerivce.getCurrentEvent();
        // get tournament_list
        List<Integer> tournamentList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                .eq(TournamentEntryEntity::getEntry, entry))
                .stream()
                .map(TournamentEntryEntity::getTournamentId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tournamentList)) {
            return new TableData<>();
        }
        // stadge_mode
        Map<String, GroupMode> groupModeMap = Arrays.stream(GroupMode.values()).collect(Collectors.toMap(Enum::name, v -> v));
        Map<String, KnockoutMode> knockModeMap = Arrays.stream(KnockoutMode.values()).collect(Collectors.toMap(Enum::name, v -> v));
        // return
        this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
                .in(TournamentInfoEntity::getId, tournamentList)
                .eq(TournamentInfoEntity::getState, 1))
                .forEach(o ->
                        list.add(new TournamentEntryData()
                                .setEntry(entry)
                                .setTournamentId(o.getId())
                                .setName(o.getName())
                                .setCreator(o.getCreator())
                                .setSeason(o.getSeason())
                                .setLeagueType(o.getLeagueType())
                                .setLeagueId(o.getLeagueId())
                                .setGroupMode(groupModeMap.get(o.getGroupMode()).getModeName())
                                .setKnockoutMode(knockModeMap.get(o.getKnockoutMode()).getModeName())
                                .setStadge(this.setCurrentStadge(currentEvent, groupModeMap.get(o.getGroupMode()), o))
                                .setCreateTime(StringUtils.substringBefore(o.getCreateTime(), " "))
                        ));
        return new TableData<>(list);
    }

    private String setCurrentStadge(int currentEvent, GroupMode groupMode, TournamentInfoEntity tournamentInfoEntity) {
        switch (groupMode) {
            case No_group: {
                if (currentEvent > tournamentInfoEntity.getKnockoutStartGw()) {
                    return "淘汰赛";
                } else if (currentEvent > tournamentInfoEntity.getKnockoutEndGw()) {
                    return "已结束";
                }
                break;
            }
            case Points_race:
            case Battle_race: {
                int groupStartGw = tournamentInfoEntity.getGroupStartGw();
                int knockoutStartGw = tournamentInfoEntity.getKnockoutStartGw();
                int knockoutEndGw = tournamentInfoEntity.getKnockoutEndGw();
                if (groupStartGw > 0 && currentEvent > groupStartGw) {
                    return "小组赛";
                } else if (knockoutStartGw > 0 && currentEvent > knockoutStartGw) {
                    return "淘汰赛";
                } else if (knockoutEndGw > 0 && currentEvent > knockoutEndGw) {
                    return "已结束";
                }
                break;
            }
        }
        return "未开始";
    }

    @Override
    public TableData<TournamentInfoData> qryEntryPointsGroupTournamentList(int entry) {
        if (entry <= 0) {
            return new TableData<>();
        }
        // get tournament_list
        List<Integer> tournamentList = this.tournamentEntryService.list(new QueryWrapper<TournamentEntryEntity>().lambda()
                .eq(TournamentEntryEntity::getEntry, entry))
                .stream()
                .map(TournamentEntryEntity::getTournamentId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(tournamentList)) {
            return new TableData<>();
        }
        List<TournamentInfoData> list = Lists.newArrayList();
        this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
                .in(TournamentInfoEntity::getId, tournamentList)
                .eq(TournamentInfoEntity::getGroupMode, GroupMode.Points_race.name())
                .eq(TournamentInfoEntity::getGroupNum, 1)
                .eq(TournamentInfoEntity::getState, 1))
                .forEach(o -> list.add(new TournamentInfoData()
                        .setId(o.getId())
                        .setName(o.getName())
                ));
        return new TableData<>(list);
    }

    @Override
    public TableData<TournamentGroupData> qryTournamentResultList(int tournamentId, int event) {
        List<TournamentGroupData> list = Lists.newArrayList();
        Map<Integer, TournamentPointsGroupResultEntity> pointsGroupResultMap = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                .eq(TournamentPointsGroupResultEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(TournamentPointsGroupResultEntity::getEntry, v -> v));
        this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .orderByAsc(TournamentGroupEntity::getGroupRank))
                .forEach(o -> {
                    // tournament group
                    TournamentGroupData tournamentGroupData = new TournamentGroupData();
                    tournamentGroupData
                            .setGroupId(o.getGroupId())
                            .setGroupRank(o.getGroupRank())
                            .setEntry(o.getEntry())
                            .setOverallPoints(o.getOverallPoints())
                            .setOverallRank(o.getOverallRank());
                    // entry info
                    EntryInfoEntity entryInfoEntity = this.querySerivce.qryEntryInfo(o.getEntry());
                    if (entryInfoEntity != null) {
                        tournamentGroupData
                                .setEntryName(entryInfoEntity.getEntryName())
                                .setPlayerName(entryInfoEntity.getPlayerName());
                    }
                    // tournament group result
                    TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity = pointsGroupResultMap.getOrDefault(o.getEntry(), new TournamentPointsGroupResultEntity());
                    if (tournamentPointsGroupResultEntity != null) {
                        tournamentGroupData.setPointsGroupEventResult(new TournamentPointsGroupEventResultData()
                                .setGroupId(tournamentPointsGroupResultEntity.getGroupId())
                                .setEvent(tournamentPointsGroupResultEntity.getEvent())
                                .setEntry(tournamentPointsGroupResultEntity.getEntry())
                                .setGroupRank(tournamentPointsGroupResultEntity.getEventGroupRank())
                                .setPoints(tournamentPointsGroupResultEntity.getEventPoints())
                                .setCost(tournamentPointsGroupResultEntity.getEventCost())
                                .setNetPoints(tournamentPointsGroupResultEntity.getEventNetPoints())
                                .setRank(tournamentPointsGroupResultEntity.getEventRank())
                        );
                    }
                    list.add(tournamentGroupData);
                });
        return new TableData<>(list);
    }

    @Override
    public TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId) {
        List<TournamentGroupData> list = Lists.newArrayList();
        this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .eq(TournamentGroupEntity::getGroupId, groupId)
                .orderByAsc(TournamentGroupEntity::getGroupRank)
                .orderByAsc(TournamentGroupEntity::getGroupIndex))
                .forEach(o -> {
                    TournamentGroupData tournamentGroupData = new TournamentGroupData();
                    BeanUtil.copyProperties(o, tournamentGroupData, CopyOptions.create().ignoreNullValue());
                    TournamentInfoEntity tournamentInfoEntity = this.tournamentInfoService.getOne(new QueryWrapper<TournamentInfoEntity>().lambda()
                            .eq(TournamentInfoEntity::getId, tournamentId)
                            .eq(TournamentInfoEntity::getState, 1));
                    if (tournamentInfoEntity != null) {
                        tournamentGroupData.setGroupMode(tournamentInfoEntity.getGroupMode());
                    }
                    tournamentGroupData
                            .setStartGw(o.getStartGw())
                            .setEndGw(o.getEndGw());
                    if (o.getEntry() < 0) {
                        tournamentGroupData
                                .setEntryName("平均分")
                                .setPlayerName("平均分");
                    } else {
                        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(o.getEntry());
                        if (entryInfoEntity != null) {
                            tournamentGroupData
                                    .setEntryName(entryInfoEntity.getEntryName())
                                    .setPlayerName(entryInfoEntity.getPlayerName());
                        }
                    }
                    list.add(tournamentGroupData);
                });
        return new TableData<>(list);
    }

    @Override
    public TableData<TournamentPointsGroupEventResultData> qryPagePointsGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
        List<TournamentPointsGroupEventResultData> list = Lists.newArrayList();
        Page<TournamentPointsGroupResultEntity> pointsGroupResultPage = this.tournamentPointsGroupResultService.getBaseMapper().selectPage(
                new Page<>(page, limit, true), new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                        .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                        .eq(TournamentPointsGroupResultEntity::getGroupId, groupId)
                        .eq(TournamentPointsGroupResultEntity::getEntry, entry)
        );
        pointsGroupResultPage.getRecords().forEach(o ->
                list.add(new TournamentPointsGroupEventResultData()
                        .setTournamentId(tournamentId)
                        .setGroupId(groupId)
                        .setEvent(o.getEvent())
                        .setEntry(entry)
                        .setGroupRank(o.getEventGroupRank())
                        .setPoints(o.getEventPoints())
                        .setCost(o.getEventCost())
                        .setNetPoints(o.getEventNetPoints())
                        .setRank(o.getEventRank())
                ));
        Page<TournamentPointsGroupEventResultData> pageResult = new Page<>(page, limit, pointsGroupResultPage.getTotal());
        pageResult.setRecords(list);
        return new TableData<>(pageResult);
    }

    @Override
    public TableData<TournamentBattleGroupEventResultData> qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
        List<TournamentBattleGroupEventResultData> list = Lists.newArrayList();
        Page<TournamentBattleGroupResultEntity> battleGroupResultPage = this.tournamentBattleGroupResultService.getBaseMapper().selectPage(
                new Page<>(page, limit, true), new QueryWrapper<TournamentBattleGroupResultEntity>().lambda()
                        .eq(TournamentBattleGroupResultEntity::getTournamentId, tournamentId)
                        .eq(TournamentBattleGroupResultEntity::getGroupId, groupId)
                        .and(o -> o.eq(TournamentBattleGroupResultEntity::getHomeEntry, entry)
                                .or(i -> i.eq(TournamentBattleGroupResultEntity::getAwayEntry, entry)))
        );
        battleGroupResultPage.getRecords().forEach(o ->
                list.add(new TournamentBattleGroupEventResultData()
                        .setTournamentId(tournamentId)
                        .setGroupId(groupId)
                        .setEvent(o.getEvent())
                        .setHomeEntry(o.getHomeEntry())
                        .setHomeEntryName(this.setBattleGroupEntryName(o.getHomeEntry()))
                        .setHomeEntryNetPoints(o.getHomeEntryNetPoints())
                        .setHomeEntryRank(o.getHomeEntryRank())
                        .setAwayEntry(o.getAwayEntry())
                        .setAwayEntryName(this.setBattleGroupEntryName(o.getAwayEntry()))
                        .setAwayEntryNetPoints(o.getAwayEntryNetPoints())
                        .setAwayEntryRank(o.getAwayEntryRank())
                        .setScore(o.getHomeEntryNetPoints() + "-" + o.getAwayEntryNetPoints())
                ));
        Page<TournamentBattleGroupEventResultData> pageResult = new Page<>(page, limit, battleGroupResultPage.getTotal());
        pageResult.setRecords(list);
        return new TableData<>(pageResult);
    }

    private String setBattleGroupEntryName(int entry) {
        if (entry < 0) {
            return "平均分";
        } else if (entry == 0) {
            return "轮空";
        }
        EntryInfoEntity entryInfoEntity = this.querySerivce.qryEntryInfo(entry);
        if (entryInfoEntity == null) {
            return "";
        }
        return entryInfoEntity.getEntryName();
    }

    /**
     * @apiNote live
     */
    @Override
    public TableData<LiveCalaData> qryEntryLivePoints(int entry) {
        int event = this.querySerivce.getCurrentEvent();
        LiveCalaData liveCalaData = this.liveService.calcLivePointsByEntry(event, entry);
        return new TableData<>(liveCalaData);
    }

    @Override
    public TableData<LiveCalaData> qryTournamentLivePoints(int tournamentId) {
        int event = this.querySerivce.getCurrentEvent();
        List<LiveCalaData> liveCalaList = this.liveService.calcLivePointsByTournament(event, tournamentId);
        return new TableData<>(liveCalaList);
    }

    @Override
    public TableData<ElementEventResultData> qryLiveFixturePlayerList(int teamId) {
        List<ElementEventResultData> list = Lists.newArrayList();
        // prepare
        Map<Integer, String> playerMap = this.getPlayerMap();
        Map<Integer, String> positionMap = this.getPositionMap();
        Map<Integer, Integer> liveBonusMap = this.getLiveBonusMap(teamId);
        // live fixture
        int event = this.querySerivce.getCurrentEvent();
        this.querySerivce.getEventLiveByEvent(event).values().forEach(o -> {
            if (o.getTeamId() != teamId || o.getMinutes() <= 0) {
                return;
            }
            ElementEventResultData elementEventResultData = new ElementEventResultData();
            elementEventResultData
                    .setEvent(o.getEvent())
                    .setElement(o.getElement())
                    .setWebName(playerMap.getOrDefault(o.getElement(), ""))
                    .setElementType(o.getElementType())
                    .setElementTypeName(positionMap.getOrDefault(o.getElementType(), ""))
                    .setMinutes(o.getMinutes())
                    .setGoalsScored(o.getGoalsScored())
                    .setAssists(o.getAssists())
                    .setGoalsConceded(o.getGoalsConceded())
                    .setOwnGoals(o.getOwnGoals())
                    .setPenaltiesSaved(o.getPenaltiesSaved())
                    .setBps(o.getAssists())
                    .setBonus(liveBonusMap.getOrDefault(o.getElement(), 0))
                    .setTotalPoints(o.getTotalPoints());
            elementEventResultData
                    .setTotalPoints(elementEventResultData.getTotalPoints() + elementEventResultData.getBonus());
            list.add(elementEventResultData);
        });
        return new TableData<>(list
                .stream()
                .sorted(Comparator.comparing(ElementEventResultData::getTotalPoints).reversed())
                .collect(Collectors.toList()));
    }

    private Map<Integer, String> getPlayerMap() {
        return this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
    }

    private Map<Integer, String> getPositionMap() {
        Map<Integer, String> map = Maps.newHashMap();
        this.querySerivce.getPositionMap().forEach((k, v) -> map.put(Integer.valueOf(k), v));
        return map;
    }

    private Map<Integer, Integer> getLiveBonusMap(int teamId) {
        Map<Integer, Integer> map = Maps.newHashMap();
        this.querySerivce.getLiveBonusCacheMap().forEach((k, v) -> {
            if (!StringUtils.equals(k, String.valueOf(teamId))) {
                return;
            }
            int element = v.keySet()
                    .stream()
                    .mapToInt(Integer::parseInt)
                    .findFirst()
                    .orElse(0);
            int bonus = v.values()
                    .stream()
                    .findFirst()
                    .orElse(0);
            map.put(element, bonus);
        });
        return map;
    }

    /**
     * @apiNote entry_result
     */
    @Cacheable(value = "qryEntryEventResult", key = "#event+'::'+#entry", unless = "#result == null")
    @Override
    public TableData<EntryPickData> qryEntryEventResult(int event, int entry) {
        if (event == 0 || entry == 0) {
            return new TableData<>();
        }
        String picks = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEvent, event)
                .eq(EntryEventResultEntity::getEntry, entry))
                .getEventPicks();
        if (StringUtils.isEmpty(picks)) {
            return new TableData<>();
        }
        List<EntryPickData> list = this.querySerivce.qryPickListFromPicks(picks)
                .stream()
                .sorted(Comparator.comparing(EntryPickData::getPosition))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return new TableData<>();
        }
        return new TableData<>(list);
    }

    @Cacheable(value = "qryEntryResultList", key = "#entry", unless = "#result==null")
    @Override
    public TableData<EntryEventResultData> qryEntryResultList(int entry) {
        if (entry <= 0) {
            return new TableData<>();
        }
        List<EntryEventResultData> list = Lists.newArrayList();
        this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry)
                .orderByAsc(EntryEventResultEntity::getEvent))
                .forEach(o -> list.add(new EntryEventResultData()
                        .setEntry(o.getEntry())
                        .setEvent(o.getEvent())
                        .setPoints(o.getEventPoints())
                        .setTransfers(o.getEventTransfers())
                        .setTransfersCost(o.getEventTransfersCost())
                        .setNetPoints(o.getEventNetPoints())
                        .setBenchPoints(o.getEventBenchPoints())
                        .setRank(o.getEventRank())
                        .setChip(o.getEventChip())
                ));
        return new TableData<>(list);
    }

    @Cacheable(value = "qryElementEventResult", key = "#event+'::'+#element", unless = "#result==null")
    @Override
    public TableData<ElementEventResultData> qryElementEventResult(int event, int element) {
        EventLiveEntity eventLiveEntity = this.eventLiveService.getOne(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event)
                .eq(EventLiveEntity::getElement, element));
        if (eventLiveEntity == null) {
            return new TableData<>();
        }
        return new TableData<>(BeanUtil.copyProperties(eventLiveEntity, ElementEventResultData.class));
    }

    /**
     * @apiNote report
     */
    @Cacheable(value = "qryTeamSelectStatByName", key = "#leagueName+'::'+#event", unless = "#result==null")
    @Override
    public TableData<LeagueStatData> qryTeamSelectStatByName(String leagueName, int event) {
        LeagueStatData leagueStatData = new LeagueStatData().setName(leagueName).setEvent(event);
        // player info
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        // team select
        List<TeamSelectStatEntity> teamSelectList = this.teamSelectStatService.list(new QueryWrapper<TeamSelectStatEntity>().lambda()
                .eq(TeamSelectStatEntity::getLeagueName, leagueName)
                .eq(TeamSelectStatEntity::getEvent, event));
        int teamSize = teamSelectList.size();
        if (CollectionUtils.isEmpty(teamSelectList)) {
            return new TableData<>(leagueStatData);
        }
        // most transfer in
        LinkedHashMap<String, String> mostTransferInMap = this.getMostTransferInMap(leagueName, event, teamSelectList, teamSize, playerMap);
        leagueStatData.setMostTransferIn(mostTransferInMap);
        // most transfer out
        LinkedHashMap<String, String> mostTransferOutMap = this.getMostTransferOutMap(leagueName, event, teamSelectList, teamSize, playerMap);
        leagueStatData.setMostTransferOut(mostTransferOutMap);
        // captain selected
        LinkedHashMap<String, String> captainSelectedMap = this.getCaptainSelectedMap(teamSelectList, teamSize, playerMap);
        leagueStatData.setCaptainSelectedMap(captainSelectedMap);
        // vice captain selected
        LinkedHashMap<String, String> viceCaptainSelectedMap = this.getViceCaptainSelectedMap(teamSelectList, teamSize, playerMap);
        leagueStatData.setViceCaptainSelectedMap(viceCaptainSelectedMap);
        // top selected player
        LinkedHashMap<String, String> topSelectedPlayerMap = this.getTopSelectedPlayerMap(teamSelectList, teamSize, playerMap);
        leagueStatData.setTopSelectedPlayerMap(topSelectedPlayerMap);
        // top selected team
        LinkedHashMap<Integer, Map<String, String>> topSelectedTeamMap = this.getTopSelectedTeamMap(teamSelectList, teamSize, playerMap);
        leagueStatData.setTopSelectedTeamMap(topSelectedTeamMap);
        return new TableData<>(leagueStatData);
    }

    private LinkedHashMap<String, String> getMostTransferInMap(String leagueName, int event, List<TeamSelectStatEntity> teamSelectList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        if (event <= 1) {
            return Maps.newLinkedHashMap();
        }
        // current gw
        Map<Integer, List<Integer>> currentSelectMap = this.collectEntrySelectedMap(teamSelectList);
        // previous gw
        Map<Integer, List<Integer>> previousSelectMap = this.collectPreviousEntrySelectedMap(leagueName, event);
        // different
        List<Integer> elementList = Lists.newArrayList();
        currentSelectMap.keySet().forEach(entry -> {
            List<Integer> currentList = currentSelectMap.get(entry);
            List<Integer> previousList = previousSelectMap.getOrDefault(entry, Lists.newArrayList());
            currentList
                    .stream()
                    .filter(o -> !previousList.contains(o))
                    .forEach(elementList::add);
        });
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private LinkedHashMap<String, String> getMostTransferOutMap(String leagueName, int event, List<TeamSelectStatEntity> teamSelectList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        if (event <= 1) {
            return Maps.newLinkedHashMap();
        }
        // current gw
        Map<Integer, List<Integer>> currentSelectMap = this.collectEntrySelectedMap(teamSelectList);
        // previous gw
        Map<Integer, List<Integer>> previousSelectMap = this.collectPreviousEntrySelectedMap(leagueName, event);
        // different
        List<Integer> elementList = Lists.newArrayList();
        previousSelectMap.keySet().forEach(entry -> {
            List<Integer> previousList = previousSelectMap.get(entry);
            List<Integer> currentList = currentSelectMap.getOrDefault(entry, Lists.newArrayList());
            previousList
                    .stream()
                    .filter(o -> !currentList.contains(o))
                    .forEach(elementList::add);
        });
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private Map<Integer, List<Integer>> collectPreviousEntrySelectedMap(String leagueName, int event) {
        List<TeamSelectStatEntity> previousSelectList = this.teamSelectStatService.list(new QueryWrapper<TeamSelectStatEntity>().lambda()
                .eq(TeamSelectStatEntity::getLeagueName, leagueName)
                .eq(TeamSelectStatEntity::getEvent, event - 1));
        return this.collectEntrySelectedMap(previousSelectList);
    }

    private Map<Integer, List<Integer>> collectEntrySelectedMap(List<TeamSelectStatEntity> teamSelectList) {
        Map<Integer, List<Integer>> teamSelectMap = Maps.newHashMap();
        teamSelectList.forEach(o -> {
            List<Integer> elementList = Lists.newArrayList(
                    o.getPosition1(), o.getPosition2(), o.getPosition3(), o.getPosition4(), o.getPosition5(),
                    o.getPosition6(), o.getPosition7(), o.getPosition8(), o.getPosition9(), o.getPosition10(),
                    o.getPosition11(), o.getPosition12(), o.getPosition13(), o.getPosition14(), o.getPosition15()
            );
            teamSelectMap.put(o.getEntry(), elementList);
        });
        return teamSelectMap;
    }

    private LinkedHashMap<String, String> getCaptainSelectedMap(List<TeamSelectStatEntity> teamSelectList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        List<Integer> elementList = teamSelectList
                .stream()
                .map(TeamSelectStatEntity::getCaptain)
                .collect(Collectors.toList());
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private LinkedHashMap<String, String> getViceCaptainSelectedMap(List<TeamSelectStatEntity> teamSelectList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        // collect
        List<Integer> elementList = teamSelectList
                .stream()
                .map(TeamSelectStatEntity::getViceCaptain)
                .collect(Collectors.toList());
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private LinkedHashMap<String, String> getTopSelectedPlayerMap(List<TeamSelectStatEntity> teamSelectList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        List<Integer> elementList = Lists.newArrayList();
        teamSelectList.forEach(o -> {
            elementList.add(o.getPosition1());
            elementList.add(o.getPosition2());
            elementList.add(o.getPosition3());
            elementList.add(o.getPosition4());
            elementList.add(o.getPosition5());
            elementList.add(o.getPosition6());
            elementList.add(o.getPosition7());
            elementList.add(o.getPosition8());
            elementList.add(o.getPosition9());
            elementList.add(o.getPosition10());
            elementList.add(o.getPosition11());
            elementList.add(o.getPosition12());
            elementList.add(o.getPosition13());
            elementList.add(o.getPosition14());
            elementList.add(o.getPosition15());
        });
        return this.collectSelectedMap(elementList, teamSize, 20, playerMap);
    }

    private LinkedHashMap<Integer, Map<String, String>> getTopSelectedTeamMap(List<TeamSelectStatEntity> teamSelectList, int teamSize, Map<Integer, PlayerEntity> playerMap) {
        // element list
        List<PlayerEntity> elementPlayerInfoList = Lists.newArrayList();
        teamSelectList.forEach(o -> {
            elementPlayerInfoList.add(playerMap.get(o.getPosition1()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition2()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition3()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition4()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition5()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition6()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition7()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition8()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition9()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition10()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition11()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition12()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition13()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition14()));
            elementPlayerInfoList.add(playerMap.get(o.getPosition15()));
        });
        // collect
        Map<Integer, Map<Integer, Long>> elementTypeCountMap = elementPlayerInfoList
                .stream()
                .collect(Collectors.groupingBy(PlayerEntity::getElementType, Collectors.groupingBy(PlayerEntity::getElement, Collectors.counting())));
        // sort by element type
        Map<Integer, Integer> playerSelectedMap = Maps.newHashMap(); // key:element -> value: count
        elementTypeCountMap.keySet().forEach(elementType -> {
            Map<Integer, Integer> result = elementTypeCountMap.get(elementType).entrySet()
                    .stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .limit(this.getLimitByElementType(elementType))
                    .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().intValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
            result.forEach(playerSelectedMap::put);
        });
        // add key:element_type
        Map<Integer, Map<Integer, Integer>> elementTypeMap = this.collectPlayerSelectedMap(playerSelectedMap, playerMap); // key:element_type -> value: elementCOuntMap
        // sort by selected
        LinkedHashMap<Integer, Integer> elementSelectedSortMap = playerSelectedMap.entrySet() // key:element -> value: count (sort by count)
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));
        List<PlayerEntity> elementList = Lists.newArrayList();
        elementSelectedSortMap.forEach((k, v) -> elementList.add(playerMap.get(k)));
        // selected line up
        LinkedHashMap<Integer, Map<String, String>> map = Maps.newLinkedHashMap(); // key:element_type -> value:elementCountMap(key:element -> value:percent)
        LinkedHashMap<Integer, Integer> lineupMap = this.getLineupMapByElementList(elementTypeMap, elementList); // key:position -> value:element
        lineupMap.forEach((position, element) -> {
            long count = playerSelectedMap.get(element);
            PlayerEntity playerEntity = playerMap.get(element);
            int elementType = playerEntity.getElementType();
            Map<String, String> valueMap = Maps.newHashMap();
            if (map.containsKey(elementType)) {
                valueMap = map.get(elementType);
            }
            valueMap.put(playerEntity.getWebName(), NumberUtil.decimalFormat("#.##%", NumberUtil.div(count, teamSize)));
            map.put(elementType, valueMap);
        });
        return map;
    }

    private Map<Integer, Map<Integer, Integer>> collectPlayerSelectedMap(Map<Integer, Integer> playerSelectedMap, Map<Integer, PlayerEntity> playerMap) {
        Map<Integer, Map<Integer, Integer>> map = Maps.newHashMap();
        playerSelectedMap.forEach((k, v) -> {
            int elementType = playerMap.get(k).getElementType();
            Map<Integer, Integer> valueMap = Maps.newHashMap();
            if (map.containsKey(elementType)) {
                valueMap = map.get(elementType);
            }
            valueMap.put(k, v);
            map.put(elementType, valueMap);
        });
        return map;
    }

    private LinkedHashMap<Integer, Integer> getLineupMapByElementList(Map<Integer, Map<Integer, Integer>> elementTypeMap, List<PlayerEntity> elementList) {
        // gkp
        List<Integer> gkpList = Lists.newArrayList();
        elementTypeMap.get(1).entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .forEachOrdered(o -> gkpList.add(o.getKey()));
        // def
        List<Integer> defList = Lists.newArrayList();
        elementTypeMap.get(2).entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .forEachOrdered(o -> defList.add(o.getKey()));
        // mid
        List<Integer> midList = Lists.newArrayList();
        elementTypeMap.get(3).entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .forEachOrdered(o -> midList.add(o.getKey()));
        // fwd
        List<Integer> fwdList = Lists.newArrayList();
        elementTypeMap.get(4).entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .forEachOrdered(o -> fwdList.add(o.getKey()));
        // linue up
        Map<String, Integer> formationMap = this.getFormationMap(elementList);
        List<Integer> positionList = Lists.newArrayList();
        // gkp line up
        positionList.add(gkpList.get(0));
        // def line up
        int defStartIndex = 2;
        int defEndIndex = defStartIndex + formationMap.get("def");
        IntStream.range(defStartIndex, defEndIndex).forEach(index -> positionList.add(defList.get(index - defStartIndex)));
        // mid line up
        int midEndIndex = defEndIndex + formationMap.get("mid");
        IntStream.range(defEndIndex, midEndIndex).forEach(index -> positionList.add(midList.get(index - defEndIndex)));
        // fwd line up
        int fwdEndIndex = midEndIndex + formationMap.get("fwd");
        IntStream.range(midEndIndex, fwdEndIndex).forEach(index -> positionList.add(fwdList.get(index - midEndIndex)));
        // return
        LinkedHashMap<Integer, Integer> map = Maps.newLinkedHashMap();
        for (int i = 0; i < positionList.size(); i++) {
            map.put(i + 1, positionList.get(i));
        }
        return map;
    }

    private Map<String, Integer> getFormationMap(List<PlayerEntity> elementList) {
        int def = 0;
        int mid = 0;
        int fwd = 0;
        List<PlayerEntity> standbyList = Lists.newArrayList();
        for (PlayerEntity playerEntity : elementList) {
            int elementType = playerEntity.getElementType();
            switch (elementType) {
                case 2: {
                    if (def < 3) {
                        def++;
                        break;
                    }
                }
                case 4: {
                    if (fwd < 1) {
                        fwd++;
                        break;
                    }
                }
                default:
                    standbyList.add(playerEntity);
            }
        }
        for (PlayerEntity playerEntity : standbyList) {
            if (def + mid + fwd >= 10) {
                break;
            }
            int elementType = playerEntity.getElementType();
            switch (elementType) {
                case 2: {
                    def++;
                    break;
                }
                case 3: {
                    mid++;
                    break;
                }
                case 4: {
                    fwd++;
                }
            }
        }
        Map<String, Integer> map = Maps.newHashMap();
        map.put("def", def);
        map.put("mid", mid);
        map.put("fwd", fwd);
        return map;
    }

    private int getLimitByElementType(int elementType) {
        switch (elementType) {
            case 1:
                return 2;
            case 2:
            case 3:
                return 5;
            case 4:
                return 3;
        }
        return 0;
    }

    private LinkedHashMap<String, String> collectSelectedMap(List<Integer> elementList, int teamSize, int limit, Map<Integer, PlayerEntity> playerMap) {
        LinkedHashMap<String, String> map = Maps.newLinkedHashMap();
        Map<Integer, Long> groupingMap = elementList
                .stream()
                .collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()));
        Map<Integer, Integer> result = groupingMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().intValue(), (oldVal, newVal) -> oldVal, LinkedHashMap::new));
        result.forEach((k, v) ->
                map.put(playerMap.get(k).getWebName(), NumberUtil.decimalFormat("#.##%", NumberUtil.div(v.intValue(), teamSize))));
        return map;
    }

    private boolean setSearchTotal(long current) {
        return current == 1;
    }


    @Override
    public TableData<EntryEventCaptainData> qryEntryCaptainList(String season, int entry) {
        return new TableData<>();
    }


}

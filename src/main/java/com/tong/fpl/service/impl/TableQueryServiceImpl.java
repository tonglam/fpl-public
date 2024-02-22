package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.*;
import com.tong.fpl.config.collector.PlayerValueCollector;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.*;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.*;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.domain.letletme.player.*;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.ILiveService;
import com.tong.fpl.service.IQueryService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/8/28
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TableQueryServiceImpl implements ITableQueryService {

    private final IInterfaceService interfaceService;
    private final IQueryService queryService;
    private final ILiveService liveService;

    private final PlayerService playerService;
    private final PlayerStatService playerStatService;
    private final PlayerValueService playerValueService;
    private final EventLiveService eventLiveService;
    private final EntryInfoService entryInfoService;
    private final EntryEventCupResultService entryEventCupResultService;
    private final EntryEventResultService entryEventResultService;
    private final EntryEventTransfersService entryEventTransferService;
    private final TournamentInfoService tournamentInfoService;
    private final TournamentGroupService tournamentGroupService;
    private final TournamentPointsGroupResultService tournamentPointsGroupResultService;
    private final TournamentBattleGroupResultService tournamentBattleGroupResultService;
    private final TournamentKnockoutService tournamentKnockoutService;
    private final LeagueEventReportService leagueEventReportService;
    private final ScoutService scoutService;

    /**
     * @implNote player
     */
    @Override
    public TableData<PlayerInfoData> qryPlayerList(String season) {
        List<PlayerInfoData> list = this.queryService.qryAllPlayers(season);
        list = list.stream().sorted(Comparator.comparing(PlayerInfoData::getPrice).reversed()).collect(Collectors.toList());
        return new TableData<>(list);
    }

    @Cacheable(value = "qryPagePlayerDataList", key = "#page+'::'+#limit", unless = "#result == null")
    @Override
    public TableData<PlayerInfoData> qryPagePlayerDataList(int page, int limit) {
        List<PlayerInfoData> list = Lists.newArrayList();
        Page<PlayerEntity> playerPage = this.playerService.getBaseMapper().selectPage(
                new Page<>(page, limit, this.setSearchTotal(page)), new QueryWrapper<>());
        playerPage.getRecords().forEach(o ->
                list.add(BeanUtil.copyProperties(this.queryService.initPlayerInfo(CommonUtils.getCurrentSeason(), o), PlayerInfoData.class)));
        Page<PlayerInfoData> pageResult = new Page<>(page, limit, playerPage.getTotal());
        pageResult.setRecords(list);
        return new TableData<>(pageResult);
    }

    @Cacheable(value = "qryPriceChangeList")
    @Override
    public TableData<PlayerValueData> qryPriceChangeList() {
        // prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<Integer, String> teamNameMap = Maps.newHashMap();
        this.queryService.getTeamNameMap().forEach((k, v) -> teamNameMap.put(Integer.valueOf(k), v));
        Map<Integer, String> teamShortNameMap = Maps.newHashMap();
        this.queryService.getTeamShortNameMap().forEach((k, v) -> teamShortNameMap.put(Integer.valueOf(k), v));
        // player value
        List<PlayerValueData> list = Lists.newArrayList();
        this.playerValueService.list().forEach(o -> {
            PlayerValueData playerValueData = new PlayerValueData();
            BeanUtil.copyProperties(o, playerValueData, CopyOptions.create().ignoreNullValue());
            PlayerEntity playerEntity = playerMap.get(String.valueOf(o.getElement()));
            if (playerEntity != null) {
                int teamId = playerEntity.getTeamId();
                playerValueData
                        .setWebName(playerEntity.getWebName())
                        .setTeamName(teamNameMap.getOrDefault(teamId, ""))
                        .setTeamShortName(teamShortNameMap.getOrDefault(teamId, ""))
                        .setElementTypeName(Position.getNameFromElementType(o.getElementType()));
            }
            list.add(playerValueData);
        });
        return new TableData<>(list);
    }

    @Cacheable(value = "qryPlayerShowListByElementType", key = "#elementType")
    @Override
    public TableData<PlayerShowData> qryPlayerShowListByElementType(int elementType) {
        List<PlayerEntity> playerEntityList = this.playerService.list(new QueryWrapper<PlayerEntity>().lambda()
                .eq(PlayerEntity::getElementType, elementType));
        if (CollectionUtils.isEmpty(playerEntityList)) {
            return new TableData<>();
        }
        int event = this.queryService.getCurrentEvent();
        List<Integer> elementList = playerEntityList
                .stream()
                .map(PlayerEntity::getElement)
                .collect(Collectors.toList());
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<String, PlayerEntity> playerMap = playerEntityList
                .stream()
                .collect(Collectors.toMap(k -> String.valueOf(k.getElement()), v -> v));
        Map<Integer, PlayerStatEntity> playerStatMap = Maps.newHashMap();
        this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
                .in(PlayerStatEntity::getElement, elementList))
                .forEach(o -> {
                    int element = o.getElement();
                    if (playerStatMap.containsKey(element) && o.getEvent() <= playerStatMap.get(element).getEvent()) {
                        return;
                    }
                    playerStatMap.put(element, o);
                });
        Multimap<Integer, EventLiveEntity> eventLiveMap = HashMultimap.create();
        this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .in(EventLiveEntity::getElement, elementList))
                .forEach(o -> eventLiveMap.put(o.getElement(), o));
        Map<Integer, Map<String, List<PlayerFixtureData>>> teamFixtureMap = Maps.newHashMap(); // teamId -> event -> fixtures
        IntStream.rangeClosed(1, 20).forEach(teamId -> teamFixtureMap.put(teamId, this.queryService.getEventFixtureByTeamId(teamId)));
        // collect
        List<CompletableFuture<PlayerShowData>> future = playerEntityList
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() ->
                        this.queryService.qryPlayerShowData(event, o.getElement(), teamNameMap, teamShortNameMap, playerMap, playerStatMap, eventLiveMap, teamFixtureMap)))
                .collect(Collectors.toList());
        List<PlayerShowData> list = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        return new TableData<>(list
                .stream()
                .sorted(Comparator.comparing(PlayerShowData::getTotalPoints).reversed())
                .collect(Collectors.toList())
        );
    }

    // do not cache
    @Override
    public TableData<PlayerShowData> qryEntryEventPlayerShowList(int event, int entry, int operator) {
        String picks = this.queryService.qryEntryEventPicks(event, entry, operator);
        Map<Integer, EntryPickData> pickMap = this.queryService.qryPickListFromPicks(picks)
                .stream()
                .collect(Collectors.toMap(EntryPickData::getElement, o -> o));
        if (CollectionUtils.isEmpty(pickMap)) {
            return new TableData<>();
        }
        List<Integer> elementList = pickMap.values()
                .stream()
                .map(EntryPickData::getElement)
                .collect(Collectors.toList());
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<Integer, PlayerStatEntity> playerStatMap = Maps.newHashMap();
        this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
                .in(PlayerStatEntity::getElement, elementList))
                .forEach(o -> {
                    int element = o.getElement();
                    if (playerStatMap.containsKey(element) && o.getEvent() <= playerStatMap.get(element).getEvent()) {
                        return;
                    }
                    playerStatMap.put(element, o);
                });
        Multimap<Integer, EventLiveEntity> eventLiveMap = HashMultimap.create();
        this.eventLiveService.list().forEach(o -> eventLiveMap.put(o.getElement(), o));
        Map<Integer, Map<String, List<PlayerFixtureData>>> teamFixtureMap = this.queryService.getTeamEventFixtureMap();
        // collect
        List<CompletableFuture<PlayerShowData>> future = pickMap.keySet()
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() ->
                        this.queryService.qryPlayerShowData(event, o, teamNameMap, teamShortNameMap, playerMap, playerStatMap, eventLiveMap, teamFixtureMap)))
                .collect(Collectors.toList());
        List<PlayerShowData> playerShowDataList = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        playerShowDataList.forEach(data -> {
            EntryPickData entryPickData = pickMap.get(data.getElement());
            if (entryPickData == null) {
                return;
            }
            data
                    .setPosition(entryPickData.getPosition())
                    .setMultiplier(entryPickData.getMultiplier())
                    .setCaptain(entryPickData.isCaptain())
                    .setViceCaptain(entryPickData.isViceCaptain());
        });
        List<PlayerShowData> list = Lists.newArrayList();
        list.addAll(playerShowDataList
                .stream()
                .filter(o -> o.getPosition() < 12)
                .sorted(Comparator.comparing(PlayerShowData::getElementType).reversed()
                        .thenComparing(PlayerShowData::getPosition))
                .collect(Collectors.toList()));
        list.addAll(playerShowDataList
                .stream()
                .filter(o -> o.getPosition() > 11)
                .sorted(Comparator.comparing(PlayerShowData::getPosition))
                .collect(Collectors.toList()));
        return new TableData<>(list);
    }

    // do not cache
    @Override
    public TableData<PlayerShowData> qrySortedEntryEventPlayerShowList(List<PlayerShowData> playerShowDataList) {
        List<PlayerShowData> list = Lists.newArrayList();
        list.addAll(playerShowDataList
                .stream()
                .filter(o -> o.getPosition() < 12)
                .sorted(Comparator.comparing(PlayerShowData::getElementType).reversed()
                        .thenComparing(PlayerShowData::getPosition))
                .collect(Collectors.toList()));
        list.addAll(playerShowDataList
                .stream()
                .filter(o -> o.getPosition() > 11)
                .sorted(Comparator.comparing(PlayerShowData::getPosition))
                .collect(Collectors.toList()));
        return new TableData<>(list);
    }

    @Cacheable(value = "qryEntryEventPlayerShowListForTransfers", key = "#event+'::'+#entry")
    @Override
    public TableData<PlayerShowData> qryEntryEventPlayerShowListForTransfers(int event, int entry) {
        EntryEventResultEntity entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry)
                .eq(EntryEventResultEntity::getEvent, event));
        if (entryEventResultEntity == null) {
            return new TableData<>();
        }
        if (StringUtils.equals(entryEventResultEntity.getEventChip(), Chip.FH.getValue())) {
            entryEventResultEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                    .eq(EntryEventResultEntity::getEntry, entry)
                    .eq(EntryEventResultEntity::getEvent, event - 1));
        }
        Map<Integer, EntryPickData> pickMap = this.queryService.qryPickListFromPicks(entryEventResultEntity.getEventPicks())
                .stream()
                .collect(Collectors.toMap(EntryPickData::getElement, o -> o));
        if (CollectionUtils.isEmpty(pickMap)) {
            return new TableData<>();
        }
        List<Integer> elementList = pickMap.values()
                .stream()
                .map(EntryPickData::getElement)
                .collect(Collectors.toList());
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<Integer, PlayerStatEntity> playerStatMap = Maps.newHashMap();
        this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
                .in(PlayerStatEntity::getElement, elementList))
                .forEach(o -> {
                    int element = o.getElement();
                    if (playerStatMap.containsKey(element) && o.getEvent() <= playerStatMap.get(element).getEvent()) {
                        return;
                    }
                    playerStatMap.put(element, o);
                });
        Multimap<Integer, EventLiveEntity> eventLiveMap = HashMultimap.create();
        this.eventLiveService.list().forEach(o -> eventLiveMap.put(o.getElement(), o));
        Map<Integer, Map<String, List<PlayerFixtureData>>> teamFixtureMap = Maps.newHashMap(); // teamId -> event -> fixtures
        IntStream.rangeClosed(1, 20).forEach(teamId -> teamFixtureMap.put(teamId, this.queryService.getEventFixtureByTeamId(teamId)));
        Map<Integer, Integer> startPriceMap = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
                .eq(PlayerValueEntity::getChangeType, ValueChangeType.Start.name()))
                .stream()
                .collect(Collectors.toMap(PlayerValueEntity::getElement, PlayerValueEntity::getValue));
        Map<Integer, Integer> transferInPriceMap = this.entryEventTransferService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                .eq(EntryEventTransfersEntity::getEntry, entry)
                .orderByAsc(EntryEventTransfersEntity::getEvent))
                .stream()
                .collect(Collectors.toMap(EntryEventTransfersEntity::getElementIn, EntryEventTransfersEntity::getElementInCost, (oldValue, newValue) -> newValue));
        Map<Integer, Integer> elementCurrentPriceMap = this.playerValueService.list()
                .stream()
                .collect(new PlayerValueCollector())
                .values()
                .stream()
                .collect(Collectors.toMap(PlayerValueEntity::getElement, PlayerValueEntity::getValue));
        // collect
        List<CompletableFuture<PlayerShowData>> future = pickMap.keySet()
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() ->
                        this.queryService.qryPlayerShowData(event, o, teamNameMap, teamShortNameMap, playerMap, playerStatMap, eventLiveMap, teamFixtureMap)))
                .collect(Collectors.toList());
        List<PlayerShowData> list = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        list.forEach(data -> {
            EntryPickData entryPickData = pickMap.get(data.getElement());
            if (entryPickData == null) {
                return;
            }
            data
                    .setSellPrice(this.calcElementSellPrice(data.getElement(), startPriceMap, transferInPriceMap, elementCurrentPriceMap) / 10.0)
                    .setPosition(entryPickData.getPosition())
                    .setMultiplier(entryPickData.getMultiplier())
                    .setCaptain(entryPickData.isCaptain())
                    .setViceCaptain(entryPickData.isViceCaptain());
        });
        return new TableData<>(
                list
                        .stream()
                        .sorted(Comparator.comparing(PlayerShowData::getElementType).reversed()
                                .thenComparing(PlayerShowData::getPosition))
                        .collect(Collectors.toList())
        );
    }

    private int calcElementSellPrice(int element, Map<Integer, Integer> startPriceMap, Map<Integer, Integer> transferElementPriceMap, Map<Integer, Integer> elementCurrentPriceMap) {
        int boughtPrice = transferElementPriceMap.containsKey(element) ? transferElementPriceMap.get(element) : startPriceMap.get(element);
        int currentPrice = elementCurrentPriceMap.containsKey(element) ? elementCurrentPriceMap.get(element) : startPriceMap.get(element);
        if (boughtPrice >= currentPrice) {
            return currentPrice;
        }
        return boughtPrice + (int) Math.floor((currentPrice - boughtPrice) * 1.0 / 2);
    }

    @Override
    public TableData<PlayerShowData> qryPlayerShowListByElementForTransfers(List<EntryPickData> pickList) {
        if (CollectionUtils.isEmpty(pickList)) {
            return new TableData<>();
        }
        int event = this.queryService.getCurrentEvent();
        Map<Integer, EntryPickData> pickMap = pickList
                .stream()
                .collect(Collectors.toMap(EntryPickData::getElement, o -> o));
        if (CollectionUtils.isEmpty(pickMap)) {
            return new TableData<>();
        }
        List<Integer> elementList = pickList
                .stream()
                .map(EntryPickData::getElement)
                .collect(Collectors.toList());
        List<Integer> transfersList = pickList
                .stream()
                .filter(EntryPickData::isEventTransferIn)
                .map(EntryPickData::getElement)
                .collect(Collectors.toList());
        // prepare
        Map<String, String> teamNameMap = this.queryService.getTeamNameMap();
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<Integer, PlayerStatEntity> playerStatMap = Maps.newHashMap();
        this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
                .in(PlayerStatEntity::getElement, elementList))
                .forEach(o -> {
                    int element = o.getElement();
                    if (playerStatMap.containsKey(element) && o.getEvent() <= playerStatMap.get(element).getEvent()) {
                        return;
                    }
                    playerStatMap.put(element, o);
                });
        Multimap<Integer, EventLiveEntity> eventLiveMap = HashMultimap.create();
        this.eventLiveService.list().forEach(o -> eventLiveMap.put(o.getElement(), o));
        Map<Integer, Map<String, List<PlayerFixtureData>>> teamFixtureMap = Maps.newHashMap(); // teamId -> event -> fixtures
        IntStream.rangeClosed(1, 20).forEach(teamId -> teamFixtureMap.put(teamId, this.queryService.getEventFixtureByTeamId(teamId)));
        IntStream.rangeClosed(1, 20).forEach(teamId -> teamFixtureMap.put(teamId, this.queryService.getEventFixtureByTeamId(teamId)));
        // collect
        List<CompletableFuture<PlayerShowData>> future = pickMap.keySet()
                .stream()
                .map(o -> CompletableFuture.supplyAsync(() ->
                        this.queryService.qryPlayerShowData(event, o, teamNameMap, teamShortNameMap, playerMap, playerStatMap, eventLiveMap, teamFixtureMap)))
                .collect(Collectors.toList());
        List<PlayerShowData> list = future
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        list.forEach(data -> {
            EntryPickData entryPickData = pickMap.get(data.getElement());
            if (entryPickData == null) {
                return;
            }
            data
                    .setSellPrice(entryPickData.getSellPrice() / 10.0)
                    .setPosition(entryPickData.getPosition())
                    .setMultiplier(entryPickData.getMultiplier())
                    .setCaptain(entryPickData.isCaptain())
                    .setViceCaptain(entryPickData.isViceCaptain());
            if (transfersList.contains(data.getElement())) {
                data.setEventTransferIn(true);
            }
        });
        return new TableData<>(
                list
                        .stream()
                        .sorted(Comparator.comparing(PlayerShowData::getElementType).reversed()
                                .thenComparing(PlayerShowData::getPosition))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public TableData<PlayerDetailData> qryPlayerDetailData(int element) {
        return new TableData<>(this.queryService.qryPlayerDetailData(element));
    }

    /**
     * @implNote league
     */
    @Override
    public TableData<EntryInfoData> qryLeagueEntryList(String url) {
        List<EntryInfoData> list = url.contains("/standings/c") ?
                this.interfaceService.getEntryInfoListFromClassic(CommonUtils.getLeagueId(url))
                : this.interfaceService.getEntryInfoListFromH2h(CommonUtils.getLeagueId(url));
        return new TableData<>(list);
    }

    /**
     * @implNote tournament
     */
    @Cacheable(value = "qryTournamentList", key = "#param.entry", unless = "#result.data.size() == 0")
    @Override
    public TableData<TournamentInfoData> qryTournamentList(TournamentQueryParam param) {
        List<TournamentInfoData> list = Lists.newArrayList();
        // get tournament info
        LambdaQueryWrapper<TournamentInfoEntity> queryWrapper = new QueryWrapper<TournamentInfoEntity>().lambda();
        List<Integer> tournamentIdList = this.queryService.qryEntryTournamentEntryList(param.getEntry());
        queryWrapper.in(TournamentInfoEntity::getId, tournamentIdList);
        if (StringUtils.isNotBlank(param.getName())) {
            queryWrapper.like(TournamentInfoEntity::getName, param.getName());
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
        if (queryWrapper.getExpression().getNormal().isEmpty()) {
            return new TableData<>();
        }
        queryWrapper
                .eq(TournamentInfoEntity::getState, 1)
                .orderByAsc(TournamentInfoEntity::getId);
        // return
        this.tournamentInfoService.list(queryWrapper).forEach(o -> {
            TournamentInfoData tournamentInfoData = new TournamentInfoData();
            BeanUtil.copyProperties(o, tournamentInfoData, CopyOptions.create().ignoreNullValue());
            tournamentInfoData
                    .setGroupMode(GroupMode.valueOf(o.getGroupMode()).getModeName())
                    .setGroupStartGw(CommonUtils.setRealGw(o.getGroupStartGw()))
                    .setGroupEndGw(CommonUtils.setRealGw(o.getGroupEndGw()))
                    .setKnockoutMode(KnockoutMode.valueOf(o.getKnockoutMode()).getModeName())
                    .setKnockoutStartGw(CommonUtils.setRealGw(o.getKnockoutStartGw()))
                    .setKnockoutEndGw(CommonUtils.setRealGw(o.getKnockoutEndGw()))
                    .setGroupFillAverage(o.getGroupFillAverage() ? "是" : "否")
                    .setCreateTime(StringUtils.substringBefore(o.getCreateTime(), " "));
            if (tournamentInfoData.getLeagueId() == 99999) {
                tournamentInfoData
                        .setLeagueId(tournamentInfoData.getId())
                        .setLeagueType("Tournament");
            }
            list.add(tournamentInfoData);
        });
        return new TableData<>(list);
    }

    @Cacheable(value = "qryEntryTournamentList", key = "#entry")
    @Override
    public TableData<TournamentEntryData> qryEntryTournamentList(int entry) {
        List<TournamentEntryData> list = Lists.newArrayList();
        if (entry == 0) {
            return new TableData<>();
        }
        int currentEvent = this.queryService.getCurrentEvent();
        // get tournament_list
        List<Integer> tournamentList = this.queryService.qryEntryTournamentEntryList(entry);
        if (CollectionUtils.isEmpty(tournamentList)) {
            return new TableData<>();
        }
        // stage_mode
        Map<String, GroupMode> groupModeMap = Arrays.stream(GroupMode.values()).collect(Collectors.toMap(Enum::name, v -> v));
        Map<String, KnockoutMode> knockModeMap = Arrays.stream(KnockoutMode.values()).collect(Collectors.toMap(Enum::name, v -> v));
        // return
        this.tournamentInfoService.list(new QueryWrapper<TournamentInfoEntity>().lambda()
                .in(TournamentInfoEntity::getId, tournamentList)
                .eq(TournamentInfoEntity::getState, 1))
                .forEach(o ->
                        list.add(
                                new TournamentEntryData()
                                        .setEntry(entry)
                                        .setTournamentId(o.getId())
                                        .setName(o.getName())
                                        .setCreator(o.getCreator())
                                        .setLeagueType(o.getLeagueType())
                                        .setLeagueId(o.getLeagueId())
                                        .setTournamentMode(o.getTournamentMode())
                                        .setGroupMode(groupModeMap.get(o.getGroupMode()).getModeName())
                                        .setKnockoutMode(knockModeMap.get(o.getKnockoutMode()).getModeName())
                                        .setStage(this.setCurrentStage(currentEvent, groupModeMap.get(o.getGroupMode()), o))
                                        .setCreateTime(StringUtils.substringBefore(o.getCreateTime(), " "))
                        ));
        return new TableData<>(list);
    }

    private String setCurrentStage(int currentEvent, GroupMode groupMode, TournamentInfoEntity tournamentInfoEntity) {
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
                if (groupStartGw > 0 && currentEvent >= groupStartGw) {
                    return "小组赛";
                } else if (knockoutStartGw > 0 && currentEvent >= knockoutStartGw) {
                    return "淘汰赛";
                } else if (knockoutEndGw > 0 && currentEvent > knockoutEndGw) {
                    return "已结束";
                }
                break;
            }
        }
        return "未开始";
    }

    @Cacheable(value = "qryGroupInfoListByGroupId", key = "#tournamentId+'::'+#groupId")
    @Override
    public TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId) {
        List<TournamentGroupData> list = Lists.newArrayList();
        // tournament_info
        TournamentInfoEntity tournamentInfoEntity = this.queryService.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return new TableData<>();
        }
        int groupNum = tournamentInfoEntity.getGroupNum();
        int current = this.queryService.getCurrentEvent();
        // tournament_group
        this.tournamentGroupService.list(new QueryWrapper<TournamentGroupEntity>().lambda()
                .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                .eq(TournamentGroupEntity::getGroupId, groupId)
                .orderByAsc(TournamentGroupEntity::getGroupRank)
                .orderByAsc(TournamentGroupEntity::getGroupIndex))
                .forEach(o -> {
                    int entry = o.getEntry();
                    TournamentGroupData tournamentGroupData = new TournamentGroupData()
                            .setEvent(current)
                            .setGroupMode(tournamentInfoEntity.getGroupMode());
                    BeanUtil.copyProperties(o, tournamentGroupData, CopyOptions.create().ignoreNullValue());
                    tournamentGroupData
                            .setStartGw(o.getStartGw())
                            .setEndGw(o.getEndGw());
                    if (entry < 0) {
                        tournamentGroupData
                                .setEntryName("平均分")
                                .setPlayerName("平均分");
                    } else {
                        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
                        if (entryInfoEntity != null) {
                            BeanUtil.copyProperties(entryInfoEntity, tournamentGroupData, CopyOptions.create().ignoreNullValue());
                        }
                    }
                    // group name
                    tournamentGroupData.setTournamentGroupNameMap(Maps.newHashMap());
                    // pk entry
                    TournamentKnockoutEntity tournamentKnockoutEntity = this.tournamentKnockoutService.getOne(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                            .eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
                            .eq(TournamentKnockoutEntity::getRound, 1)
                            .eq(TournamentKnockoutEntity::getHomeEntry, entry));
                    if (tournamentKnockoutEntity != null) {
                        int pkEntry = tournamentKnockoutEntity.getAwayEntry();
                        if (pkEntry > 0) {
                            tournamentGroupData
                                    .setPkDraw(true)
                                    .setPkEntry(pkEntry);
                            // pk entry group
                            TournamentGroupEntity tournamentGroupEntity = this.tournamentGroupService.getOne(new QueryWrapper<TournamentGroupEntity>().lambda()
                                    .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                                    .le(TournamentGroupEntity::getGroupId, groupNum)
                                    .eq(TournamentGroupEntity::getEntry, pkEntry));
                            if (tournamentGroupEntity != null) {
                                tournamentGroupData.setPkGroupName(tournamentGroupEntity.getGroupName());
                            }
                            // pk entry_info
                            EntryInfoData pkEntryInfo = this.queryService.qryEntryInfo(pkEntry);
                            if (pkEntryInfo != null) {
                                BeanUtil.copyProperties(pkEntryInfo, tournamentGroupData, CopyOptions.create().ignoreNullValue());

                            }
                        }
                    } else {
                        tournamentKnockoutEntity = this.tournamentKnockoutService.getOne(new QueryWrapper<TournamentKnockoutEntity>().lambda()
                                .eq(TournamentKnockoutEntity::getTournamentId, tournamentId)
                                .eq(TournamentKnockoutEntity::getRound, 1)
                                .eq(TournamentKnockoutEntity::getAwayEntry, entry));
                        if (tournamentKnockoutEntity != null) {
                            int pkEntry = tournamentKnockoutEntity.getHomeEntry();
                            if (pkEntry > 0) {
                                tournamentGroupData
                                        .setPkDraw(true)
                                        .setPkEntry(pkEntry);
                                // pk entry group
                                TournamentGroupEntity tournamentGroupEntity = this.tournamentGroupService.getOne(new QueryWrapper<TournamentGroupEntity>().lambda()
                                        .eq(TournamentGroupEntity::getTournamentId, tournamentId)
                                        .le(TournamentGroupEntity::getGroupId, groupNum)
                                        .eq(TournamentGroupEntity::getEntry, pkEntry));
                                if (tournamentGroupEntity != null) {
                                    tournamentGroupData.setPkGroupName(tournamentGroupEntity.getGroupName());
                                }
                                // pk entry_info
                                EntryInfoData pkEntryInfo = this.queryService.qryEntryInfo(pkEntry);
                                if (pkEntryInfo != null) {
                                    BeanUtil.copyProperties(pkEntryInfo, tournamentGroupData, CopyOptions.create().ignoreNullValue());

                                }
                            }
                        } else {
                            tournamentGroupData
                                    .setPkDraw(false)
                                    .setPkEntry(0)
                                    .setPkGroupName("")
                                    .setPkEntryName("")
                                    .setPkPlayerName("");
                        }
                    }
                    // cup
                    EntryEventCupResultEntity entryEventCupResultEntity = this.entryEventCupResultService.list(new QueryWrapper<EntryEventCupResultEntity>().lambda()
                                    .eq(EntryEventCupResultEntity::getEntry, entry)
                                    .orderByDesc(EntryEventCupResultEntity::getEvent))
                            .stream()
                            .findFirst()
                            .orElse(null);
                    if (entryEventCupResultEntity == null) {
                        tournamentGroupData.setLastCupEvent(0);
                    } else {
                        if (entryEventCupResultEntity.getEvent() == current && StringUtils.equals("Win", entryEventCupResultEntity.getResult())) {
                            tournamentGroupData.setLastCupEvent(99);
                        } else {
                            tournamentGroupData.setLastCupEvent(entryEventCupResultEntity.getEvent());
                        }
                    }
                    list.add(tournamentGroupData);
                });
        return new TableData<>(list);
    }

    @Cacheable(value = "qryPointsGroupChampion", key = "#tournamentId", unless = "#result == null")
    @Override
    public TableData<TournamentGroupEventChampionData> qryPointsGroupChampion(int tournamentId) {
        TournamentGroupEventChampionData data = new TournamentGroupEventChampionData();
        List<TournamentPointsGroupEventResultData> eventChampionResultList = Lists.newArrayList();
        List<TournamentPointsGroupEventResultData> eventRunnerUpResultList = Lists.newArrayList();
        List<TournamentPointsGroupEventResultData> eventSecondRunnerUpResultList = Lists.newArrayList();
        List<Multimap<Integer, TournamentPointsGroupEventResultData>> championCountList = Lists.newArrayList();
        // tournament_points_group_result
        int current = this.queryService.getCurrentEvent();
        IntStream.rangeClosed(1, current).forEach(event -> {
            List<TournamentPointsGroupResultEntity> tournamentPointsGroupResultEntityList = this.tournamentPointsGroupResultService.list(new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                    .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                    .eq(TournamentPointsGroupResultEntity::getEvent, event)
                    .gt(TournamentPointsGroupResultEntity::getEventPoints, 0)
                    .orderByDesc(TournamentPointsGroupResultEntity::getEventPoints));
            if (CollectionUtils.isEmpty(tournamentPointsGroupResultEntityList)) {
                return;
            }
            if (tournamentPointsGroupResultEntityList.size() < 3) {
                log.error("tournament:{}, event:{}, less than 3 entry, no champion then", tournamentId, event);
            }
            Multimap<Integer, TournamentPointsGroupEventResultData> eventChampionCountMap = this.getEventChampionCountMap(tournamentPointsGroupResultEntityList);
            eventChampionResultList.addAll(eventChampionCountMap.get(1));
            eventRunnerUpResultList.addAll(eventChampionCountMap.get(2));
            eventSecondRunnerUpResultList.addAll(eventChampionCountMap.get(3));
            championCountList.add(eventChampionCountMap);
        });
        // return
        data
                .setEventChampionResultList(eventChampionResultList)
                .setEventRunnerUpResultList(eventRunnerUpResultList)
                .setEventSecondRunnerUpResultList(eventSecondRunnerUpResultList)
                .setChampionCountList(this.setCountListByMultiMap(championCountList));
        return new TableData<>(data);
    }

    private Multimap<Integer, TournamentPointsGroupEventResultData> getEventChampionCountMap(List<TournamentPointsGroupResultEntity> tournamentPointsGroupResultEntityList) {
        Multimap<Integer, TournamentPointsGroupEventResultData> map = HashMultimap.create();
        int onePoints = tournamentPointsGroupResultEntityList.get(0).getEventPoints();
        map.put(1, this.initEventResultData(tournamentPointsGroupResultEntityList.get(0)));
        int twoPoints = tournamentPointsGroupResultEntityList.get(1).getEventPoints();
        if (twoPoints == onePoints) { // 1, 1, 2
            map.put(1, this.initEventResultData(tournamentPointsGroupResultEntityList.get(1)));
            map.put(2, this.initEventResultData(tournamentPointsGroupResultEntityList.get(2)));
        } else { // 1, 2, 2 or 1, 2, 3 or 1, 2, 3, 3
            map.put(2, this.initEventResultData(tournamentPointsGroupResultEntityList.get(1)));
            int threePoints = tournamentPointsGroupResultEntityList.get(2).getEventPoints();
            if (threePoints == twoPoints) { // 1, 2, 2
                map.put(2, this.initEventResultData(tournamentPointsGroupResultEntityList.get(2)));
            } else {
                map.put(3, this.initEventResultData(tournamentPointsGroupResultEntityList.get(2)));  // 1, 2, 3
                int fourPoints = tournamentPointsGroupResultEntityList.get(3).getEventPoints();
                if (fourPoints == threePoints) { // 1, 2, 3, 3
                    map.put(3, this.initEventResultData(tournamentPointsGroupResultEntityList.get(3)));
                }
            }
        }
        return map;
    }

    private TournamentPointsGroupEventResultData initEventResultData(TournamentPointsGroupResultEntity tournamentPointsGroupResultEntity) {
        TournamentPointsGroupEventResultData tournamentPointsGroupEventResultData = new TournamentPointsGroupEventResultData()
                .setTournamentId(tournamentPointsGroupResultEntity.getTournamentId())
                .setEvent(tournamentPointsGroupResultEntity.getEvent())
                .setPoints(tournamentPointsGroupResultEntity.getEventPoints())
                .setCost(tournamentPointsGroupResultEntity.getEventCost())
                .setNetPoints(tournamentPointsGroupResultEntity.getEventNetPoints())
                .setRank(tournamentPointsGroupResultEntity.getEventRank());
        // entry_info
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(tournamentPointsGroupResultEntity.getEntry());
        if (entryInfoData != null) {
            BeanUtil.copyProperties(entryInfoData, tournamentPointsGroupEventResultData, CopyOptions.create().ignoreNullValue());
        }
        return tournamentPointsGroupEventResultData;
    }

    private List<TournamentGroupChampionCountData> setCountListByMultiMap(List<Multimap<Integer, TournamentPointsGroupEventResultData>> championCountList) {
        Map<Integer, TournamentGroupChampionCountData> entryChampionCountMap = Maps.newHashMap();
        // champion
        List<TournamentPointsGroupEventResultData> championList = Lists.newArrayList();
        championCountList.forEach(o -> championList.addAll(o.get(1)));
        championList.forEach(o -> {
            int entry = o.getEntry();
            TournamentGroupChampionCountData entryChampionCountData = this.initChampionCountData(entry, entryChampionCountMap);
            entryChampionCountData.setChampionNum(entryChampionCountData.getChampionNum() + 1);
            entryChampionCountMap.put(entry, entryChampionCountData);
        });
        // runner_up
        List<TournamentPointsGroupEventResultData> runnerUpList = Lists.newArrayList();
        championCountList.forEach(o -> runnerUpList.addAll(o.get(2)));
        runnerUpList.forEach(o -> {
            int entry = o.getEntry();
            TournamentGroupChampionCountData entryChampionCountData = this.initChampionCountData(entry, entryChampionCountMap);
            entryChampionCountData.setRunnerUpNum(entryChampionCountData.getRunnerUpNum() + 1);
            entryChampionCountMap.put(entry, entryChampionCountData);
        });
        // second runner_up
        List<TournamentPointsGroupEventResultData> secondRunnerUpList = Lists.newArrayList();
        championCountList.forEach(o -> secondRunnerUpList.addAll(o.get(3)));
        secondRunnerUpList.forEach(o -> {
            int entry = o.getEntry();
            TournamentGroupChampionCountData entryChampionCountData = this.initChampionCountData(entry, entryChampionCountMap);
            entryChampionCountData.setSecondRunnerUpNum(entryChampionCountData.getSecondRunnerUpNum() + 1);
            entryChampionCountMap.put(entry, entryChampionCountData);
        });
        return entryChampionCountMap.values()
                .stream()
                .sorted(Comparator.comparing(TournamentGroupChampionCountData::getChampionNum)
                        .thenComparing(TournamentGroupChampionCountData::getRunnerUpNum)
                        .thenComparing(TournamentGroupChampionCountData::getSecondRunnerUpNum)
                        .reversed())
                .collect(Collectors.toList());
    }

    private TournamentGroupChampionCountData initChampionCountData(int entry, Map<Integer, TournamentGroupChampionCountData> entryChampionCountMap) {
        TournamentGroupChampionCountData entryChampionCountData;
        if (entryChampionCountMap.containsKey(entry)) {
            entryChampionCountData = entryChampionCountMap.get(entry);
        } else {
            entryChampionCountData = new TournamentGroupChampionCountData().setEntry(entry);
            EntryInfoData entryInfoData = this.queryService.qryEntryInfo(entry);
            if (entryInfoData != null) {
                entryChampionCountData
                        .setEntryName(entryInfoData.getEntryName())
                        .setPlayerName(entryInfoData.getPlayerName());
            }
        }
        return entryChampionCountData;
    }

    @Cacheable(value = "qryPagePointsGroupResult", key = "#tournamentId+'::'+#groupId+'::'+#entry+'::'+#page+'::'+#limit", unless = "#result == null")
    @Override
    public TableData<TournamentPointsGroupEventResultData> qryPagePointsGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
        List<TournamentPointsGroupEventResultData> list = Lists.newArrayList();
        // event_list
        int current = this.queryService.getCurrentEvent();
        List<Integer> eventList = Lists.newArrayList();
        IntStream.rangeClosed(1, current).forEach(eventList::add);
        if (CollectionUtils.isEmpty(eventList)) {
            return new TableData<>();
        }
        // tournament_points_group_result
        Page<TournamentPointsGroupResultEntity> pointsGroupResultPage = this.tournamentPointsGroupResultService.getBaseMapper().selectPage(
                new Page<>(page, limit, true), new QueryWrapper<TournamentPointsGroupResultEntity>().lambda()
                        .eq(TournamentPointsGroupResultEntity::getTournamentId, tournamentId)
                        .eq(TournamentPointsGroupResultEntity::getGroupId, groupId)
                        .eq(TournamentPointsGroupResultEntity::getEntry, entry)
                        .in(TournamentPointsGroupResultEntity::getEvent, eventList)
        );
        pointsGroupResultPage.getRecords().forEach(o -> {
            TournamentPointsGroupEventResultData tournamentPointsGroupEventResultData = new TournamentPointsGroupEventResultData()
                    .setTournamentId(tournamentId)
                    .setGroupId(groupId)
                    .setEvent(o.getEvent())
                    .setEntry(entry)
                    .setGroupRank(o.getEventGroupRank())
                    .setPoints(o.getEventPoints())
                    .setCost(o.getEventCost())
                    .setNetPoints(o.getEventNetPoints())
                    .setRank(o.getEventRank());
            // entry_event_result
            EntryEventResultData entryEventResultData = this.queryService.qryEntryEventResult(o.getEvent(), entry);
            if (entryEventResultData != null) {
                tournamentPointsGroupEventResultData
                        .setBenchPoints(entryEventResultData.getBenchPoints())
                        .setChip(entryEventResultData.getChip());
            }
            list.add(tournamentPointsGroupEventResultData);
        });
        Page<TournamentPointsGroupEventResultData> pageResult = new Page<>(page, limit, pointsGroupResultPage.getTotal());
        pageResult.setRecords(list);
        return new TableData<>(pageResult);
    }

    @Cacheable(value = "qryPageEntryEventCupResult", key = "#entry+'::'+#page+'::'+#limit")
    @Override
    public TableData<EntryCupData> qryPageEntryEventCupResult(int entry, int page, int limit) {
        Page<EntryEventCupResultEntity> cupResultPage = this.entryEventCupResultService.getBaseMapper().selectPage(
                new Page<>(page, limit, true), new QueryWrapper<EntryEventCupResultEntity>().lambda()
                        .eq(EntryEventCupResultEntity::getEntry, entry)
        );
        if (CollectionUtils.isEmpty(cupResultPage.getRecords())) {
            return new TableData<>();
        }
        List<EntryCupData> list = Lists.newArrayList();
        cupResultPage.getRecords().forEach(o -> {
            EntryCupData data = new EntryCupData()
                    .setEvent(o.getEvent())
                    .setEntry(entry)
                    .setEntryName(o.getEntryName())
                    .setPlayerName(o.getPlayerName())
                    .setEventPoints(o.getEventPoints())
                    .setAgainstEntry(o.getAgainstEntry())
                    .setAgainstEntryName(o.getAgainstEntryName())
                    .setAgainstPlayerName(o.getAgainstPlayerName())
                    .setAgainstEventPoints(o.getAgainstEventPoints())
                    .setResult(o.getResult());
            list.add(data);
        });
        Page<EntryCupData> pageResult = new Page<>(page, limit, cupResultPage.getTotal());
        pageResult.setRecords(list);
        return new TableData<>(pageResult);
    }

    @Cacheable(value = "qryPageBattleGroupResult", key = "#tournamentId+'::'+#groupId+'::'+#entry+'::'+#page+'::'+#limit")
    @Override
    public TableData<TournamentBattleGroupEventResultData> qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
        List<TournamentBattleGroupEventResultData> list = Lists.newArrayList();
        // event_list
        int current = this.queryService.getCurrentEvent();
        List<Integer> eventList = Lists.newArrayList();
        IntStream.rangeClosed(1, current).forEach(eventList::add);
        if (CollectionUtils.isEmpty(eventList)) {
            return new TableData<>();
        }
        // tournament_battle_group_result
        Page<TournamentBattleGroupResultEntity> battleGroupResultPage = this.tournamentBattleGroupResultService.getBaseMapper().selectPage(
                new Page<>(page, limit, true), new QueryWrapper<TournamentBattleGroupResultEntity>().lambda()
                        .eq(TournamentBattleGroupResultEntity::getTournamentId, tournamentId)
                        .eq(TournamentBattleGroupResultEntity::getGroupId, groupId)
                        .in(TournamentBattleGroupResultEntity::getEvent, eventList)
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
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(entry);
        if (entryInfoData == null) {
            return "";
        }
        return entryInfoData.getEntryName();
    }

    /**
     * @implNote live
     */
    @Override
    public TableData<LiveCalcData> qryEntryLivePoints(int entry) {
        LiveCalcData liveCalcData = this.liveService.calcLivePointsByEntry(this.queryService.getCurrentEvent(), entry);
        return new TableData<>(liveCalcData);
    }

    @Override
    public TableData<LiveCalcData> qryTournamentLivePoints(int tournamentId) {
        List<LiveCalcData> liveCalcList = this.liveService.calcLivePointsByTournament(this.queryService.getCurrentEvent(), tournamentId).getLiveCalcDataList();
        return new TableData<>(liveCalcList);
    }

    @Override
    public TableData<LiveMatchTeamData> qryLiveTeamDataList(int statusId) {
        return new TableData<>(this.queryService.qryLiveTeamDataList(statusId));
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
        List<EntryPickData> list = this.queryService.qryPickListFromPicks(picks)
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
        // entry_event_result
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

    /**
     * @implNote report
     */
    @Cacheable(value = "qryTeamSelectStatByName", key = "#event+'::'+#leagueName", unless = "#result==null")
    @Override
    public TableData<LeagueStatData> qryTeamSelectStatByName(int event, String leagueName) {
        LeagueStatData leagueStatData = new LeagueStatData().setName(leagueName).setEvent(event);
        // player info
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        // team select
        List<LeagueEventReportEntity> leagueEventReportList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueName, leagueName)
                .eq(LeagueEventReportEntity::getEvent, event));
        int teamSize = leagueEventReportList.size();
        if (CollectionUtils.isEmpty(leagueEventReportList)) {
            return new TableData<>(leagueStatData);
        }
        // most transfer in
        LinkedHashMap<String, String> mostTransferInMap = this.getMostTransferInMap(leagueName, event, leagueEventReportList, teamSize, playerMap);
        leagueStatData.setMostTransferIn(mostTransferInMap);
        // most transfer out
        LinkedHashMap<String, String> mostTransferOutMap = this.getMostTransferOutMap(leagueName, event, leagueEventReportList, teamSize, playerMap);
        leagueStatData.setMostTransferOut(mostTransferOutMap);
        // captain selected
        LinkedHashMap<String, String> captainSelectedMap = this.getCaptainSelectedMap(leagueEventReportList, teamSize, playerMap);
        leagueStatData.setCaptainSelectedMap(captainSelectedMap);
        // vice captain selected
        LinkedHashMap<String, String> viceCaptainSelectedMap = this.getViceCaptainSelectedMap(leagueEventReportList, teamSize, playerMap);
        leagueStatData.setViceCaptainSelectedMap(viceCaptainSelectedMap);
        // top selected player
        LinkedHashMap<String, String> topSelectedPlayerMap = this.getTopSelectedPlayerMap(leagueEventReportList, teamSize, playerMap);
        leagueStatData.setTopSelectedPlayerMap(topSelectedPlayerMap);
        // top selected team
        LinkedHashMap<Integer, Map<String, String>> topSelectedTeamMap = this.getTopSelectedTeamMap(leagueEventReportList, teamSize, playerMap);
        leagueStatData.setTopSelectedTeamMap(topSelectedTeamMap);
        return new TableData<>(leagueStatData);
    }

    private LinkedHashMap<String, String> getMostTransferInMap(String leagueName, int event, List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<String, PlayerEntity> playerMap) {
        if (event <= 1) {
            return Maps.newLinkedHashMap();
        }
        // current gw
        Map<Integer, List<Integer>> currentSelectMap = this.collectEntrySelectedMap(leagueEventReportList);
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

    private LinkedHashMap<String, String> getMostTransferOutMap(String leagueName, int event, List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<String, PlayerEntity> playerMap) {
        if (event <= 1) {
            return Maps.newLinkedHashMap();
        }
        // current gw
        Map<Integer, List<Integer>> currentSelectMap = this.collectEntrySelectedMap(leagueEventReportList);
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
        List<LeagueEventReportEntity> previousSelectList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueName, leagueName)
                .eq(LeagueEventReportEntity::getEvent, event - 1));
        return this.collectEntrySelectedMap(previousSelectList);
    }

    private Map<Integer, List<Integer>> collectEntrySelectedMap(List<LeagueEventReportEntity> leagueEventReportList) {
        Map<Integer, List<Integer>> teamSelectMap = Maps.newHashMap();
        leagueEventReportList.forEach(o -> {
            List<Integer> elementList = Lists.newArrayList(
                    o.getPosition1(), o.getPosition2(), o.getPosition3(), o.getPosition4(), o.getPosition5(),
                    o.getPosition6(), o.getPosition7(), o.getPosition8(), o.getPosition9(), o.getPosition10(),
                    o.getPosition11(), o.getPosition12(), o.getPosition13(), o.getPosition14(), o.getPosition15()
            );
            teamSelectMap.put(o.getEntry(), elementList);
        });
        return teamSelectMap;
    }

    private LinkedHashMap<String, String> getCaptainSelectedMap(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<String, PlayerEntity> playerMap) {
        List<Integer> elementList = leagueEventReportList
                .stream()
                .map(LeagueEventReportEntity::getCaptain)
                .collect(Collectors.toList());
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private LinkedHashMap<String, String> getViceCaptainSelectedMap(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<String, PlayerEntity> playerMap) {
        // collect
        List<Integer> elementList = leagueEventReportList
                .stream()
                .map(LeagueEventReportEntity::getViceCaptain)
                .collect(Collectors.toList());
        return this.collectSelectedMap(elementList, teamSize, 5, playerMap);
    }

    private LinkedHashMap<String, String> getTopSelectedPlayerMap(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<String, PlayerEntity> playerMap) {
        List<Integer> elementList = Lists.newArrayList();
        leagueEventReportList.forEach(o -> {
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

    private LinkedHashMap<Integer, Map<String, String>> getTopSelectedTeamMap(List<LeagueEventReportEntity> leagueEventReportList, int teamSize, Map<String, PlayerEntity> playerMap) {
        // element list
        List<PlayerEntity> elementPlayerInfoList = Lists.newArrayList();
        leagueEventReportList.forEach(o -> {
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition1())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition2())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition3())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition4())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition5())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition6())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition7())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition8())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition9())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition10())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition11())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition12())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition13())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition14())));
            elementPlayerInfoList.add(playerMap.get(String.valueOf(o.getPosition15())));
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
            playerSelectedMap.putAll(result);
        });
        // add key:element_type
        Map<Integer, Map<Integer, Integer>> elementTypeMap = this.collectPlayerSelectedMap(playerSelectedMap, playerMap); // key:element_type -> value: elementCountMap
        // sort by selected
        LinkedHashMap<Integer, Integer> elementSelectedSortMap = playerSelectedMap.entrySet() // key:element -> value: count (sort by count)
                .stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new));
        List<PlayerEntity> elementList = Lists.newArrayList();
        elementSelectedSortMap.forEach((k, v) -> elementList.add(playerMap.get(String.valueOf(k))));
        // selected line up
        LinkedHashMap<Integer, Map<String, String>> map = Maps.newLinkedHashMap(); // key:element_type -> value:elementCountMap(key:element -> value:percent)
        LinkedHashMap<Integer, Integer> lineupMap = this.getLineupMapByElementList(elementTypeMap, elementList); // key:position -> value:element
        lineupMap.forEach((position, element) -> {
            long count = playerSelectedMap.get(element);
            PlayerEntity playerEntity = playerMap.get(String.valueOf(element));
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

    private Map<Integer, Map<Integer, Integer>> collectPlayerSelectedMap(Map<Integer, Integer> playerSelectedMap, Map<String, PlayerEntity> playerMap) {
        Map<Integer, Map<Integer, Integer>> map = Maps.newHashMap();
        playerSelectedMap.forEach((k, v) -> {
            int elementType = playerMap.get(String.valueOf(k)).getElementType();
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
        // line up
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

    @Cacheable(value = "qryLeagueCaptainReportStat", key = "#leagueId+'::'+#leagueType", unless = "#result==null")
    @Override
    public TableData<LeagueEventReportStatData> qryLeagueCaptainReportStat(int leagueId, String leagueType) {
        // prepare
        Map<String, String> webNameMap = this.queryService.qryPlayerWebNameMap();
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .gt(LeagueEventReportEntity::getOverallPoints, 0)
                .orderByAsc(LeagueEventReportEntity::getEvent));
        Multimap<Integer, LeagueEventReportEntity> entryEventReportMap = HashMultimap.create();
        leagueEventReportEntityList.forEach(o -> entryEventReportMap.put(o.getEntry(), o));
        if (entryEventReportMap.isEmpty()) {
            return new TableData<>();
        }
        // collect
        List<LeagueEventReportStatData> list = Lists.newArrayList();
        entryEventReportMap.keySet().forEach(entry -> {
            LeagueEventReportStatData leagueEventReportStatData = this.qryEntryCaptainReportStat(entryEventReportMap.get(entry), webNameMap);
            if (leagueEventReportStatData != null) {
                list.add(leagueEventReportStatData);
            }
        });
        // sort
        Map<String, Integer> rankMap = this.sortLeagueCaptainReportRank(list);
        if (!CollectionUtils.isEmpty(rankMap)) {
            list.forEach(o -> o.setRank(rankMap.get(o.getCaptainTotalPoints() + "-" + o.getOverallRank())));
        }
        return new TableData<>(list
                .stream()
                .sorted(Comparator.comparing(LeagueEventReportStatData::getRank))
                .collect(Collectors.toList())
        );
    }

    private LeagueEventReportStatData qryEntryCaptainReportStat(Collection<LeagueEventReportEntity> leagueEventReportEntities, Map<String, String> webNameMap) {
        int size = leagueEventReportEntities.size();
        if (size == 0) {
            return null;
        }
        LeagueEventReportEntity leagueEventReportEntity = leagueEventReportEntities
                .stream()
                .max(Comparator.comparing(LeagueEventReportEntity::getEvent))
                .orElse(null);
        LeagueEventReportStatData leagueEventReportStatData = new LeagueEventReportStatData();
        BeanUtil.copyProperties(leagueEventReportEntity, leagueEventReportStatData, CopyOptions.create().ignoreNullValue());
        // stat
        int overallPoints = leagueEventReportStatData.getOverallPoints();
        int captainTotalPoints = this.calcCaptainTotalPoints(leagueEventReportEntities);
        LinkedHashMap<Integer, Long> captainSelectMap = Maps.newLinkedHashMap();
        leagueEventReportEntities
                .stream()
                .collect(Collectors.groupingBy(LeagueEventReportEntity::getPlayedCaptain, Collectors.counting())).entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .forEachOrdered(o -> captainSelectMap.put(o.getKey(), o.getValue()));
        // max
        LeagueEventReportEntity captainMaxEntity = leagueEventReportEntities
                .stream()
                .max(Comparator.comparing(this::calcPlayedCaptainPoints))
                .orElse(new LeagueEventReportEntity());
        int captainMax = captainMaxEntity.getPlayedCaptain() == captainMaxEntity.getCaptain() ?
                captainMaxEntity.getCaptain() : captainMaxEntity.getViceCaptain();
        int captainMaxPoints = captainMaxEntity.getPlayedCaptain() == captainMaxEntity.getCaptain() ?
                captainMaxEntity.getCaptainPoints() : captainMaxEntity.getViceCaptainPoints();
        // min
        LeagueEventReportEntity captainMinEntity = leagueEventReportEntities
                .stream()
                .min(Comparator.comparing(this::calcPlayedCaptainPoints))
                .orElse(new LeagueEventReportEntity());
        int captainMin = captainMinEntity.getPlayedCaptain() == captainMinEntity.getCaptain() ?
                captainMinEntity.getCaptain() : captainMinEntity.getViceCaptain();
        int captainMinPoints = captainMinEntity.getPlayedCaptain() == captainMinEntity.getCaptain() ?
                captainMinEntity.getCaptainPoints() : captainMinEntity.getViceCaptainPoints();
        // most selected captain
        int mostSelectedCaptain = captainSelectMap.keySet()
                .stream()
                .findFirst()
                .orElse(0);
        // data
        LeagueEventCaptainData leagueEventCaptainData = new LeagueEventCaptainData()
                .setTotalPoints(captainTotalPoints)
                .setTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(captainTotalPoints, overallPoints), 1))
                .setMaxPointsEvent(captainMaxEntity.getEvent())
                .setMaxPoints(captainMaxPoints)
                .setMaxPointsWebName(webNameMap.getOrDefault(String.valueOf(captainMax), ""))
                .setMinPointsEvent(captainMinEntity.getEvent())
                .setMinPoints(captainMinPoints)
                .setMinPointsWebName(webNameMap.getOrDefault(String.valueOf(captainMin), ""))
                .setMostSelected(mostSelectedCaptain)
                .setMostSelectedWebName(webNameMap.getOrDefault(String.valueOf(mostSelectedCaptain), ""))
                .setMostSelectedTimes(captainSelectMap.get(mostSelectedCaptain).intValue())
                .setBlankTimes((int) leagueEventReportEntities
                        .stream()
                        .filter(LeagueEventReportEntity::getCaptainBlank)
                        .count())
                .setHitTimes((int) leagueEventReportEntities
                        .stream()
                        .filter(o -> o.getCaptain().equals(o.getHighestScore()))
                        .count());
        leagueEventReportStatData
                .setCaptainTotalPoints(leagueEventCaptainData.getTotalPoints())
                .setCaptainData(leagueEventCaptainData);
        return leagueEventReportStatData;
    }

    private int calcCaptainTotalPoints(Collection<LeagueEventReportEntity> leagueEventReportEntities) {
        return leagueEventReportEntities
                .stream()
                .mapToInt(this::calcPlayedCaptainPoints)
                .sum();
    }

    private int calcPlayedCaptainPoints(LeagueEventReportEntity leagueEventReportEntity) {
        int captainPoints = leagueEventReportEntity.getPlayedCaptain() == leagueEventReportEntity.getCaptain() ?
                leagueEventReportEntity.getCaptainPoints() : leagueEventReportEntity.getViceCaptainPoints();
        return StringUtils.equals(Chip.TC.getValue(), leagueEventReportEntity.getEventChip()) ? captainPoints * 3 : captainPoints * 2;
    }

    private Map<String, Integer> sortLeagueCaptainReportRank(List<LeagueEventReportStatData> leagueEventReportStatDataList) {
        Map<String, Integer> rankMap = Maps.newHashMap();
        Map<String, Integer> rankCountMap = Maps.newLinkedHashMap();
        leagueEventReportStatDataList
                .stream()
                .sorted(
                        Comparator.comparing(LeagueEventReportStatData::getCaptainTotalPoints)
                                .thenComparing(LeagueEventReportStatData::getOverallRank)
                                .reversed()
                )
                .forEachOrdered(o -> {
                    String key = o.getCaptainTotalPoints() + "-" + o.getOverallRank();
                    if (rankCountMap.containsKey(key)) {
                        rankCountMap.put(key, rankCountMap.get(key) + 1);
                    } else {
                        rankCountMap.put(key, 1);
                    }
                });
        int index = 1;
        for (String key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    @Cacheable(value = "qryLeagueCaptainEventReportList", key = "#event+'::'+#leagueId+'::'+#leagueType", unless = "#result==null")
    @Override
    public TableData<LeagueEventReportData> qryLeagueCaptainEventReportList(int event, int leagueId, String leagueType) {
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .eq(LeagueEventReportEntity::getEvent, event));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return new TableData<>();
        }
        List<LeagueEventReportData> list = Lists.newArrayList();
        // prepare
        Map<String, String> webNameMap = this.queryService.qryPlayerWebNameMap();
        Map<String, String> elementEoMap = this.queryService.qryLeagueEventEoMap(event, leagueId, leagueType);
        // collect
        leagueEventReportEntityList.forEach(o -> {
            LeagueEventReportData data = new LeagueEventReportData();
            BeanUtil.copyProperties(o, data);
            // captain
            int captain = o.getPlayedCaptain();
            int captainPoints = captain == o.getCaptain() ? o.getCaptainPoints() : o.getViceCaptainPoints();
            data
                    .setPlayedCaptain(captain)
                    .setCaptainWebName(webNameMap.getOrDefault(String.valueOf(captain), ""))
                    .setCaptainPoints(StringUtils.equals(Chip.TC.getValue(), o.getEventChip()) ? 3 * captainPoints : 2 * captainPoints)
                    .setCaptainPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(captainPoints, data.getEventPoints()), 1))
                    .setCaptainBlank(captain == o.getCaptain() ? o.getCaptainBlank() : o.getViceCaptainBlank())
                    .setCaptainSelected(captain == o.getCaptain() ? o.getCaptainSelected() : o.getViceCaptainSelected())
                    .setCaptainEffectiveOwnerShipRate(elementEoMap.get(String.valueOf(captain)))
                    .setHighestScoreWebName(webNameMap.getOrDefault(String.valueOf(o.getHighestScore()), ""))
                    .setHighestScorePointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getHighestScorePoints(), data.getEventPoints()), 1))
                    .setHighestScoreEffectiveOwnerShipRate(elementEoMap.get(String.valueOf(o.getHighestScore())));
            list.add(data);
        });
        // sort
        Map<String, Integer> rankMap = this.sortLeagueCaptainEventRank(list);
        if (!CollectionUtils.isEmpty(rankMap)) {
            list.forEach(o -> o.setRank(rankMap.get(o.getCaptainPoints() + "-" + o.getEventNetPoints())));
        }
        return new TableData<>(list
                .stream()
                .sorted(Comparator.comparing(LeagueEventReportData::getRank))
                .collect(Collectors.toList())
        );
    }

    private Map<String, Integer> sortLeagueCaptainEventRank(List<LeagueEventReportData> leagueEventReportDataList) {
        Map<String, Integer> rankMap = Maps.newHashMap();
        Map<String, Integer> rankCountMap = Maps.newLinkedHashMap();
        leagueEventReportDataList
                .stream()
                .sorted(
                        Comparator.comparing(LeagueEventReportData::getCaptainPoints)
                                .thenComparing(LeagueEventReportData::getEventNetPoints)
                                .reversed()
                )
                .forEachOrdered(o -> {
                    String key = o.getCaptainPoints() + "-" + o.getEventNetPoints();
                    if (rankCountMap.containsKey(key)) {
                        rankCountMap.put(key, rankCountMap.get(key) + 1);
                    } else {
                        rankCountMap.put(key, 1);
                    }
                });
        int index = 1;
        for (String key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    @Cacheable(value = "qryEntryCaptainEventReportList", key = "#leagueId+'::'+#leagueType+'::'+#entry", unless = "#result==null")
    @Override
    public TableData<LeagueEventReportData> qryEntryCaptainEventReportList(int leagueId, String leagueType, int entry) {
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .eq(LeagueEventReportEntity::getEntry, entry)
                .gt(LeagueEventReportEntity::getEventPoints, 0)
                .orderByAsc(LeagueEventReportEntity::getEvent));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return new TableData<>();
        }
        List<LeagueEventReportData> list = Lists.newArrayList();
        // prepare
        Map<String, String> webNameMap = this.queryService.qryPlayerWebNameMap();
        // collect
        leagueEventReportEntityList.forEach(o -> {
            Map<String, String> elementEoMap = this.queryService.qryLeagueEventEoMap(o.getEvent(), leagueId, leagueType);
            LeagueEventReportData data = new LeagueEventReportData();
            BeanUtil.copyProperties(o, data);
            // captain
            int captain = o.getPlayedCaptain();
            int captainPoints = captain == o.getCaptain() ? o.getCaptainPoints() : o.getViceCaptainPoints();
            data
                    .setPlayedCaptain(captain)
                    .setCaptainWebName(webNameMap.getOrDefault(String.valueOf(captain), ""))
                    .setCaptainPoints(StringUtils.equals(Chip.TC.getValue(), o.getEventChip()) ? 3 * captainPoints : 2 * captainPoints)
                    .setCaptainPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(captainPoints, data.getEventPoints()), 1))
                    .setCaptainBlank(captain == o.getCaptain() ? o.getCaptainBlank() : o.getViceCaptainBlank())
                    .setCaptainSelected(captain == o.getCaptain() ? o.getCaptainSelected() : o.getViceCaptainSelected())
                    .setCaptainEffectiveOwnerShipRate(elementEoMap.get(String.valueOf(captain)))
                    .setHighestScoreWebName(webNameMap.getOrDefault(String.valueOf(o.getHighestScore()), ""))
                    .setHighestScorePointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getHighestScorePoints(), data.getEventPoints()), 1))
                    .setHighestScoreEffectiveOwnerShipRate(elementEoMap.get(String.valueOf(o.getHighestScore())));
            list.add(data);
        });
        return new TableData<>(list);
    }

    @Cacheable(value = "qryLeagueTransferReportStat", key = "#leagueId+'::'+#leagueType", unless = "#result==null")
    @Override
    public TableData<LeagueEventReportStatData> qryLeagueTransfersReportStat(int leagueId, String leagueType) {
        // prepare
        Table<Integer, Integer, Integer> elementPointsTable = HashBasedTable.create(); // element -> event -> points
        this.eventLiveService.list().forEach(o -> elementPointsTable.put(o.getElement(), o.getEvent(), o.getTotalPoints()));
        Multimap<Integer, EntryEventTransfersEntity> entryEventTransferMap = HashMultimap.create();
        this.entryEventTransferService.list().forEach(o -> entryEventTransferMap.put(o.getEntry(), o));
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .orderByAsc(LeagueEventReportEntity::getEvent));
        Multimap<Integer, LeagueEventReportEntity> entryEventReportMap = HashMultimap.create();
        leagueEventReportEntityList.forEach(o -> entryEventReportMap.put(o.getEntry(), o));
        if (entryEventReportMap.isEmpty()) {
            return new TableData<>();
        }
        // collect
        List<LeagueEventReportStatData> list = Lists.newArrayList();
        entryEventReportMap.keySet().forEach(entry -> {
            LeagueEventReportStatData leagueEventReportStatData = this.qryEntryTransferReportStat(entry, entryEventReportMap.get(entry), elementPointsTable, entryEventTransferMap);
            if (leagueEventReportStatData != null) {
                list.add(leagueEventReportStatData);
            }
        });
        // sort
        Map<String, Integer> rankMap = this.sortLeagueTransfersReportRank(list);
        if (!CollectionUtils.isEmpty(rankMap)) {
            list.forEach(o -> o.setRank(rankMap.get(o.getTransferTotalNetPoints() + "-" + o.getOverallRank())));
        }
        return new TableData<>(list
                .stream()
                .sorted(Comparator.comparing(LeagueEventReportStatData::getRank))
                .collect(Collectors.toList())
        );
    }

    private LeagueEventReportStatData qryEntryTransferReportStat(int entry, Collection<LeagueEventReportEntity> leagueEventReportEntities, Table<Integer, Integer, Integer> elementPointsTable, Multimap<Integer, EntryEventTransfersEntity> entryEventTransferMap) {
        int size = leagueEventReportEntities.size();
        if (size == 0) {
            return null;
        }
        if (!entryEventTransferMap.containsKey(entry)) {
            return null;
        }
        Collection<EntryEventTransfersEntity> entryEventTransferEntities = entryEventTransferMap.get(entry);
        LeagueEventReportEntity leagueEventReportEntity = leagueEventReportEntities
                .stream()
                .max(Comparator.comparing(LeagueEventReportEntity::getEvent))
                .orElse(null);
        LeagueEventReportStatData leagueEventReportStatData = new LeagueEventReportStatData();
        BeanUtil.copyProperties(leagueEventReportEntity, leagueEventReportStatData, CopyOptions.create().ignoreNullValue());
        leagueEventReportStatData
                .setTransfers(
                        leagueEventReportEntities
                                .stream()
                                .mapToInt(LeagueEventReportEntity::getEventTransfers)
                                .sum()
                )
                .setTransfersPlayed((int) entryEventTransferEntities
                        .stream()
                        .filter(EntryEventTransfersEntity::getElementInPlayed)
                        .count()
                )
                .setTransfersCost(
                        leagueEventReportEntities
                                .stream()
                                .mapToInt(LeagueEventReportEntity::getEventTransfersCost)
                                .sum()
                );
        // data
        LeagueEventTransferData data = new LeagueEventTransferData()
                .setTransferInTotalPoints(this.sumSeasonElementPoints(elementPointsTable, entryEventTransferEntities, "in"))
                .setTransferInPlayedTotalPoints(this.sumSeasonElementPoints(elementPointsTable, entryEventTransferEntities, "played"))
                .setTransferOutTotalPoints(this.sumSeasonElementPoints(elementPointsTable, entryEventTransferEntities, "out"))
                .setTransferInTotalValue(
                        entryEventTransferEntities
                                .stream()
                                .mapToInt(EntryEventTransfersEntity::getElementInCost)
                                .sum()
                )
                .setTransferOutTotalValue(
                        entryEventTransferEntities
                                .stream()
                                .mapToInt(EntryEventTransfersEntity::getElementOutCost)
                                .sum()
                );
        data
                .setTransferPoints(data.getTransferInTotalPoints() - data.getTransferOutTotalPoints())
                .setTransferPlayedPoints(data.getTransferInPlayedTotalPoints() - data.getTransferOutTotalPoints())
                .setTransferNetPoints(data.getTransferPlayedPoints() - leagueEventReportStatData.getTransfersCost())
                .setTransferValue(data.getTransferInTotalValue() - data.getTransferOutTotalValue());
        leagueEventReportStatData
                .setTransferTotalNetPoints(data.getTransferNetPoints())
                .setTransferData(data);
        return leagueEventReportStatData;
    }

    private int sumSeasonElementPoints(Table<Integer, Integer, Integer> elementPointsTable, Collection<EntryEventTransfersEntity> entryEventTransferEntities, String type) {
        switch (type) {
            case "in": {
                return entryEventTransferEntities
                        .stream()
                        .mapToInt(o -> elementPointsTable.get(o.getElementIn(), o.getEvent()))
                        .sum();
            }
            case "played": {
                return entryEventTransferEntities
                        .stream()
                        .filter(EntryEventTransfersEntity::getElementInPlayed)
                        .mapToInt(o -> elementPointsTable.get(o.getElementIn(), o.getEvent()))
                        .sum();
            }
            case "out": {
                return entryEventTransferEntities
                        .stream()
                        .mapToInt(o -> elementPointsTable.get(o.getElementOut(), o.getEvent()))
                        .sum();
            }
        }
        return 0;
    }

    private Map<String, Integer> sortLeagueTransfersReportRank(List<LeagueEventReportStatData> leagueEventReportStatDataList) {
        Map<String, Integer> rankMap = Maps.newHashMap();
        Map<String, Integer> rankCountMap = Maps.newLinkedHashMap();
        leagueEventReportStatDataList
                .stream()
                .sorted(
                        Comparator.comparing(LeagueEventReportStatData::getTransferTotalNetPoints)
                                .thenComparing(LeagueEventReportStatData::getOverallRank)
                                .reversed()
                )
                .forEachOrdered(o -> {
                    String key = o.getTransferTotalNetPoints() + "-" + o.getOverallRank();
                    if (rankCountMap.containsKey(key)) {
                        rankCountMap.put(key, rankCountMap.get(key) + 1);
                    } else {
                        rankCountMap.put(key, 1);
                    }
                });
        int index = 1;
        for (String key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    @Cacheable(value = "qryLeagueTransferEventReportList", key = "#event+'::'+#leagueId+'::'+#leagueType", unless = "#result==null")
    @Override
    public TableData<LeagueEventReportData> qryLeagueTransfersEventReportList(int event, int leagueId, String leagueType) {
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .eq(LeagueEventReportEntity::getEvent, event));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return new TableData<>();
        }
        List<Integer> entryList = leagueEventReportEntityList
                .stream()
                .map(LeagueEventReportEntity::getEntry)
                .collect(Collectors.toList());
        List<LeagueEventReportData> list = Lists.newArrayList();
        // prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Map<Integer, Integer> elementPointsMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        Multimap<Integer, EntryEventTransfersEntity> entryEventTransferMap = HashMultimap.create();
        this.entryEventTransferService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                .eq(EntryEventTransfersEntity::getEvent, event)
                .in(EntryEventTransfersEntity::getEntry, entryList))
                .forEach(o -> entryEventTransferMap.put(o.getEntry(), o));
        // collect
        leagueEventReportEntityList.forEach(o -> {
            int entry = o.getEntry();
            if (!entryEventTransferMap.containsKey(entry)) {
                return;
            }
            LeagueEventReportData data = new LeagueEventReportData();
            BeanUtil.copyProperties(o, data);
            Collection<EntryEventTransfersEntity> entryEventTransferEntities = entryEventTransferMap.get(entry);
            data
                    .setEventTransfersPlayed((int)
                            entryEventTransferEntities
                                    .stream()
                                    .filter(EntryEventTransfersEntity::getElementInPlayed)
                                    .count()
                    )
                    .setTransferInTotalPoints(this.sumElementPoints(elementPointsMap,
                            entryEventTransferEntities
                                    .stream()
                                    .map(EntryEventTransfersEntity::getElementIn)
                                    .collect(Collectors.toList())
                    ))
                    .setTransferInPlayedTotalPoints(this.sumElementPoints(elementPointsMap,
                            entryEventTransferEntities
                                    .stream()
                                    .filter(EntryEventTransfersEntity::getElementInPlayed)
                                    .map(EntryEventTransfersEntity::getElementIn)
                                    .collect(Collectors.toList())
                    ))
                    .setTransferOutTotalPoints(this.sumElementPoints(elementPointsMap,
                            entryEventTransferEntities
                                    .stream()
                                    .map(EntryEventTransfersEntity::getElementOut)
                                    .collect(Collectors.toList())
                    ))
                    .setTransferInTotalValue(
                            entryEventTransferEntities
                                    .stream()
                                    .mapToInt(EntryEventTransfersEntity::getElementInCost)
                                    .sum()
                    )
                    .setTransferOutTotalValue(
                            entryEventTransferEntities
                                    .stream()
                                    .mapToInt(EntryEventTransfersEntity::getElementOutCost)
                                    .sum()
                    );
            data
                    .setTransferPoints(data.getTransferInTotalPoints() - data.getTransferOutTotalPoints())
                    .setTransferPlayedPoints(data.getTransferInPlayedTotalPoints() - data.getTransferOutTotalPoints())
                    .setTransferNetPoints(data.getTransferPlayedPoints() - data.getEventTransfersCost())
                    .setTransferValue(data.getTransferInTotalValue() - data.getTransferOutTotalValue())
                    .setEntryEventTransferList(this.setEntryTransferList(entryEventTransferEntities, playerMap, elementPointsMap));
            if (StringUtils.equals(data.getEventChip(), Chip.WC.getValue()) || StringUtils.equals(data.getEventChip(), Chip.FH.getValue())) {
                data
                        .setEventTransfers(0)
                        .setEventTransfersPlayed(0)
                        .setTransferInTotalValue(0)
                        .setTransferOutTotalValue(0)
                        .setTransferValue(0);
            }
            list.add(data);
        });
        // sort
        Map<String, Integer> rankMap = this.sortLeagueTransfersEventRank(list);
        if (!CollectionUtils.isEmpty(rankMap)) {
            list.forEach(o -> o.setRank(rankMap.get(o.getTransferNetPoints() + "-" + o.getEventNetPoints())));
        }
        return new TableData<>(list
                .stream()
                .sorted(Comparator.comparing(LeagueEventReportData::getRank))
                .collect(Collectors.toList())
        );
    }

    private int sumElementPoints(Map<Integer, Integer> elementPointsMap, List<Integer> elementList) {
        return elementList
                .stream()
                .mapToInt(o -> elementPointsMap.getOrDefault(o, 0))
                .sum();
    }

    private List<EntryEventTransfersData> setEntryTransferList(Collection<EntryEventTransfersEntity> entryEventTransferEntities, Map<String, PlayerEntity> playerMap, Map<Integer, Integer> elementPointsMap) {
        List<EntryEventTransfersData> list = Lists.newArrayList();
        entryEventTransferEntities.forEach(o -> {
            EntryEventTransfersData entryEventTransferData = new EntryEventTransfersData();
            entryEventTransferData
                    .setEntry(o.getEntry())
                    .setEvent(o.getEvent())
                    .setElementIn(o.getElementIn())
                    .setElementInCost(o.getElementInCost())
                    .setElementInPoints(elementPointsMap.getOrDefault(o.getElementIn(), 0))
                    .setElementInPlayed(o.getElementInPlayed())
                    .setElementOut(o.getElementOut())
                    .setElementOutCost(o.getElementOutCost())
                    .setElementOutPoints(elementPointsMap.getOrDefault(o.getElementOut(), 0))
                    .setTime(LocalDateTime.parse(StringUtils.substringBefore(o.getTime(), ".")).format(DateTimeFormatter.ofPattern(Constant.DATETIME)));
            int elementIn = o.getElementIn();
            if (playerMap.containsKey(String.valueOf(elementIn))) {
                PlayerEntity elementInEntity = playerMap.get(String.valueOf(elementIn));
                entryEventTransferData
                        .setElementInWebName(elementInEntity.getWebName())
                        .setElementInType(elementInEntity.getElementType())
                        .setElementInTypeName(Position.getNameFromElementType(elementInEntity.getElementType()));
            }
            int elementOut = o.getElementOut();
            if (playerMap.containsKey(String.valueOf(elementOut))) {
                PlayerEntity elementOutEntity = playerMap.get(String.valueOf(elementOut));
                entryEventTransferData
                        .setElementOutWebName(elementOutEntity.getWebName())
                        .setElementOutType(elementOutEntity.getElementType())
                        .setElementOutTypeName(Position.getNameFromElementType(elementOutEntity.getElementType()));
            }
            list.add(entryEventTransferData);
        });
        return list
                .stream()
                .sorted(Comparator.comparing(EntryEventTransfersData::getTime))
                .collect(Collectors.toList());
    }

    private Map<String, Integer> sortLeagueTransfersEventRank(List<LeagueEventReportData> leagueEventReportDataList) {
        Map<String, Integer> rankMap = Maps.newHashMap();
        Map<String, Integer> rankCountMap = Maps.newLinkedHashMap();
        leagueEventReportDataList
                .stream()
                .sorted(
                        Comparator.comparing(LeagueEventReportData::getTransferNetPoints)
                                .thenComparing(LeagueEventReportData::getEventNetPoints)
                                .reversed()
                )
                .forEachOrdered(o -> {
                    String key = o.getTransferNetPoints() + "-" + o.getEventNetPoints();
                    if (rankCountMap.containsKey(key)) {
                        rankCountMap.put(key, rankCountMap.get(key) + 1);
                    } else {
                        rankCountMap.put(key, 1);
                    }
                });
        int index = 1;
        for (String key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    @Cacheable(value = "qryEntryTransferEventReportList", key = "#leagueId+'::'+#leagueType+'::'+#entry", unless = "#result==null")
    @Override
    public TableData<LeagueEventReportData> qryEntryTransfersEventReportList(int leagueId, String leagueType, int entry) {
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .eq(LeagueEventReportEntity::getEntry, entry)
                .orderByAsc(LeagueEventReportEntity::getEvent));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return new TableData<>();
        }
        List<LeagueEventReportData> list = Lists.newArrayList();
        // prepare
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        Multimap<Integer, EntryEventTransfersEntity> entryEventTransferMap = HashMultimap.create();
        this.entryEventTransferService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                .eq(EntryEventTransfersEntity::getEntry, entry))
                .forEach(o -> entryEventTransferMap.put(o.getEvent(), o));
        // collect
        leagueEventReportEntityList.forEach(o -> {
            int event = o.getEvent();
            Map<Integer, Integer> elementPointsMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                    .eq(EventLiveEntity::getEvent, event))
                    .stream()
                    .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
            if (!entryEventTransferMap.containsKey(event)) {
                return;
            }
            LeagueEventReportData data = new LeagueEventReportData();
            BeanUtil.copyProperties(o, data);
            Collection<EntryEventTransfersEntity> entryEventTransferEntities = entryEventTransferMap.get(event);
            data
                    .setEventTransfersPlayed((int)
                            entryEventTransferEntities
                                    .stream()
                                    .filter(EntryEventTransfersEntity::getElementInPlayed)
                                    .count()
                    )
                    .setTransferInTotalPoints(this.sumElementPoints(elementPointsMap,
                            entryEventTransferEntities
                                    .stream()
                                    .map(EntryEventTransfersEntity::getElementIn)
                                    .collect(Collectors.toList())
                    ))
                    .setTransferInPlayedTotalPoints(this.sumElementPoints(elementPointsMap,
                            entryEventTransferEntities
                                    .stream()
                                    .filter(EntryEventTransfersEntity::getElementInPlayed)
                                    .map(EntryEventTransfersEntity::getElementIn)
                                    .collect(Collectors.toList())
                    ))
                    .setTransferOutTotalPoints(this.sumElementPoints(elementPointsMap,
                            entryEventTransferEntities
                                    .stream()
                                    .map(EntryEventTransfersEntity::getElementOut)
                                    .collect(Collectors.toList())
                    ))
                    .setTransferInTotalValue(
                            entryEventTransferEntities
                                    .stream()
                                    .mapToInt(EntryEventTransfersEntity::getElementInCost)
                                    .sum()
                    )

                    .setTransferOutTotalValue(
                            entryEventTransferEntities
                                    .stream()
                                    .mapToInt(EntryEventTransfersEntity::getElementOutCost)
                                    .sum()
                    );
            data
                    .setTransferPoints(data.getTransferInTotalPoints() - data.getTransferOutTotalPoints())
                    .setTransferPlayedPoints(data.getTransferInPlayedTotalPoints() - data.getTransferOutTotalPoints())
                    .setTransferNetPoints(data.getTransferPlayedPoints() - data.getEventTransfersCost())
                    .setTransferValue(data.getTransferInTotalValue() - data.getTransferOutTotalValue())
                    .setEntryEventTransferList(this.setEntryTransferList(entryEventTransferEntities, playerMap, elementPointsMap));
            if (StringUtils.equals(data.getEventChip(), Chip.WC.getValue()) || StringUtils.equals(data.getEventChip(), Chip.FH.getValue())) {
                data
                        .setEventTransfers(entryEventTransferEntities.size())
                        .setTransferInTotalValue(0)
                        .setTransferOutTotalValue(0)
                        .setTransferValue(0);
            }
            list.add(data);
        });
        return new TableData<>(list);
    }

    // do not cache
    @Override
    public TableData<LeagueEventReportStatData> qryLeagueScoringReportStat(int leagueId, String leagueType) {
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return new TableData<>();
        }
        List<Integer> entryList = leagueEventReportEntityList
                .stream()
                .map(LeagueEventReportEntity::getEntry)
                .collect(Collectors.toList());
        // prepare
        Multimap<Integer, LeagueEventReportEntity> entryLeagueReportMap = HashMultimap.create();
        leagueEventReportEntityList.forEach(o -> entryLeagueReportMap.put(o.getEntry(), o));
        List<PlayerPickData> pickDataList = this.queryService.qryLeaguePickDataList(leagueId, leagueType, entryList);
        Table<Integer, Integer, List<EntryPickData>> eventEventPickTable = HashBasedTable.create(); // entry -> element_type -> List<EntryPickData>
        pickDataList.forEach(o -> {
            eventEventPickTable.put(o.getEntry(), Position.GKP.getElementType(), o.getGkps());
            eventEventPickTable.put(o.getEntry(), Position.DEF.getElementType(), o.getDefs());
            eventEventPickTable.put(o.getEntry(), Position.MID.getElementType(), o.getMids());
            eventEventPickTable.put(o.getEntry(), Position.FWD.getElementType(), o.getFwds());
            eventEventPickTable.put(o.getEntry(), Position.SUB.getElementType(), o.getFwds());
        });
        Map<String, String> playerNameMap = this.queryService.qryPlayerWebNameMap();
        // collect
        List<LeagueEventReportStatData> list = Lists.newArrayList();
        entryLeagueReportMap.keySet().forEach(entry -> {
            Collection<LeagueEventReportEntity> stats = entryLeagueReportMap.get(entry);
            LeagueEventReportStatData data = new LeagueEventReportStatData();
            LeagueEventReportEntity lastStat = stats
                    .stream()
                    .max(Comparator.comparing(LeagueEventReportEntity::getEvent))
                    .orElse(null);
            if (lastStat == null) {
                return;
            }
            BeanUtil.copyProperties(lastStat, data, CopyOptions.create().ignoreNullValue());
            data
                    .setTransfersCost(
                            stats
                                    .stream()
                                    .mapToInt(LeagueEventReportEntity::getEventTransfersCost)
                                    .sum()
                    );
            LeagueEventScoringData leagueEventScoringData = new LeagueEventScoringData()
                    .setBenchTotalPoints(
                            stats
                                    .stream()
                                    .mapToInt(LeagueEventReportEntity::getEventBenchPoints)
                                    .sum()
                    )
                    .setAutoSubsTotalPoints(
                            stats
                                    .stream()
                                    .mapToInt(LeagueEventReportEntity::getEventAutoSubPoints)
                                    .sum()
                    )
                    .setGkpTotalPoints(
                            Objects.requireNonNull(eventEventPickTable.get(entry, Position.GKP.getElementType()))
                                    .stream()
                                    .mapToInt(EntryPickData::getPoints)
                                    .sum()
                    )
                    .setDefTotalPoints(
                            Objects.requireNonNull(eventEventPickTable.get(entry, Position.DEF.getElementType()))
                                    .stream()
                                    .mapToInt(EntryPickData::getPoints)
                                    .sum()
                    )
                    .setMidTotalPoints(
                            Objects.requireNonNull(eventEventPickTable.get(entry, Position.MID.getElementType()))
                                    .stream()
                                    .mapToInt(EntryPickData::getPoints)
                                    .sum()
                    )
                    .setFwdTotalPoints(
                            Objects.requireNonNull(eventEventPickTable.get(entry, Position.FWD.getElementType()))
                                    .stream()
                                    .mapToInt(EntryPickData::getPoints)
                                    .sum()
                    )
                    .setCaptainTotalPoints(
                            stats
                                    .stream()
                                    .mapToInt(o -> o.getPlayedCaptain() == o.getCaptain() ? o.getCaptainPoints() : o.getViceCaptainPoints())
                                    .sum()
                    )
                    .setMostSelectedGkp(
                            Objects.requireNonNull(eventEventPickTable.get(entry, Position.GKP.getElementType()))
                                    .stream()
                                    .collect(Collectors.groupingBy(EntryPickData::getElement, Collectors.counting()))
                                    .entrySet()
                                    .stream()
                                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse(0)
                    )
                    .setMostSelectedDef(
                            Objects.requireNonNull(eventEventPickTable.get(entry, Position.DEF.getElementType()))
                                    .stream()
                                    .collect(Collectors.groupingBy(EntryPickData::getElement, Collectors.counting()))
                                    .entrySet()
                                    .stream()
                                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse(0)
                    )
                    .setMostSelectedMid(
                            Objects.requireNonNull(eventEventPickTable.get(entry, Position.MID.getElementType()))
                                    .stream()
                                    .collect(Collectors.groupingBy(EntryPickData::getElement, Collectors.counting()))
                                    .entrySet()
                                    .stream()
                                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse(0)
                    )
                    .setMostSelectedFwd(
                            Objects.requireNonNull(eventEventPickTable.get(entry, Position.FWD.getElementType()))
                                    .stream()
                                    .collect(Collectors.groupingBy(EntryPickData::getElement, Collectors.counting()))
                                    .entrySet()
                                    .stream()
                                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse(0)
                    )
                    .setMostSelectedCaptain(
                            stats
                                    .stream()
                                    .map(o -> o.getPlayedCaptain() == o.getCaptain() ? o.getCaptain() : o.getViceCaptain())
                                    .collect(Collectors.groupingBy(Integer::intValue, Collectors.counting()))
                                    .entrySet()
                                    .stream()
                                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse(0)
                    )
                    .setMostSelectedFormation(
                            pickDataList
                                    .stream()
                                    .filter(o -> o.getEntry() == entry)
                                    .map(PlayerPickData::getFormation)
                                    .findFirst()
                                    .orElse("")
                    );
            leagueEventScoringData
                    .setAutoSubsTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(leagueEventScoringData.getAutoSubsTotalPoints(), data.getOverallPoints()), 1))
                    .setGkpTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(leagueEventScoringData.getGkpTotalPoints(), data.getOverallPoints()), 1))
                    .setDefTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(leagueEventScoringData.getDefTotalPoints(), data.getOverallPoints()), 1))
                    .setMidTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(leagueEventScoringData.getMidTotalPoints(), data.getOverallPoints()), 1))
                    .setFwdTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(leagueEventScoringData.getFwdTotalPoints(), data.getOverallPoints()), 1))
                    .setCaptainTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(leagueEventScoringData.getCaptainTotalPoints(), data.getOverallPoints()), 1))
                    .setMostSelectedGkpName(playerNameMap.getOrDefault(String.valueOf(leagueEventScoringData.getMostSelectedGkp()), ""))
                    .setMostSelectedDefName(playerNameMap.getOrDefault(String.valueOf(leagueEventScoringData.getMostSelectedDef()), ""))
                    .setMostSelectedMidName(playerNameMap.getOrDefault(String.valueOf(leagueEventScoringData.getMostSelectedMid()), ""))
                    .setMostSelectedFwdName(playerNameMap.getOrDefault(String.valueOf(leagueEventScoringData.getMostSelectedFwd()), ""))
                    .setMostSelectedCaptainName(playerNameMap.getOrDefault(String.valueOf(leagueEventScoringData.getMostSelectedCaptain()), ""));
            data.setScoringData(leagueEventScoringData);
            list.add(data);
        });
        // sort
        Map<Integer, Integer> rankMap = this.sortLeagueScoringRank(list);
        if (!CollectionUtils.isEmpty(rankMap)) {
            list.forEach(o -> o.setRank(rankMap.get(o.getOverallRank())));
        }
        return new TableData<>(list
                .stream()
                .sorted(Comparator.comparing(LeagueEventReportStatData::getRank))
                .collect(Collectors.toList())
        );
    }

    private Map<Integer, Integer> sortLeagueScoringRank(List<LeagueEventReportStatData> leagueEventReportStatDataList) {
        Map<Integer, Integer> rankMap = Maps.newHashMap();
        Map<Integer, Integer> rankCountMap = Maps.newLinkedHashMap();
        leagueEventReportStatDataList
                .stream()
                .sorted(Comparator.comparing(LeagueEventReportStatData::getOverallRank))
                .forEachOrdered(o -> {
                    int key = o.getOverallRank();
                    if (rankCountMap.containsKey(key)) {
                        rankCountMap.put(key, rankCountMap.get(key) + 1);
                    } else {
                        rankCountMap.put(key, 1);
                    }
                });
        int index = 1;
        for (int key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    // do not cache
    @Override
    public TableData<LeagueEventReportData> qryLeagueScoringEventReportList(int event, int leagueId, String leagueType) {
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .eq(LeagueEventReportEntity::getEvent, event));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return new TableData<>();
        }
        // prepare
        List<PlayerPickData> pickDataList = this.queryService.qryLeagueEventPickDataList(event, leagueId, leagueType);
        Table<Integer, Integer, List<EntryPickData>> eventEventPickTable = HashBasedTable.create(); // entry -> element_type -> List<EntryPickData>
        pickDataList.forEach(o -> {
            eventEventPickTable.put(o.getEntry(), Position.GKP.getElementType(), o.getGkps());
            eventEventPickTable.put(o.getEntry(), Position.DEF.getElementType(), o.getDefs());
            eventEventPickTable.put(o.getEntry(), Position.MID.getElementType(), o.getMids());
            eventEventPickTable.put(o.getEntry(), Position.FWD.getElementType(), o.getFwds());
            eventEventPickTable.put(o.getEntry(), Position.SUB.getElementType(), o.getSubs());
        });
        Multimap<Integer, EntryEventAutoSubsData> eventEventAutoSubMap = HashMultimap.create();
        this.queryService.qryLeagueEventAutoSubDataList(event, leagueId, leagueType).forEach(o -> eventEventAutoSubMap.put(o.getEntry(), o));
        Map<String, String> playerNameMap = this.queryService.qryPlayerWebNameMap();
        // collect
        List<LeagueEventReportData> list = Lists.newArrayList();
        leagueEventReportEntityList.forEach(o -> {
            int entry = o.getEntry();
            LeagueEventReportData data = new LeagueEventReportData();
            BeanUtil.copyProperties(o, data);
            if (!eventEventPickTable.containsRow(entry)) {
                return;
            }
            // pick list
            List<EntryPickData> entryEventPickList = Lists.newArrayList();
            entryEventPickList.addAll(Objects.requireNonNull(eventEventPickTable.get(entry, Position.GKP.getElementType())));
            entryEventPickList.addAll(Objects.requireNonNull(eventEventPickTable.get(entry, Position.DEF.getElementType())));
            entryEventPickList.addAll(Objects.requireNonNull(eventEventPickTable.get(entry, Position.MID.getElementType())));
            entryEventPickList.addAll(Objects.requireNonNull(eventEventPickTable.get(entry, Position.FWD.getElementType())));
            data.setEntryEventPickList(
                    entryEventPickList
                            .stream()
                            .sorted(Comparator.comparing(EntryPickData::getPosition))
                            .collect(Collectors.toList())
            );
            List<EntryPickData> entryEventBenchList = Lists.newArrayList(Objects.requireNonNull(eventEventPickTable.get(entry, Position.SUB.getElementType())));
            data.setEntryEventBenchList(
                    entryEventBenchList
                            .stream()
                            .sorted(Comparator.comparing(EntryPickData::getPosition))
                            .collect(Collectors.toList())
            );
            // scoring
            data
                    .setGkpPoints(this.calcEntryEventElementTypePoints(entry, Position.GKP.getElementType(), eventEventPickTable))
                    .setDefPoints(this.calcEntryEventElementTypePoints(entry, Position.DEF.getElementType(), eventEventPickTable))
                    .setMidPoints(this.calcEntryEventElementTypePoints(entry, Position.MID.getElementType(), eventEventPickTable))
                    .setFwdPoints(this.calcEntryEventElementTypePoints(entry, Position.FWD.getElementType(), eventEventPickTable))
                    .setCaptainPoints(o.getPlayedCaptain() == o.getCaptain() ? o.getCaptainPoints() : o.getViceCaptainPoints())
                    .setCaptainWebName(playerNameMap.getOrDefault(String.valueOf(data.getPlayedCaptain()), ""))
                    .setPlayedNum(
                            (int) Objects.requireNonNull(eventEventPickTable.get(entry, Position.GKP.getElementType()))
                                    .stream()
                                    .filter(pickData -> pickData.getMinutes() > 0)
                                    .count() +
                                    (int) Objects.requireNonNull(eventEventPickTable.get(entry, Position.DEF.getElementType()))
                                            .stream()
                                            .filter(pickData -> pickData.getMinutes() > 0)
                                            .count() +
                                    (int) Objects.requireNonNull(eventEventPickTable.get(entry, Position.MID.getElementType()))
                                            .stream()
                                            .filter(pickData -> pickData.getMinutes() > 0)
                                            .count() +
                                    (int) Objects.requireNonNull(eventEventPickTable.get(entry, Position.FWD.getElementType()))
                                            .stream()
                                            .filter(pickData -> pickData.getMinutes() > 0)
                                            .count()
                    )

                    .setFormation(
                            StringUtils.joinWith(
                                    "-",
                                    Objects.requireNonNull(eventEventPickTable.get(entry, Position.DEF.getElementType())).size(),
                                    Objects.requireNonNull(eventEventPickTable.get(entry, Position.MID.getElementType())).size(),
                                    Objects.requireNonNull(eventEventPickTable.get(entry, Position.FWD.getElementType())).size()
                            )
                    );
            // autoSubs
            List<EntryEventAutoSubsData> entryEventAutoSubsList = Lists.newArrayList();
            if (eventEventAutoSubMap.containsKey(entry) && !CollectionUtils.isEmpty(eventEventAutoSubMap.get(entry))) {
                entryEventAutoSubsList.addAll(eventEventAutoSubMap.get(entry));
            }
            data
                    .setEventAutoSubPoints(
                            entryEventAutoSubsList
                                    .stream()
                                    .mapToInt(EntryEventAutoSubsData::getElementInPoints)
                                    .sum()
                    )
                    .setAutoSubNum(eventEventAutoSubMap.containsKey(entry) ? eventEventAutoSubMap.get(entry).size() : 0)
                    .setEntryEventAutoSubsList(entryEventAutoSubsList);
            // other data
            data
                    .setEventAutoSubPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getEventAutoSubPoints(), data.getEventPoints()), 1))
                    .setCaptainPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getCaptainPoints(), data.getEventPoints()), 1))
                    .setGkpPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getGkpPoints(), data.getEventPoints()), 1))
                    .setDefPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getDefPoints(), data.getEventPoints()), 1))
                    .setMidPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getMidPoints(), data.getEventPoints()), 1))
                    .setFwdPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getFwdPoints(), data.getEventPoints()), 1));
            list.add(data);
        });
        // sort
        Map<Integer, Integer> rankMap = this.sortLeagueScoringEventRank(list);
        if (!CollectionUtils.isEmpty(rankMap)) {
            list.forEach(o -> o.setRank(rankMap.get(o.getEventPoints())));
        }
        return new TableData<>(list
                .stream()
                .sorted(Comparator.comparing(LeagueEventReportData::getRank))
                .collect(Collectors.toList())
        );
    }

    private int calcEntryEventElementTypePoints(int entry, int elementType, Table<Integer, Integer, List<EntryPickData>> eventEventPickTable) {
        if (!eventEventPickTable.contains(entry, elementType) || CollectionUtils.isEmpty(eventEventPickTable.get(entry, elementType))) {
            return 0;
        }
        return Objects.requireNonNull(eventEventPickTable.get(entry, elementType))
                .stream()
                .filter(o -> o.getElementType() == elementType)
                .mapToInt(EntryPickData::getPoints)
                .sum();
    }

    private Map<Integer, Integer> sortLeagueScoringEventRank(List<LeagueEventReportData> leagueEventReportDataList) {
        Map<Integer, Integer> rankMap = Maps.newHashMap();
        Map<Integer, Integer> rankCountMap = Maps.newLinkedHashMap();
        leagueEventReportDataList
                .stream()
                .sorted(Comparator.comparing(LeagueEventReportData::getEventPoints).reversed())
                .forEachOrdered(o -> {
                    int key = o.getEventPoints();
                    if (rankCountMap.containsKey(key)) {
                        rankCountMap.put(key, rankCountMap.get(key) + 1);
                    } else {
                        rankCountMap.put(key, 1);
                    }
                });
        int index = 1;
        for (int key : rankCountMap.keySet()) {
            rankMap.put(key, index);
            index += rankCountMap.get(key);
        }
        return rankMap;
    }

    @Cacheable(value = "qryEntryScoringEventReportList", key = "#leagueId+'::'+#leagueType+'::'+#entry", unless = "#result==null")
    @Override
    public TableData<LeagueEventReportData> qryEntryScoringEventReportList(int leagueId, String leagueType, int entry) {
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .eq(LeagueEventReportEntity::getEntry, entry)
                .orderByAsc(LeagueEventReportEntity::getEvent));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return new TableData<>();
        }
        // prepare
        Map<String, String> playerNameMap = this.queryService.qryPlayerWebNameMap();
        // collect
        List<CompletableFuture<LeagueEventReportData>> future = leagueEventReportEntityList.stream()
                .map(o -> CompletableFuture.supplyAsync(() -> this.collectEntryScoringEventReportList(o, playerNameMap), new ForkJoinPool(4)))
                .collect(Collectors.toList());
        return new TableData<>(
                future
                        .stream()
                        .map(CompletableFuture::join)
                        .sorted(Comparator.comparing(LeagueEventReportData::getEvent))
                        .collect(Collectors.toList())
        );
    }

    private LeagueEventReportData collectEntryScoringEventReportList(LeagueEventReportEntity o, Map<String, String> playerNameMap) {
        int entry = o.getEntry();
        int event = o.getEvent();
        LeagueEventReportData data = new LeagueEventReportData();
        BeanUtil.copyProperties(o, data);
        PlayerPickData pickData = this.queryService.qryEntryPickData(event, entry);
        if (pickData == null) {
            return data;
        }
        // pick list
        List<EntryPickData> entryEventPickList = Lists.newArrayList();
        entryEventPickList.addAll(pickData.getGkps());
        entryEventPickList.addAll(pickData.getDefs());
        entryEventPickList.addAll(pickData.getMids());
        entryEventPickList.addAll(pickData.getFwds());
        data.setEntryEventPickList(
                entryEventPickList
                        .stream()
                        .sorted(Comparator.comparing(EntryPickData::getPosition))
                        .collect(Collectors.toList())
        );
        List<EntryPickData> entryEventBenchList = Lists.newArrayList(pickData.getSubs());
        data.setEntryEventBenchList(
                entryEventBenchList
                        .stream()
                        .sorted(Comparator.comparing(EntryPickData::getPosition))
                        .collect(Collectors.toList())
        );
        // scoring
        data
                .setGkpPoints(
                        pickData.getGkps()
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                )
                .setDefPoints(
                        pickData.getDefs()
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                )
                .setMidPoints(
                        pickData.getMids()
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                )
                .setFwdPoints(
                        pickData.getFwds()
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                )
                .setCaptainPoints(o.getPlayedCaptain() == o.getCaptain() ? o.getCaptainPoints() : o.getViceCaptainPoints())
                .setCaptainWebName(playerNameMap.getOrDefault(String.valueOf(data.getPlayedCaptain()), ""))
                .setPlayedNum(
                        (int) pickData.getGkps()
                                .stream()
                                .filter(pick -> pick.getMinutes() > 0)
                                .count() +
                                (int) pickData.getDefs()
                                        .stream()
                                        .filter(pick -> pick.getMinutes() > 0)
                                        .count() +
                                (int) pickData.getMids()
                                        .stream()
                                        .filter(pick -> pick.getMinutes() > 0)
                                        .count() +
                                (int) pickData.getFwds()
                                        .stream()
                                        .filter(pick -> pick.getMinutes() > 0)
                                        .count()
                )
                .setFormation(pickData.getFormation());
        // autoSubs
        List<EntryEventAutoSubsData> entryEventAutoSubsList = this.queryService.qryEntryAutoSubDataList(event, entry);
        data
                .setEventAutoSubPoints(
                        entryEventAutoSubsList
                                .stream()
                                .mapToInt(EntryEventAutoSubsData::getElementInPoints)
                                .sum()
                )
                .setAutoSubNum(entryEventAutoSubsList.size())
                .setEntryEventAutoSubsList(entryEventAutoSubsList);
        // other data
        data
                .setEventAutoSubPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getEventAutoSubPoints(), data.getEventPoints()), 1))
                .setCaptainPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getCaptainPoints(), data.getEventPoints()), 1))
                .setGkpPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getGkpPoints(), data.getEventPoints()), 1))
                .setDefPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getDefPoints(), data.getEventPoints()), 1))
                .setMidPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getMidPoints(), data.getEventPoints()), 1))
                .setFwdPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getFwdPoints(), data.getEventPoints()), 1));
        return data;
    }

    private LinkedHashMap<String, String> collectSelectedMap(List<Integer> elementList, int teamSize, int limit, Map<String, PlayerEntity> playerMap) {
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
                map.put(playerMap.get(String.valueOf(k)).getWebName(), NumberUtil.formatPercent(NumberUtil.div(v.intValue(), teamSize), 1)));
        return map;
    }

    private boolean setSearchTotal(long current) {
        return current == 1;
    }

    @Override
    public TableData<ScoutData> qryEventScoutPickList(int event) {
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        List<ScoutData> list = Lists.newArrayList();
        this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
                .eq(ScoutEntity::getEvent, event))
                .forEach(o ->
                        list.add(new ScoutData()
                                .setEvent(event)
                                .setEntry(o.getEntry())
                                .setScoutName(o.getScoutName())
                                .setGkpName(this.qryPlayerWebNameByElement(o.getGkp()))
                                .setGkpTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getGkpTeamId()), ""))
                                .setDefName(this.qryPlayerWebNameByElement(o.getDef()))
                                .setDefTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getDefTeamId()), ""))
                                .setMidName(this.qryPlayerWebNameByElement(o.getMid()))
                                .setMidTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getMidTeamId()), ""))
                                .setFwdName(this.qryPlayerWebNameByElement(o.getFwd()))
                                .setFwdTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getFwdTeamId()), ""))
                                .setCaptainName(this.qryPlayerWebNameByElement(o.getCaptain()))
                                .setCaptainTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getCaptainTeamId()), ""))
                                .setReason(o.getReason())
                        ));
        return new TableData<>(list
                .stream()
                .sorted(Comparator.comparing(ScoutData::getEntry))
                .collect(Collectors.toList()));
    }

    private String qryPlayerWebNameByElement(int element) {
        PlayerEntity playerEntity = this.queryService.getPlayerByElement(element);
        return playerEntity == null ? "" : playerEntity.getWebName();
    }

    @Cacheable(value = "qryEventScoutList", key = "#event")
    @Override
    public TableData<ScoutData> qryEventScoutList(int event) {
        if (event == 0) {
            return this.qrySeasonScoutList();
        }
        Map<String, String> teamShortNameMap = this.queryService.getTeamShortNameMap();
        List<ScoutData> list = Lists.newArrayList();
        this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
                .eq(ScoutEntity::getEvent, event))
                .forEach(o ->
                        list.add(new ScoutData()
                                .setEvent(event)
                                .setEntry(o.getEntry())
                                .setScoutName(o.getScoutName())
                                .setGkpName(this.qryPlayerWebNameByElement(o.getGkp()))
                                .setGkpTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getGkpTeamId()), ""))
                                .setGkpPoints(o.getGkpPoints())
                                .setDefName(this.qryPlayerWebNameByElement(o.getDef()))
                                .setDefTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getDefTeamId()), ""))
                                .setDefPoints(o.getDefPoints())
                                .setMidName(this.qryPlayerWebNameByElement(o.getMid()))
                                .setMidTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getMidTeamId()), ""))
                                .setMidPoints(o.getMidPoints())
                                .setFwdName(this.qryPlayerWebNameByElement(o.getFwd()))
                                .setFwdTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getFwdTeamId()), ""))
                                .setFwdPoints(o.getFwdPoints())
                                .setCaptainName(this.qryPlayerWebNameByElement(o.getCaptain()))
                                .setCaptainTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getCaptainTeamId()), ""))
                                .setCaptainPoints(o.getCaptainPoints())
                                .setEventPoints(o.getEventPoints())
                                .setTotalPoints(o.getTotalPoints())
                        ));
        return new TableData<>(list
                .stream()
                .sorted(Comparator.comparing(ScoutData::getEventPoints).reversed())
                .collect(Collectors.toList()));
    }

    private TableData<ScoutData> qrySeasonScoutList() {
        Multimap<Integer, ScoutEntity> scoutMap = HashMultimap.create();
        this.scoutService.list(new QueryWrapper<ScoutEntity>().lambda()
                .gt(ScoutEntity::getEventPoints, 0))
                .forEach(o -> scoutMap.put(o.getEntry(), o));
        List<ScoutData> list = Lists.newArrayList();
        scoutMap.keySet().forEach(entry -> {
            Collection<ScoutEntity> scoutEntities = scoutMap.get(entry);
            ScoutEntity last = scoutEntities
                    .stream()
                    .max(Comparator.comparing(ScoutEntity::getEvent))
                    .orElse(new ScoutEntity());
            ScoutData scoutData = new ScoutData()
                    .setEntry(entry)
                    .setScoutName(last.getScoutName())
                    .setGkpPoints(
                            scoutEntities
                                    .stream()
                                    .mapToInt(ScoutEntity::getGkpPoints)
                                    .sum()
                    )
                    .setDefPoints(
                            scoutEntities
                                    .stream()
                                    .mapToInt(ScoutEntity::getDefPoints)
                                    .sum()
                    )
                    .setMidPoints(
                            scoutEntities
                                    .stream()
                                    .mapToInt(ScoutEntity::getMidPoints)
                                    .sum()
                    )
                    .setFwdPoints(
                            scoutEntities
                                    .stream()
                                    .mapToInt(ScoutEntity::getFwdPoints)
                                    .sum()
                    )
                    .setCaptainPoints(
                            scoutEntities
                                    .stream()
                                    .mapToInt(ScoutEntity::getCaptainPoints)
                                    .sum()
                    )
                    .setTotalPoints(last.getTotalPoints());
            list.add(scoutData);
        });
        return new TableData<>(list
                .stream()
                .sorted(Comparator.comparing(ScoutData::getTotalPoints).reversed())
                .collect(Collectors.toList()));
    }

}

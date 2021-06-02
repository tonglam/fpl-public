package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventAutoSubsData;
import com.tong.fpl.domain.letletme.entry.EntryEventTransfersData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.summary.entry.*;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonCaptainData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonEntryData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonScoreData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonSummaryData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.ISummaryService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2021/6/2
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SummaryServiceImpl implements ISummaryService {

    private final IApiQueryService apiQueryService;
    private final PlayerService playerService;
    private final EventLiveService eventLiveService;
    private final EntryInfoService entryInfoService;
    private final EntryEventResultService entryEventResultService;
    private final EntryEventTransfersService entryEventTransfersService;
    private final LeagueEventReportService leagueEventReportService;

    /**
     * @implNote entry
     */

    @Cacheable(
            value = "api::qryEntrySeasonInfo",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntrySeasonInfoData qryEntrySeasonInfo(int entry) {
        // prepare
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        if (entryInfoEntity == null) {
            return new EntrySeasonInfoData();
        }
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventResultEntityList)) {
            return new EntrySeasonInfoData();
        }
        return new EntrySeasonInfoData()
                .setEntry(entry)
                .setEntryName(entryInfoEntity.getEntryName())
                .setPlayerName(entryInfoEntity.getPlayerName())
                .setOverallPoints(entryInfoEntity.getOverallPoints())
                .setOverallRank(entryInfoEntity.getOverallRank())
                .setTotalTransfers(entryInfoEntity.getTotalTransfers())
                .setTotalTransfersCost(
                        entryEventResultEntityList
                                .stream()
                                .mapToInt(EntryEventResultEntity::getEventTransfersCost)
                                .sum()
                )
                .setTotalBenchPoints(
                        entryEventResultEntityList
                                .stream()
                                .mapToInt(EntryEventResultEntity::getEventBenchPoints)
                                .sum()
                )
                .setTotalAutoSubsPoints(
                        entryEventResultEntityList
                                .stream()
                                .mapToInt(EntryEventResultEntity::getEventAutoSubPoints)
                                .sum()
                )
                .setValue(entryInfoEntity.getTeamValue() / 10.0)
                .setBank(entryInfoEntity.getBank() / 10.0)
                .setTeamValue((entryInfoEntity.getTeamValue() - entryInfoEntity.getBank()) / 10.0);
    }

    @Cacheable(
            value = "api::qryEntrySeasonSummary",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntrySeasonSummaryData qryEntrySeasonSummary(int entry) {
        EntrySeasonSummaryData data = new EntrySeasonSummaryData();
        // prepare
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        if (entryInfoEntity == null) {
            return data;
        }
        Map<Integer, String> webNameMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
        if (CollectionUtils.isEmpty(webNameMap)) {
            return data;
        }
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventResultEntityList)) {
            return data;
        }
        data
                .setEntry(entry)
                .setEntryName(entryInfoEntity.getEntryName())
                .setPlayerName(entryInfoEntity.getPlayerName());
        // entry_event_result
        entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getEventPoints))
                .ifPresent(o ->
                        data
                                .setHighestScore(o.getEventPoints())
                                .setHighestEvent(o.getEvent())
                );
        entryEventResultEntityList
                .stream()
                .min(Comparator.comparing(EntryEventResultEntity::getEventPoints))
                .ifPresent(o ->
                        data
                                .setLowestScore(o.getEventPoints())
                                .setLowestEvent(o.getEvent())
                );
        entryEventResultEntityList
                .stream()
                .min(Comparator.comparing(EntryEventResultEntity::getOverallRank))
                .ifPresent(o ->
                        data
                                .setHighestOverallRank(o.getOverallRank())
                                .setHighestOverallRankEvent(o.getEvent())
                );
        entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getOverallRank))
                .ifPresent(o ->
                        data
                                .setLowestOverallRank(o.getOverallRank())
                                .setLowestOverallRankEvent(o.getEvent())
                );
        // average
        Map<String, Integer> averageMap = this.apiQueryService.qryEventAverageScore();
        data
                .setBelowAverages(
                        entryEventResultEntityList
                                .stream()
                                .filter(o -> o.getEventPoints() < averageMap.getOrDefault(String.valueOf(o.getEvent()), 0))
                                .sorted(Comparator.comparing(EntryEventResultEntity::getEvent))
                                .map(o ->
                                        new EntryBelowAverageData()
                                                .setEvent(o.getEvent())
                                                .setPoints(o.getEventPoints())
                                                .setAveragePoints(averageMap.getOrDefault(String.valueOf(o.getEvent()), 0))
                                )
                                .collect(Collectors.toList())
                );
        // above hundred
        data.setAboveHundred(
                entryEventResultEntityList
                        .stream()
                        .filter(o -> o.getEventPoints() >= 100)
                        .map(o ->
                                new EntryAboveHundredData()
                                        .setEvent(o.getEvent())
                                        .setEntry(o.getEntry())
                                        .setEntryName(entryInfoEntity.getEntryName())
                                        .setPlayerName(entryInfoEntity.getPlayerName())
                                        .setPoint(o.getEventPoints())
                                        .setTransfers(o.getEventTransfers())
                                        .setCost(o.getEventTransfersCost())
                                        .setNetPoints(o.getEventNetPoints())
                                        .setChip(o.getEventChip())
                        )
                        .collect(Collectors.toList())
        );
        // bench
        entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getEventBenchPoints))
                .ifPresent(o -> {
                    List<EntryPickData> pickList = JsonUtils.json2Collection(o.getEventPicks(), List.class, EntryPickData.class);
                    if (CollectionUtils.isEmpty(pickList)) {
                        return;
                    }
                    data
                            .setHighestBench(
                                    pickList
                                            .stream()
                                            .filter(i -> i.getPosition() > 11)
                                            .map(i -> i.setWebName(webNameMap.getOrDefault(i.getElement(), "")))
                                            .collect(Collectors.toList())
                            )
                            .setHighestBenchPoints(o.getEventBenchPoints())
                            .setHighestBenchPointsEvent(o.getEvent());
                });
        // autoSub
        entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getEventAutoSubPoints))
                .ifPresent(o -> {
                            List<EntryEventAutoSubsData> list = JsonUtils.json2Collection(o.getEventAutoSubs(), List.class, EntryEventAutoSubsData.class);
                            if (CollectionUtils.isEmpty(list)) {
                                return;
                            }
                            list.forEach(i ->
                                    i
                                            .setEvent(o.getEvent())
                                            .setElementInWebName(webNameMap.getOrDefault(i.getElementIn(), ""))
                                            .setElementOutWebName(webNameMap.getOrDefault(i.getElementOut(), ""))
                            );
                            data
                                    .setHighestAutoSubsPointsEvent(o.getEvent())
                                    .setHighestAutoSubs(list);
                        }
                );
        data.setHighestAutoSubsPoints(
                data.getHighestAutoSubs()
                        .stream()
                        .mapToInt(EntryEventAutoSubsData::getElementInPoints)
                        .sum() -
                        data.getHighestAutoSubs()
                                .stream()
                                .mapToInt(EntryEventAutoSubsData::getElementOutPoints)
                                .sum()
        );
        // chips
        data.setChips(
                entryEventResultEntityList
                        .stream()
                        .filter(o ->
                                StringUtils.equalsIgnoreCase(Chip.TC.getValue(), o.getEventChip()) ||
                                        StringUtils.equalsIgnoreCase(Chip.BB.getValue(), o.getEventChip()) ||
                                        StringUtils.equalsIgnoreCase(Chip.FH.getValue(), o.getEventChip())
                        )
                        .map(this::setEntryChipResult)
                        .collect(Collectors.toList())
        );
        return data;
    }

    private EntryChipData setEntryChipResult(EntryEventResultEntity entryEventResultEntity) {
        int profit = 0;
        // prepare
        int event = entryEventResultEntity.getEvent();
        Chip chip = Chip.getChipFromValue(entryEventResultEntity.getEventChip());
        Map<Integer, Integer> pointsMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, EventLiveEntity::getTotalPoints));
        // calc profit
        switch (chip) {
            case TC: {
                profit = entryEventResultEntity.getCaptainPoints();
                break;
            }
            case BB: {
                List<EntryPickData> pickList = JsonUtils.json2Collection(entryEventResultEntity.getEventPicks(), List.class, EntryPickData.class);
                if (CollectionUtils.isEmpty(pickList)) {
                    break;
                }
                profit = pickList
                        .stream()
                        .filter(o -> o.getPosition() > 11)
                        .mapToInt(o -> pointsMap.getOrDefault(o.getElement(), 0))
                        .sum();
                break;
            }
            case FH: {
                EntryEventResultEntity lastEventEntity = this.entryEventResultService.getOne(new QueryWrapper<EntryEventResultEntity>().lambda()
                        .eq(EntryEventResultEntity::getEvent, event - 1)
                        .eq(EntryEventResultEntity::getEntry, entryEventResultEntity.getEntry()));
                if (lastEventEntity == null) {
                    break;
                }
                List<EntryPickData> pickList = JsonUtils.json2Collection(lastEventEntity.getEventPicks(), List.class, EntryPickData.class);
                if (CollectionUtils.isEmpty(pickList)) {
                    break;
                }
                profit = entryEventResultEntity.getEventPoints() -
                        pickList
                                .stream()
                                .filter(o -> o.getPosition() <= 11)
                                .mapToInt(o -> pointsMap.getOrDefault(o.getElement(), 0))
                                .sum();
                break;
            }
        }
        return new EntryChipData()
                .setEvent(event)
                .setName(chip.name())
                .setEventPoints(entryEventResultEntity.getEventPoints())
                .setProfit(profit);
    }

    @Cacheable(
            value = "api::qryEntrySeasonCaptain",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntrySeasonCaptainData qryEntrySeasonCaptain(int entry) {
        EntrySeasonCaptainData data = new EntrySeasonCaptainData();
        // prepare
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        if (entryInfoEntity == null) {
            return data;
        }
        Map<String, String> shortNameMap = this.apiQueryService.getTeamShortNameMap();
        if (CollectionUtils.isEmpty(shortNameMap)) {
            return data;
        }
        Map<Integer, String> webNameMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
        if (CollectionUtils.isEmpty(webNameMap)) {
            return data;
        }
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventResultEntityList)) {
            return data;
        }
        data
                .setEntry(entry)
                .setEntryName(entryInfoEntity.getEntryName())
                .setPlayerName(entryInfoEntity.getPlayerName());
        // collect
        Map<Integer, Integer> viceCaptainMap = Maps.newHashMap(); // event -> vice
        entryEventResultEntityList.forEach(o -> {
            List<EntryPickData> pickList = JsonUtils.json2Collection(o.getEventPicks(), List.class, EntryPickData.class);
            if (CollectionUtils.isEmpty(pickList)) {
                return;
            }
            pickList
                    .stream()
                    .filter(EntryPickData::isViceCaptain)
                    .map(EntryPickData::getElement)
                    .forEach(i -> viceCaptainMap.put(o.getEvent(), i));
        });
        data
                .setTotalPoints(
                        entryEventResultEntityList
                                .stream()
                                .mapToInt(o -> {
                                    if (StringUtils.equals(Chip.TC.getValue(), o.getEventChip())) {
                                        return 3 * o.getCaptainPoints();
                                    }
                                    return 2 * o.getCaptainPoints();
                                })
                                .sum()
                )
                .setViceTotalPoints(
                        entryEventResultEntityList
                                .stream()
                                .filter(o -> o.getPlayedCaptain().equals(viceCaptainMap.getOrDefault(o.getEvent(), 0)))
                                .mapToInt(o -> {
                                    if (StringUtils.equals(Chip.TC.getValue(), o.getEventChip())) {
                                        return 3 * o.getCaptainPoints();
                                    }
                                    return 2 * o.getCaptainPoints();
                                })
                                .sum()
                );
        // max
        entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getCaptainPoints))
                .ifPresent(o ->
                        data
                                .setMostPointsEvent(o.getEvent())
                                .setMostPoints(o.getCaptainPoints())
                                .setMostPointsWebName(webNameMap.getOrDefault(o.getPlayedCaptain(), ""))
                );
        // min
        entryEventResultEntityList
                .stream()
                .min(Comparator.comparing(EntryEventResultEntity::getCaptainPoints))
                .ifPresent(o ->
                        data
                                .setLeastPointsEvent(o.getEvent())
                                .setLeastPoints(o.getCaptainPoints())
                                .setLeastPointsWebName(webNameMap.getOrDefault(o.getPlayedCaptain(), ""))
                );
        // tc
        entryEventResultEntityList
                .stream()
                .filter(o -> StringUtils.equals(Chip.TC.getValue(), o.getEventChip()))
                .findFirst()
                .ifPresent(o ->
                        data
                                .setTcEvent(o.getEvent())
                                .setTcPoints(o.getCaptainPoints())
                                .setTcPointsWebName(webNameMap.getOrDefault(o.getPlayedCaptain(), ""))
                );
        // most selected
        Map<Integer, Long> countMap = entryEventResultEntityList.
                stream()
                .collect(Collectors.groupingBy(EntryEventResultEntity::getPlayedCaptain, Collectors.counting()));
        data.setMostSelected(
                countMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                        .limit(3)
                        .map(o ->
                                new EntrySelectedCaptainData()
                                        .setElement(o.getKey())
                                        .setWebName(webNameMap.getOrDefault(o.getKey(), ""))
                                        .setTimes(o.getValue().intValue())
                                        .setTotalPoints(
                                                entryEventResultEntityList
                                                        .stream()
                                                        .filter(i -> i.getPlayedCaptain().equals(o.getKey()))
                                                        .mapToInt(i -> {
                                                            if (StringUtils.equalsIgnoreCase(Chip.TC.getValue(), i.getEventChip())) {
                                                                return 3 * i.getCaptainPoints();
                                                            } else {
                                                                return 2 * i.getCaptainPoints();
                                                            }
                                                        })
                                                        .sum()
                                        )
                        )
                        .collect(Collectors.toList())
        );
        int overallPoints = entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getOverallPoints))
                .map(EntryEventResultEntity::getOverallPoints)
                .orElse(0);
        data
                .setTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getTotalPoints(), overallPoints), 2))
                .setViceTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getViceTotalPoints(), overallPoints), 2));
        return data;
    }

    @Cacheable(
            value = "api::qryEntrySeasonTransfers",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntrySeasonTransfersData qryEntrySeasonTransfers(int entry) {
        EntrySeasonTransfersData data = new EntrySeasonTransfersData();
        // prepare
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        if (entryInfoEntity == null) {
            return data;
        }
        Map<String, String> shortNameMap = this.apiQueryService.getTeamShortNameMap();
        if (CollectionUtils.isEmpty(shortNameMap)) {
            return data;
        }
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        if (CollectionUtils.isEmpty(playerMap)) {
            return data;
        }
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventResultEntityList)) {
            return data;
        }
        List<EntryEventTransfersEntity> entryEventTransfersEntityList = this.entryEventTransfersService.list(new QueryWrapper<EntryEventTransfersEntity>().lambda()
                .eq(EntryEventTransfersEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventTransfersEntityList)) {
            return data;
        }
        data
                .setEntry(entry)
                .setEntryName(entryInfoEntity.getEntryName())
                .setPlayerName(entryInfoEntity.getPlayerName());
        // chip event list
        List<Integer> chipEventList = entryEventResultEntityList
                .stream()
                .filter(o -> StringUtils.equalsIgnoreCase(Chip.WC.getValue(), o.getEventChip()) || StringUtils.equalsIgnoreCase(Chip.FH.getValue(), o.getEventChip()))
                .map(EntryEventResultEntity::getEvent)
                .collect(Collectors.toList());
        // best transfers
        int bestProfit = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .mapToInt(o -> o.getElementInPoints() - o.getElementOutPoints())
                .max()
                .orElse(0);
        List<EntryEventTransfersData> bestTransfersInList = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .filter(EntryEventTransfersEntity::getElementInPlayed)
                .filter(o -> (o.getElementInPoints() - o.getElementOutPoints()) == bestProfit)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data.setBestTransfers(bestTransfersInList);
        // worst transfers
        int worstProfit = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .mapToInt(o -> o.getElementInPoints() - o.getElementOutPoints())
                .min()
                .orElse(0);
        List<EntryEventTransfersData> worstTransfersInList = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .filter(o -> (o.getElementInPoints() - o.getElementOutPoints()) == worstProfit)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data.setWorstTransfers(worstTransfersInList);
        // most transfers in
        Map<Integer, Long> mostTransfersInCountMap = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .collect(Collectors.groupingBy(EntryEventTransfersEntity::getElementIn, Collectors.counting()));
        int mostTransferInElement = mostTransfersInCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(1)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);
        List<EntryEventTransfersData> mostTransfersInList = entryEventTransfersEntityList
                .stream()
                .filter(o -> o.getElementIn() == mostTransferInElement)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data
                .setMostTransfersInWebName(mostTransfersInList.get(0).getElementInWebName())
                .setMostTransfersIn(mostTransfersInList);
        // most transfers out
        Map<Integer, Long> mostTransfersOutCountMap = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .collect(Collectors.groupingBy(EntryEventTransfersEntity::getElementOut, Collectors.counting()));
        int mostTransferOutElement = mostTransfersOutCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(1)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);
        List<EntryEventTransfersData> mostTransfersOutList = entryEventTransfersEntityList
                .stream()
                .filter(o -> o.getElementOut() == mostTransferOutElement)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data
                .setMostTransfersOutWebName(mostTransfersOutList.get(0).getElementOutWebName())
                .setMostTransfersOut(mostTransfersOutList);
        // negative transfers in points
        List<EntryEventTransfersData> negativeTransfersInPointsList = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .filter(o -> o.getElementInPoints() < 0)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data.setNegativeTransferInPoints(negativeTransfersInPointsList);
        // most transfers event
        Map<Integer, Long> mostTransfersCountMap = entryEventTransfersEntityList.
                stream()
                .filter(o -> !chipEventList.contains(o.getEvent()))
                .collect(Collectors.groupingBy(EntryEventTransfersEntity::getEvent, Collectors.counting()));
        int mostTransfersEvent = mostTransfersCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(1)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(0);
        int mostTransfersCost = entryEventResultEntityList
                .stream()
                .filter(o -> o.getEvent() == mostTransfersEvent)
                .map(EntryEventResultEntity::getEventTransfersCost)
                .findFirst()
                .orElse(0);
        List<EntryEventTransfersData> mostTransfersList = entryEventTransfersEntityList
                .stream()
                .filter(o -> o.getEvent() == mostTransfersEvent)
                .map(o -> this.initEntryEventTransfersData(o, shortNameMap, playerMap))
                .collect(Collectors.toList());
        data
                .setMostTransfersEvent(mostTransfersList.get(0).getEvent())
                .setMostTransfersCost(mostTransfersCost)
                .setMostTransfersProfit(
                        mostTransfersList
                                .stream()
                                .mapToInt(o -> o.getElementInPoints() - o.getElementOutPoints())
                                .sum()
                )
                .setMostTransfers(mostTransfersList);
        // transfersCost
        data.setTransfersCost(
                entryEventResultEntityList
                        .stream()
                        .filter(o -> o.getEventTransfersCost() > 0)
                        .map(o -> {
                            EntryTransfersCostData entryTransfersCostData = new EntryTransfersCostData()
                                    .setEvent(o.getEvent())
                                    .setTransfers(o.getEventTransfers())
                                    .setPoints(o.getEventPoints())
                                    .setCost(o.getEventTransfersCost())
                                    .setNetPoints(o.getEventNetPoints())
                                    .setTransfersList(
                                            entryEventTransfersEntityList
                                                    .stream()
                                                    .filter(i -> i.getEvent().equals(o.getEvent()))
                                                    .map(i -> this.initEntryEventTransfersData(i, shortNameMap, playerMap))
                                                    .collect(Collectors.toList())
                                    );
                            entryTransfersCostData
                                    .setProfit(
                                            entryTransfersCostData.getTransfersList()
                                                    .stream()
                                                    .mapToInt(i -> i.getElementInPoints() - i.getElementOutPoints())
                                                    .sum()
                                    );
                            return entryTransfersCostData;
                        })
                        .collect(Collectors.toList())
        );
        return data;

    }

    private EntryEventTransfersData initEntryEventTransfersData(EntryEventTransfersEntity entryEventTransfersEntity, Map<String, String> shortNameMap, Map<Integer, PlayerEntity> playerMap) {
        PlayerEntity elementInEntity = playerMap.get(entryEventTransfersEntity.getElementIn());
        PlayerEntity elementOutEntity = playerMap.get(entryEventTransfersEntity.getElementOut());
        return BeanUtil.copyProperties(entryEventTransfersEntity, EntryEventTransfersData.class)
                .setElementInWebName(elementInEntity.getWebName())
                .setElementInTeamId(elementInEntity.getTeamId())
                .setElementInTeamShortName(shortNameMap.getOrDefault(String.valueOf(elementInEntity.getTeamId()), ""))
                .setElementInCost(entryEventTransfersEntity.getElementInCost() / 10.0)
                .setElementOutWebName(elementOutEntity.getWebName())
                .setElementOutTeamId(elementOutEntity.getTeamId())
                .setElementOutTeamShortName(shortNameMap.getOrDefault(String.valueOf(elementOutEntity.getTeamId()), ""))
                .setElementOutCost(entryEventTransfersEntity.getElementOutCost() / 10.0);
    }

    @Cacheable(
            value = "api::qryEntrySeasonScore",
            key = "#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.entry eq 0"
    )
    @Override
    public EntrySeasonScoreData qryEntrySeasonScore(int entry) {
        EntrySeasonScoreData data = new EntrySeasonScoreData();
        // prepare
        EntryInfoEntity entryInfoEntity = this.entryInfoService.getById(entry);
        if (entryInfoEntity == null) {
            return data;
        }
        Map<String, String> shortNameMap = this.apiQueryService.getTeamShortNameMap();
        if (CollectionUtils.isEmpty(shortNameMap)) {
            return data;
        }
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        if (CollectionUtils.isEmpty(playerMap)) {
            return data;
        }
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventResultEntityList)) {
            return data;
        }
        // collect event score
        Table<Integer, Integer, List<EntryPickData>> pickTable = HashBasedTable.create(); // elementType -> event -> dataList
        entryEventResultEntityList.forEach(o -> {
            List<EntryPickData> pickList = JsonUtils.json2Collection(o.getEventPicks(), List.class, EntryPickData.class);
            if (CollectionUtils.isEmpty(pickList)) {
                return;
            }
            int event = o.getEvent();
            pickList.forEach(pick -> {
                if (!StringUtils.equalsIgnoreCase(Chip.BB.getValue(), o.getEventChip()) && pick.getPosition() > 11) {
                    return;
                }
                PlayerEntity playerEntity = playerMap.get(pick.getElement());
                if (playerEntity == null) {
                    return;
                }
                int elementType = playerEntity.getElementType();
                List<EntryPickData> list = Lists.newArrayList();
                if (pickTable.contains(elementType, event)) {
                    list = pickTable.get(elementType, event);
                }
                if (CollectionUtils.isEmpty(list)) {
                    list = Lists.newArrayList();
                }
                pick
                        .setEvent(event)
                        .setElementType(elementType)
                        .setElementTypeName(Position.getNameFromElementType(elementType))
                        .setWebName(playerEntity.getWebName())
                        .setTeamId(playerEntity.getTeamId())
                        .setTeamShortName(shortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""));
                list.add(pick);
                pickTable.put(elementType, event, list);
            });
        });
        data
                .setEntry(entry)
                .setEntryName(entryInfoEntity.getEntryName())
                .setPlayerName(entryInfoEntity.getPlayerName())
                .setOverallPoints(entryInfoEntity.getOverallPoints());
        // gkp
        List<EntryPickData> gkpPickList = Lists.newArrayList();
        pickTable.row(Position.GKP.getElementType()).values().forEach(gkpPickList::addAll);
        data
                .setGkpTotalPoints(
                        gkpPickList
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                )
                .setGkpTotalNum(
                        (int) gkpPickList
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                );
        data.setGkpTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getGkpTotalPoints(), data.getOverallPoints()), 2));
        Map<String, Long> mostSelectedGkpCountMap = gkpPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(o.getElement()).getWebName(), Collectors.counting()));
        data.setMostSelectedGkp(
                mostSelectedGkpCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(2)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        // def
        List<EntryPickData> defPickList = Lists.newArrayList();
        pickTable.row(Position.DEF.getElementType()).values().forEach(defPickList::addAll);
        data
                .setDefTotalPoints(
                        defPickList
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                )
                .setDefTotalNum(
                        (int) defPickList
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                );
        data.setDefTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getDefTotalPoints(), data.getOverallPoints()), 2));
        Map<String, Long> mostSelectedDefCountMap = defPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(o.getElement()).getWebName(), Collectors.counting()));
        data.setMostSelectedDef(
                mostSelectedDefCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        // mid
        List<EntryPickData> midPickList = Lists.newArrayList();
        pickTable.row(Position.MID.getElementType()).values().forEach(midPickList::addAll);
        data
                .setMidTotalPoints(
                        midPickList
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                )
                .setMidTotalNum(
                        (int) midPickList
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                );
        data.setMidTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getMidTotalPoints(), data.getOverallPoints()), 2));
        Map<String, Long> mostSelectedMidCountMap = midPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(o.getElement()).getWebName(), Collectors.counting()));
        data.setMostSelectedMid(
                mostSelectedMidCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        // fwd
        List<EntryPickData> fwdPickList = Lists.newArrayList();
        pickTable.row(Position.FWD.getElementType()).values().forEach(fwdPickList::addAll);
        data
                .setFwdTotalPoints(
                        fwdPickList
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                )
                .setFwdTotalNum(
                        (int) fwdPickList
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                );
        data.setFwdTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getFwdTotalPoints(), data.getOverallPoints()), 2));
        Map<String, Long> mostSelectedFwdCountMap = fwdPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(o.getElement()).getWebName(), Collectors.counting()));
        data.setMostSelectedFwd(
                mostSelectedFwdCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(3)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        // formation
        List<String> formationList = pickTable.columnMap().values()
                .stream()
                .map(o -> StringUtils.joinWith("-", o.get(2).size(), o.get(3).size(), o.get(4).size()))
                .collect(Collectors.toList());
        Map<String, Long> mostSelectedFormationCountMap = formationList.
                stream()
                .collect(Collectors.groupingBy(String::toString, Collectors.counting()));
        data.setMostSelectedFormation(
                mostSelectedFormationCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(3)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        return data;
    }

    /**
     * @implNote league
     */

    @Cacheable(
            value = "api::qryLeagueSeasonSummary",
            key = "#leagueId+'::'+#leagueType",
            cacheManager = "apiCacheManager",
            unless = "#result.leagueId eq 0"
    )
    @Override
    public LeagueSeasonSummaryData qryLeagueSeasonSummary(int leagueId, String leagueType) {
        LeagueSeasonSummaryData data = new LeagueSeasonSummaryData();
        // prepare
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .ne(LeagueEventReportEntity::getEventPoints, 0)
                .orderByAsc(LeagueEventReportEntity::getEvent));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return data;
        }
        Map<Integer, EntrySeasonInfoData> map = Maps.newHashMap();
        leagueEventReportEntityList.forEach(o -> {
            EntrySeasonInfoData entrySeasonInfoData = this.initLeagueEntryInfoData(o, map);
            if (entrySeasonInfoData != null) {
                map.put(o.getEntry(), entrySeasonInfoData);
            }
        });
        if (CollectionUtils.isEmpty(map)) {
            return data;
        }
        // league info
        LeagueEventReportEntity leagueEventReportEntity = leagueEventReportEntityList
                .stream()
                .max(Comparator.comparing(LeagueEventReportEntity::getEvent))
                .orElse(null);
        if (leagueEventReportEntity == null) {
            return data;
        }
        data
                .setLeagueId(leagueId)
                .setLeagueType(leagueType)
                .setLeagueName(leagueEventReportEntity.getLeagueName());
        // rank
        List<EntrySeasonInfoData> topRankList = map.values()
                .stream()
                .sorted(Comparator.comparing(EntrySeasonInfoData::getOverallRank))
                .limit(5)
                .collect(Collectors.toList());
        data
                .setAverageOverallPoints(
                        NumberUtil.round(
                                map.values()
                                        .stream()
                                        .mapToDouble(EntrySeasonInfoData::getOverallPoints)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setTopAverageOverallPoints(
                        NumberUtil.round(
                                topRankList
                                        .stream()
                                        .mapToDouble(EntrySeasonInfoData::getOverallPoints)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setTopRank(topRankList);
        // value
        List<EntrySeasonInfoData> topValueList = map.values()
                .stream()
                .sorted(Comparator.comparing(EntrySeasonInfoData::getTeamValue).reversed())
                .limit(5)
                .collect(Collectors.toList());
        data
                .setAverageValue(
                        NumberUtil.round(
                                map.values()
                                        .stream()
                                        .mapToDouble(EntrySeasonInfoData::getTeamValue)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setTopAverageValue(
                        NumberUtil.round(
                                topValueList
                                        .stream()
                                        .mapToDouble(EntrySeasonInfoData::getTeamValue)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setTopValue(topValueList);
        // cost
        List<EntrySeasonInfoData> topCostList = map.values()
                .stream()
                .sorted(Comparator.comparing(EntrySeasonInfoData::getTotalTransfersCost).reversed())
                .limit(5)
                .collect(Collectors.toList());
        data
                .setAverageCost(
                        NumberUtil.round(
                                map.values()
                                        .stream()
                                        .mapToDouble(EntrySeasonInfoData::getTotalTransfersCost)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setTopAverageCost(
                        NumberUtil.round(
                                topCostList
                                        .stream()
                                        .mapToDouble(EntrySeasonInfoData::getTotalTransfersCost)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setTopCost(topCostList);
        // bench
        List<EntrySeasonInfoData> topBenchList = map.values()
                .stream()
                .sorted(Comparator.comparing(EntrySeasonInfoData::getTotalBenchPoints).reversed())
                .limit(5)
                .collect(Collectors.toList());
        data
                .setAverageBenchPoints(
                        NumberUtil.round(
                                map.values()
                                        .stream()
                                        .mapToDouble(EntrySeasonInfoData::getTotalBenchPoints)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setTopAverageBenchPoints(
                        NumberUtil.round(
                                topBenchList
                                        .stream()
                                        .mapToDouble(EntrySeasonInfoData::getTotalBenchPoints)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setTopBench(topBenchList);
        // autoSubs
        List<EntrySeasonInfoData> topAutoSubsList = map.values()
                .stream()
                .sorted(Comparator.comparing(EntrySeasonInfoData::getTotalAutoSubsPoints).reversed())
                .limit(5)
                .collect(Collectors.toList());
        data
                .setAverageAutoSubsPoints(
                        NumberUtil.round(
                                map.values()
                                        .stream()
                                        .mapToDouble(EntrySeasonInfoData::getTotalAutoSubsPoints)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setTopAverageAutoSubsPoints(
                        NumberUtil.round(
                                topAutoSubsList
                                        .stream()
                                        .mapToDouble(EntrySeasonInfoData::getTotalAutoSubsPoints)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setTopAutoSubs(topAutoSubsList);
        // above hundred
        List<LeagueEventReportEntity> aboveHundredEntityList = leagueEventReportEntityList
                .stream()
                .filter(o -> o.getEventPoints() >= 100)
                .collect(Collectors.toList());
        Map<String, Long> entryAboveHundredCountMap = aboveHundredEntityList
                .stream()
                .collect(Collectors.groupingBy(LeagueEventReportEntity::getEntryName, Collectors.counting()));
        LinkedHashMap<String, List<EntryAboveHundredData>> topAboveHundred = Maps.newLinkedHashMap();
        entryAboveHundredCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .forEach(o ->
                        topAboveHundred.put(o.getKey(),
                                aboveHundredEntityList
                                        .stream()
                                        .filter(i -> StringUtils.equals(o.getKey(), i.getEntryName()))
                                        .map(i ->
                                                new EntryAboveHundredData()
                                                        .setEvent(i.getEvent())
                                                        .setEntry(i.getEntry())
                                                        .setEntryName(i.getEntryName())
                                                        .setPlayerName(i.getPlayerName())
                                                        .setPoint(i.getEventPoints())
                                                        .setTransfers(i.getEventTransfers())
                                                        .setCost(i.getEventTransfersCost())
                                                        .setNetPoints(i.getEventNetPoints())
                                                        .setChip(i.getEventChip())
                                        )
                                        .collect(Collectors.toList())
                        ));
        data.setTopAboveHundred(topAboveHundred);
        return data;
    }

    private EntrySeasonInfoData initLeagueEntryInfoData(LeagueEventReportEntity leagueEventReportEntity, Map<Integer, EntrySeasonInfoData> map) {
        int entry = leagueEventReportEntity.getEntry();
        EntrySeasonInfoData data;
        if (!map.containsKey(entry)) {
            data = new EntrySeasonInfoData()
                    .setEvent(leagueEventReportEntity.getEvent())
                    .setEntry(leagueEventReportEntity.getEntry())
                    .setEntryName(leagueEventReportEntity.getEntryName())
                    .setPlayerName(leagueEventReportEntity.getPlayerName())
                    .setOverallPoints(leagueEventReportEntity.getOverallPoints())
                    .setOverallRank(leagueEventReportEntity.getOverallRank())
                    .setValue(leagueEventReportEntity.getTeamValue() / 10.0)
                    .setBank(leagueEventReportEntity.getBank() / 10.0)
                    .setTeamValue((leagueEventReportEntity.getTeamValue() - leagueEventReportEntity.getBank()) / 10.0)
                    .setTotalTransfers(leagueEventReportEntity.getEventTransfers())
                    .setTotalTransfersCost(leagueEventReportEntity.getEventTransfersCost())
                    .setTotalBenchPoints(leagueEventReportEntity.getEventBenchPoints())
                    .setTotalAutoSubsPoints(leagueEventReportEntity.getEventAutoSubPoints());
        } else {
            data = map.get(entry);
            data.setTotalTransfers(data.getTotalTransfers() + leagueEventReportEntity.getEventTransfers())
                    .setTotalTransfersCost(data.getTotalTransfersCost() + leagueEventReportEntity.getEventTransfersCost())
                    .setTotalBenchPoints(data.getTotalBenchPoints() + leagueEventReportEntity.getEventBenchPoints())
                    .setTotalAutoSubsPoints(data.getTotalAutoSubsPoints() + leagueEventReportEntity.getEventAutoSubPoints());
            if (data.getEvent() < leagueEventReportEntity.getEvent()) {
                data
                        .setOverallPoints(leagueEventReportEntity.getOverallPoints())
                        .setOverallRank(leagueEventReportEntity.getOverallRank())
                        .setValue(leagueEventReportEntity.getTeamValue() / 10.0)
                        .setBank(leagueEventReportEntity.getBank() / 10.0)
                        .setTeamValue((leagueEventReportEntity.getTeamValue() - leagueEventReportEntity.getBank()) / 10.0);
            }
        }
        return data;
    }

    @Cacheable(
            value = "api::qryLeagueSeasonCaptain",
            key = "#leagueId+'::'+#leagueType",
            cacheManager = "apiCacheManager",
            unless = "#result.leagueId eq 0"
    )
    @Override
    public LeagueSeasonCaptainData qryLeagueSeasonCaptain(int leagueId, String leagueType) {
        LeagueSeasonCaptainData data = new LeagueSeasonCaptainData();
        // prepare
        int event = this.apiQueryService.getCurrentEvent();
        Multimap<Integer, LeagueEventReportEntity> map = HashMultimap.create();
        this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .ne(LeagueEventReportEntity::getEventPoints, 0)
                .orderByAsc(LeagueEventReportEntity::getEvent))
                .forEach(o -> map.put(o.getEntry(), o));
        if (map.size() == 0) {
            return data;
        }
        Map<Integer, String> webNameMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, PlayerEntity::getWebName));
        if (CollectionUtils.isEmpty(webNameMap)) {
            return data;
        }
        Map<Integer, Integer> captainPointsMap = map.values()
                .stream()
                .collect(Collectors.toMap(LeagueEventReportEntity::getPlayedCaptain,
                        v -> {
                            if (v.getPlayedCaptain() == v.getCaptain()) {
                                return this.calcCaptainPoints(v.getCaptainPoints(), v.getEventChip());
                            } else if (v.getPlayedCaptain() == v.getViceCaptain()) {
                                return this.calcCaptainPoints(v.getViceCaptainPoints(), v.getEventChip());
                            }
                            return 0;
                        }, Integer::sum, HashMap::new));
        // league info
        LeagueEventReportEntity
                leagueEventReportEntity = this.leagueEventReportService.getOne(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getEvent, event)
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .last("limit 1"));
        if (leagueEventReportEntity == null) {
            return data;
        }
        data
                .setLeagueId(leagueId)
                .setLeagueType(leagueType)
                .setLeagueName(leagueEventReportEntity.getLeagueName())
                .setTotalCaptain(captainPointsMap.size())
                .setTotalCaptainPoints(
                        map.values()
                                .stream()
                                .mapToInt(o -> this.calcCaptainPoints(o.getCaptainPoints(), o.getEventChip()))
                                .sum()
                )
                .setTotalViceCaptainPoints(
                        map.values()
                                .stream()
                                .filter(o -> o.getPlayedCaptain() == o.getViceCaptain())
                                .mapToInt(o -> this.calcCaptainPoints(o.getViceCaptainPoints(), o.getEventChip()))
                                .sum()
                )
                .setAverageCaptainPoints(
                        NumberUtil.round(
                                map.keySet()
                                        .stream()
                                        .map(entry ->
                                                map.get(entry)
                                                        .stream()
                                                        .mapToDouble(o -> this.calcCaptainPoints(o.getCaptainPoints(), o.getEventChip()))
                                                        .sum()
                                        )
                                        .collect(Collectors.toList())
                                        .stream()
                                        .mapToDouble(Double::doubleValue)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setAverageViceCaptainPoints(
                        NumberUtil.round(
                                map.keySet()
                                        .stream()
                                        .map(entry ->
                                                map.get(entry)
                                                        .stream()
                                                        .filter(o -> o.getPlayedCaptain() == o.getViceCaptain())
                                                        .mapToDouble(o -> this.calcCaptainPoints(o.getViceCaptainPoints(), o.getEventChip()))
                                                        .sum()
                                        )
                                        .collect(Collectors.toList())
                                        .stream()
                                        .mapToDouble(Double::doubleValue)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                );
        // most points
        data.setMostPointsCaptain(
                captainPointsMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                        .limit(5)
                        .map(o ->
                                new EntrySelectedCaptainData()
                                        .setElement(o.getKey())
                                        .setWebName(webNameMap.getOrDefault(o.getKey(), ""))
                                        .setTimes(
                                                map.values()
                                                        .stream()
                                                        .filter(i -> i.getPlayedCaptain() == o.getKey())
                                                        .count()
                                        )
                                        .setTotalPoints(o.getValue())
                                        .setTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(o.getValue() * 1.0, data.getTotalCaptainPoints() * 1.0), 2))
                        )
                        .collect(Collectors.toList())
        );
        // most selected
        Map<Integer, Long> captainSelectedMap = map.values()
                .stream()
                .collect(Collectors.groupingBy(LeagueEventReportEntity::getCaptain, Collectors.counting()));
        data.setMostSelectedCaptain(
                captainSelectedMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                        .limit(5)
                        .map(o ->
                                new EntrySelectedCaptainData()
                                        .setElement(o.getKey())
                                        .setWebName(webNameMap.getOrDefault(o.getKey(), ""))
                                        .setTimes(o.getValue())
                                        .setTotalPoints(captainPointsMap.get(o.getKey()))
                                        .setTotalPointsByPercent("")
                        )
                        .collect(Collectors.toList())
        );
        // tc selected
        Map<Integer, Long> tcSelectedMap = map.values()
                .stream()
                .filter(o -> StringUtils.equalsIgnoreCase(Chip.TC.getValue(), o.getEventChip()))
                .collect(Collectors.groupingBy(LeagueEventReportEntity::getCaptain, Collectors.counting()));
        data.setMostTcSelectedCaptain(
                tcSelectedMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                        .limit(3)
                        .map(o ->
                                new EntrySelectedCaptainData()
                                        .setElement(o.getKey())
                                        .setWebName(webNameMap.getOrDefault(o.getKey(), ""))
                                        .setTimes(o.getValue())
                                        .setTotalPoints(
                                                map.values()
                                                        .stream()
                                                        .filter(i -> o.getKey() == i.getPlayedCaptain() && StringUtils.equalsIgnoreCase(Chip.TC.getValue(), i.getEventChip()))
                                                        .mapToInt(i -> {
                                                            if (i.getPlayedCaptain() == i.getCaptain()) {
                                                                return 3 * i.getCaptainPoints();
                                                            } else if (i.getPlayedCaptain() == i.getViceCaptain()) {
                                                                return 3 * i.getViceCaptainPoints();
                                                            }
                                                            return 0;
                                                        })
                                                        .sum()
                                        )
                                        .setTotalPointsByPercent("")
                        )
                        .collect(Collectors.toList())
        );
        // entry captain points
        Map<Integer, Integer> entryCaptainPointsMap = map.values()
                .stream()
                .collect(Collectors.toMap(LeagueEventReportEntity::getEntry,
                        v -> {
                            if (v.getPlayedCaptain() == v.getCaptain()) {
                                return this.calcCaptainPoints(v.getCaptainPoints(), v.getEventChip());
                            } else if (v.getPlayedCaptain() == v.getViceCaptain()) {
                                return this.calcCaptainPoints(v.getViceCaptainPoints(), v.getEventChip());
                            }
                            return 0;
                        }, Integer::sum, HashMap::new));
        data
                .setBestCaptainEntry(
                        entryCaptainPointsMap.entrySet()
                                .stream()
                                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                                .limit(5)
                                .map(o -> {
                                    int entry = o.getKey();
                                    LeagueEventReportEntity entity = map.get(entry)
                                            .stream()
                                            .max(Comparator.comparing(LeagueEventReportEntity::getEvent))
                                            .orElse(new LeagueEventReportEntity());
                                    return new EntrySeasonCaptainData()
                                            .setEntry(entry)
                                            .setEntryName(entity.getEntryName())
                                            .setPlayerName(entity.getPlayerName())
                                            .setTotalPoints(o.getValue())
                                            .setTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(o.getValue() * 1.0, entity.getOverallPoints() * 1.0), 2));
                                })
                                .collect(Collectors.toList())
                )
                .setWorstCaptainEntry(
                        entryCaptainPointsMap.entrySet()
                                .stream()
                                .sorted(Map.Entry.comparingByValue())
                                .limit(5)
                                .map(o -> {
                                    int entry = o.getKey();
                                    LeagueEventReportEntity entity = map.get(entry)
                                            .stream()
                                            .max(Comparator.comparing(LeagueEventReportEntity::getEvent))
                                            .orElse(new LeagueEventReportEntity());
                                    return new EntrySeasonCaptainData()
                                            .setEntry(entry)
                                            .setEntryName(entity.getEntryName())
                                            .setPlayerName(entity.getPlayerName())
                                            .setTotalPoints(o.getValue())
                                            .setTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(o.getValue() * 1.0, entity.getOverallPoints() * 1.0), 2));
                                })
                                .collect(Collectors.toList())
                );
        // entry captain points by percent
        Map<Integer, Double> entryCaptainPointsByPercentMap = Maps.newHashMap();
        entryCaptainPointsMap.keySet().forEach(entry -> entryCaptainPointsByPercentMap.put(entry,
                NumberUtil.div(entryCaptainPointsMap.get(entry) * 1.0,
                        map.get(entry)
                                .stream()
                                .max(Comparator.comparing(LeagueEventReportEntity::getEvent))
                                .orElse(new LeagueEventReportEntity())
                                .getOverallPoints() * 1.0
                )
        ));
        data
                .setMostPointsByPercentEntry(
                        entryCaptainPointsByPercentMap.entrySet()
                                .stream()
                                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                                .limit(5)
                                .map(o -> {
                                    int entry = o.getKey();
                                    LeagueEventReportEntity entity = map.get(entry)
                                            .stream()
                                            .max(Comparator.comparing(LeagueEventReportEntity::getEvent))
                                            .orElse(new LeagueEventReportEntity());
                                    return new EntrySeasonCaptainData()
                                            .setEntry(entry)
                                            .setEntryName(entity.getEntryName())
                                            .setPlayerName(entity.getPlayerName())
                                            .setTotalPoints(entryCaptainPointsMap.get(entry))
                                            .setTotalPointsByPercent(NumberUtil.formatPercent(o.getValue(), 2));
                                })
                                .collect(Collectors.toList())
                )
                .setLeastPointsByPercentEntry(
                        entryCaptainPointsByPercentMap.entrySet()
                                .stream()
                                .sorted(Map.Entry.comparingByValue())
                                .limit(5)
                                .map(o -> {
                                    int entry = o.getKey();
                                    LeagueEventReportEntity entity = map.get(entry)
                                            .stream()
                                            .max(Comparator.comparing(LeagueEventReportEntity::getEvent))
                                            .orElse(new LeagueEventReportEntity());
                                    return new EntrySeasonCaptainData()
                                            .setEntry(entry)
                                            .setEntryName(entity.getEntryName())
                                            .setPlayerName(entity.getPlayerName())
                                            .setTotalPoints(entryCaptainPointsMap.get(entry))
                                            .setTotalPointsByPercent(NumberUtil.formatPercent(o.getValue(), 2));
                                })
                                .collect(Collectors.toList())
                );
        return data;
    }

    private int calcCaptainPoints(int points, String chip) {
        if (StringUtils.equalsIgnoreCase(Chip.TC.getValue(), chip)) {
            return 3 * points;
        }
        return 2 * points;
    }

    @Cacheable(
            value = "api::qryLeagueSeasonScore",
            key = "#leagueId+'::'+#leagueType",
            cacheManager = "apiCacheManager",
            unless = "#result.leagueId eq 0"
    )
    @Override
    public LeagueSeasonScoreData qryLeagueSeasonScore(int leagueId, String leagueType) {
        LeagueSeasonScoreData data = new LeagueSeasonScoreData();
        // prepare
        int current = this.apiQueryService.getCurrentEvent();
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .ne(LeagueEventReportEntity::getEventPoints, 0)
                .orderByAsc(LeagueEventReportEntity::getEvent));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return data;
        }
        Map<String, String> shortNameMap = this.apiQueryService.getTeamShortNameMap();
        if (CollectionUtils.isEmpty(shortNameMap)) {
            return data;
        }
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        if (CollectionUtils.isEmpty(playerMap)) {
            return data;
        }
        Table<Integer, Integer, EventLiveEntity> eventLiveTable = HashBasedTable.create(); // element -> event -> data
        this.eventLiveService.list().forEach(o -> eventLiveTable.put(o.getElement(), o.getEvent(), o));
        // collect
        List<EntryPickData> entryPickDataList = Lists.newArrayList();
        leagueEventReportEntityList.forEach(o -> {
            int event = o.getEvent();
            int entry = o.getEntry();
            if (StringUtils.equalsIgnoreCase(Chip.BB.getValue(), o.getEventChip())) {
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition1(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition2(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition3(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition4(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition5(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition6(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition7(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition8(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition9(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition10(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition11(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition12(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition13(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition14(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition15(), shortNameMap, playerMap, eventLiveTable));
            } else {
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition1(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition2(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition3(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition4(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition5(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition6(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition7(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition8(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition9(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition10(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, entry, o.getPosition11(), shortNameMap, playerMap, eventLiveTable));
            }
        });
        Table<Integer, Integer, List<EntryPickData>> pickTable = HashBasedTable.create(); // elementType -> event -> dataList
        entryPickDataList.forEach(o -> {
            int event = o.getEvent();
            int elementType = o.getElementType();
            List<EntryPickData> list = Lists.newArrayList();
            if (pickTable.contains(elementType, event)) {
                list = pickTable.get(elementType, event);
            }
            if (CollectionUtils.isEmpty(list)) {
                list = Lists.newArrayList();
            }
            list.add(o);
            pickTable.put(elementType, event, list);
        });
        Table<Integer, Integer, Map<Integer, List<EntryPickData>>> entryPickTable = HashBasedTable.create();
        entryPickDataList.forEach(o -> {
            int event = o.getEvent();
            int entry = o.getEntry();
            Map<Integer, List<EntryPickData>> map = Maps.newHashMap();
            if (entryPickTable.contains(entry, event)) {
                map = entryPickTable.get(entry, event);
            }
            if (CollectionUtils.isEmpty(map)) {
                map = Maps.newHashMap();
            }
            int elementType = o.getElementType();
            List<EntryPickData> list = map.getOrDefault(elementType, Lists.newArrayList());
            list.add(o);
            map.put(elementType, list);
            entryPickTable.put(entry, event, map);
        });
        data
                .setLeagueId(leagueEventReportEntityList.get(0).getLeagueId())
                .setLeagueType(leagueEventReportEntityList.get(0).getLeagueType())
                .setLeagueName(leagueEventReportEntityList.get(0).getLeagueName())
                .setTotalOverallPoints(
                        leagueEventReportEntityList
                                .stream()
                                .filter(o -> o.getEvent() == current)
                                .mapToInt(LeagueEventReportEntity::getOverallPoints)
                                .sum()
                )
                .setAverageOverallPoints(
                        NumberUtil.round(
                                leagueEventReportEntityList
                                        .stream()
                                        .filter(o -> o.getEvent() == current)
                                        .mapToInt(LeagueEventReportEntity::getOverallPoints)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                );
        // gkp
        List<EntryPickData> gkpPickList = Lists.newArrayList();
        pickTable.row(Position.GKP.getElementType()).values().forEach(gkpPickList::addAll);
        data.setGkpTotalPoints(
                gkpPickList
                        .stream()
                        .mapToInt(EntryPickData::getPoints)
                        .sum()
        );
        data.setGkpTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getGkpTotalPoints(), data.getTotalOverallPoints()), 2));
        Map<String, Long> mostSelectedGkpCountMap = gkpPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(o.getElement()).getWebName(), Collectors.counting()));
        data.setMostSelectedGkpByPercent(
                mostSelectedGkpCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toMap(Map.Entry::getKey, v -> NumberUtil.formatPercent(NumberUtil.div(v.getValue() * 1.0, gkpPickList.size() * 1.0), 2),
                                (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        // entry gkp
        Multimap<Integer, EntryPickData> entryGkpPickMap = HashMultimap.create();
        entryPickTable.rowKeySet().forEach(entry -> entryPickTable.row(entry).values().forEach(i -> i.get(Position.GKP.getElementType()).forEach(j -> entryGkpPickMap.put(entry, j))));
        data.setAverageEntryGkpTotalNum(
                NumberUtil.round(
                        entryGkpPickMap.keySet()
                                .stream()
                                .mapToDouble(o ->
                                        entryGkpPickMap.get(o)
                                                .stream()
                                                .map(EntryPickData::getElement)
                                                .distinct()
                                                .count()
                                )
                                .average()
                                .orElse(0)
                        , 2)
                        .doubleValue()
        );
        Map<Integer, Integer> entryGkpPointsMap = entryGkpPickMap.keySet()
                .stream()
                .collect(Collectors.toMap(k -> k, v ->
                        entryGkpPickMap.get(v)
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                ));
        data.setAverageEntryGkpTotalPoints(
                NumberUtil.round(
                        entryGkpPointsMap.values()
                                .stream()
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(0)
                        , 2)
                        .doubleValue()
        )
                .setMostEntryGkpPoints(
                        entryGkpPointsMap.entrySet()
                                .stream()
                                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                                .limit(5)
                                .collect(Collectors.toMap(k -> this.apiQueryService.qryEntryInfo(k.getKey()), Map.Entry::getValue,
                                        (oldValue, newValue) -> newValue, LinkedHashMap::new))
                );
        // def
        List<EntryPickData> defPickList = Lists.newArrayList();
        pickTable.row(Position.DEF.getElementType()).values().forEach(defPickList::addAll);
        data.setDefTotalPoints(
                defPickList
                        .stream()
                        .mapToInt(EntryPickData::getPoints)
                        .sum()
        );
        data.setDefTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getDefTotalPoints(), data.getTotalOverallPoints()), 2));
        Map<String, Long> mostSelectedDefCountMap = defPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(o.getElement()).getWebName(), Collectors.counting()));
        data.setMostSelectedDefByPercent(
                mostSelectedDefCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toMap(Map.Entry::getKey, v -> NumberUtil.formatPercent(NumberUtil.div(v.getValue() * 1.0, defPickList.size() * 1.0), 2),
                                (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        // entry def
        Multimap<Integer, EntryPickData> entryDefPickMap = HashMultimap.create();
        entryPickTable.rowKeySet().forEach(entry -> entryPickTable.row(entry).values().forEach(i -> i.get(Position.DEF.getElementType()).forEach(j -> entryDefPickMap.put(entry, j))));
        data.setAverageEntryDefTotalNum(
                NumberUtil.round(
                        entryDefPickMap.keySet()
                                .stream()
                                .mapToDouble(o ->
                                        entryDefPickMap.get(o)
                                                .stream()
                                                .map(EntryPickData::getElement)
                                                .distinct()
                                                .count()
                                )
                                .average()
                                .orElse(0)
                        , 2)
                        .doubleValue()
        );
        Map<Integer, Integer> entryDefPointsMap = entryDefPickMap.keySet()
                .stream()
                .collect(Collectors.toMap(k -> k, v ->
                        entryDefPickMap.get(v)
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                ));
        data.setAverageEntryDefTotalPoints(
                NumberUtil.round(
                        entryDefPointsMap.values()
                                .stream()
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(0)
                        , 2)
                        .doubleValue()
        )
                .setMostEntryDefPoints(
                        entryDefPointsMap.entrySet()
                                .stream()
                                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                                .limit(5)
                                .collect(Collectors.toMap(k -> this.apiQueryService.qryEntryInfo(k.getKey()), Map.Entry::getValue,
                                        (oldValue, newValue) -> newValue, LinkedHashMap::new))
                );
        // mid
        List<EntryPickData> midPickList = Lists.newArrayList();
        pickTable.row(Position.MID.getElementType()).values().forEach(midPickList::addAll);
        data.setMidTotalPoints(
                midPickList
                        .stream()
                        .mapToInt(EntryPickData::getPoints)
                        .sum()
        );
        data.setMidTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getMidTotalPoints(), data.getTotalOverallPoints()), 2));
        Map<String, Long> mostSelectedMidCountMap = midPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(o.getElement()).getWebName(), Collectors.counting()));
        data.setMostSelectedMidByPercent(
                mostSelectedMidCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toMap(Map.Entry::getKey, v -> NumberUtil.formatPercent(NumberUtil.div(v.getValue() * 1.0, midPickList.size() * 1.0), 2),
                                (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        // entry mid
        Multimap<Integer, EntryPickData> entryMidPickMap = HashMultimap.create();
        entryPickTable.rowKeySet().forEach(entry -> entryPickTable.row(entry).values().forEach(i -> i.get(Position.MID.getElementType()).forEach(j -> entryMidPickMap.put(entry, j))));
        data.setAverageEntryMidTotalNum(
                NumberUtil.round(
                        entryMidPickMap.keySet()
                                .stream()
                                .mapToDouble(o ->
                                        entryMidPickMap.get(o)
                                                .stream()
                                                .map(EntryPickData::getElement)
                                                .distinct()
                                                .count()
                                )
                                .average()
                                .orElse(0)
                        , 2)
                        .doubleValue()
        );
        Map<Integer, Integer> entryMidPointsMap = entryMidPickMap.keySet()
                .stream()
                .collect(Collectors.toMap(k -> k, v ->
                        entryMidPickMap.get(v)
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                ));
        data.setAverageEntryMidTotalPoints(
                NumberUtil.round(
                        entryMidPointsMap.values()
                                .stream()
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(0)
                        , 2)
                        .doubleValue()
        )
                .setMostEntryMidPoints(
                        entryMidPointsMap.entrySet()
                                .stream()
                                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                                .limit(5)
                                .collect(Collectors.toMap(k -> this.apiQueryService.qryEntryInfo(k.getKey()), Map.Entry::getValue,
                                        (oldValue, newValue) -> newValue, LinkedHashMap::new))
                );
        // fwd
        List<EntryPickData> fwdPickList = Lists.newArrayList();
        pickTable.row(Position.FWD.getElementType()).values().forEach(fwdPickList::addAll);
        data.setFwdTotalPoints(
                fwdPickList
                        .stream()
                        .mapToInt(EntryPickData::getPoints)
                        .sum()
        );
        data.setFwdTotalPointsByPercent(NumberUtil.formatPercent(NumberUtil.div(data.getFwdTotalPoints(), data.getTotalOverallPoints()), 2));
        Map<String, Long> mostSelectedFwdCountMap = fwdPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(o.getElement()).getWebName(), Collectors.counting()));
        data.setMostSelectedFwdByPercent(
                mostSelectedFwdCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toMap(Map.Entry::getKey, v -> NumberUtil.formatPercent(NumberUtil.div(v.getValue() * 1.0, fwdPickList.size() * 1.0), 2),
                                (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        // entry fwd
        Multimap<Integer, EntryPickData> entryFwdPickMap = HashMultimap.create();
        entryPickTable.rowKeySet().forEach(entry -> entryPickTable.row(entry).values().forEach(i -> i.get(Position.FWD.getElementType()).forEach(j -> entryFwdPickMap.put(entry, j))));
        data.setAverageEntryFwdTotalNum(
                NumberUtil.round(
                        entryFwdPickMap.keySet()
                                .stream()
                                .mapToDouble(o ->
                                        entryFwdPickMap.get(o)
                                                .stream()
                                                .map(EntryPickData::getElement)
                                                .distinct()
                                                .count()
                                )
                                .average()
                                .orElse(0)
                        , 2)
                        .doubleValue()
        );
        Map<Integer, Integer> entryFwdPointsMap = entryFwdPickMap.keySet()
                .stream()
                .collect(Collectors.toMap(k -> k, v ->
                        entryFwdPickMap.get(v)
                                .stream()
                                .mapToInt(EntryPickData::getPoints)
                                .sum()
                ));
        data.setAverageEntryFwdTotalPoints(
                NumberUtil.round(
                        entryFwdPointsMap.values()
                                .stream()
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(0)
                        , 2)
                        .doubleValue()
        )
                .setMostEntryFwdPoints(
                        entryFwdPointsMap.entrySet()
                                .stream()
                                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                                .limit(5)
                                .collect(Collectors.toMap(k -> this.apiQueryService.qryEntryInfo(k.getKey()), Map.Entry::getValue,
                                        (oldValue, newValue) -> newValue, LinkedHashMap::new))
                );
        // formation
        List<String> formationList = Lists.newArrayList();
        entryPickTable.columnMap().values().forEach(o ->
                o.values().forEach(i -> formationList.add(StringUtils.joinWith("-", i.get(2).size(), i.get(3).size(), i.get(4).size())))
        );
        Map<String, Long> mostSelectedFormationCountMap = formationList.
                stream()
                .collect(Collectors.groupingBy(String::toString, Collectors.counting()));
        data.setMostSelectedFormation(
                mostSelectedFormationCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(5)
                        .collect(Collectors.toMap(Map.Entry::getKey, v -> NumberUtil.formatPercent(NumberUtil.div(v.getValue() * 1.0, formationList.size() * 1.0), 2),
                                (oldValue, newValue) -> newValue, LinkedHashMap::new))
        );
        return data;
    }

    private EntryPickData initLeagueEntryPickData(int event, int entry, int element, Map<String, String> shortNameMap, Map<Integer, PlayerEntity> playerMap, Table<Integer, Integer, EventLiveEntity> eventLiveTable) {
        PlayerEntity playerEntity = playerMap.getOrDefault(element, null);
        EventLiveEntity eventLiveEntity = eventLiveTable.get(element, event);
        if (playerEntity == null || eventLiveEntity == null) {
            return null;
        }
        int elementType = playerEntity.getElementType();
        return new EntryPickData()
                .setEvent(event)
                .setEntry(entry)
                .setElement(element)
                .setWebName(playerEntity.getWebName())
                .setElementType(elementType)
                .setElementTypeName(Position.getNameFromElementType(elementType))
                .setTeamId(playerEntity.getTeamId())
                .setTeamShortName(shortNameMap.getOrDefault(String.valueOf(playerEntity.getTeamId()), ""))
                .setMinutes(eventLiveEntity.getMinutes())
                .setPoints(eventLiveEntity.getTotalPoints());
    }

    //    @Cacheable(
//            value = "api::qryLeagueSeasonEntry",
//            key = "#leagueId+'::'+#leagueType+'::'+#entry",
//            cacheManager = "apiCacheManager",
//            unless = "#result.leagueId eq 0"
//    )
    @Override
    public LeagueSeasonEntryData qryLeagueSeasonEntry(int leagueId, String leagueType, int entry) {
        LeagueSeasonEntryData data = new LeagueSeasonEntryData();
        // prepare
        int current = this.apiQueryService.getCurrentEvent();
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueId, leagueId)
                .eq(LeagueEventReportEntity::getLeagueType, leagueType)
                .ne(LeagueEventReportEntity::getEventPoints, 0)
                .orderByAsc(LeagueEventReportEntity::getEvent));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return data;
        }
        LeagueEventReportEntity last = leagueEventReportEntityList
                .stream()
                .filter(o -> o.getEntry() == entry)
                .max(Comparator.comparing(LeagueEventReportEntity::getEvent))
                .orElse(null);
        if (last == null) {
            return data;
        }
        Multimap<Integer, LeagueEventReportEntity> entryLeagueEventReportMap = HashMultimap.create();
        leagueEventReportEntityList.forEach(o -> entryLeagueEventReportMap.put(o.getEntry(), o));
        // info
        data
                .setLeagueId(last.getLeagueId())
                .setLeagueType(last.getLeagueType())
                .setLeagueName(last.getLeagueName())
                .setEntry(entry)
                .setEntryName(last.getEntryName())
                .setPlayerName(last.getPlayerName())
                .setOverallPoints(last.getOverallPoints())
                .setOverallRank(last.getOverallRank())
                .setValue(last.getTeamValue() / 10.0);
        this.setEntryLeagueInfoData(current, leagueEventReportEntityList, entryLeagueEventReportMap, data);
        // captain
        this.setEntryLeagueCaptainData(entry, leagueEventReportEntityList, entryLeagueEventReportMap, data);
        // score
        this.setEntryLeagueScoreData(entry, leagueEventReportEntityList, entryLeagueEventReportMap, data);
        return data;
    }

    private void setEntryLeagueInfoData(int event, List<LeagueEventReportEntity> leagueEventReportEntityList, Multimap<Integer, LeagueEventReportEntity> entryLeagueEventReportMap, LeagueSeasonEntryData data) {
        // league rank
        List<Integer> overallRankList = leagueEventReportEntityList
                .stream()
                .filter(o -> o.getEvent() == event)
                .map(LeagueEventReportEntity::getOverallRank)
                .sorted(Comparator.comparing(Integer::intValue))
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> leagueRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, overallRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, overallRankList, leagueRankMap);
            leagueRankMap.put(overallRankList.get(i), rank);
        });
        data.setLeagueRank(leagueRankMap.get(data.getOverallRank()));
        // value rank
        List<Double> valueList = leagueEventReportEntityList
                .stream()
                .filter(o -> o.getEvent() == event)
                .map(o -> o.getTeamValue() / 10.0)
                .sorted(Comparator.comparing(Double::doubleValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Double, Integer> valueRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, valueList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, valueList, valueRankMap);
            valueRankMap.put(valueList.get(i), rank);
        });
        data.setValueRank(valueRankMap.get(data.getValue()));
        // transfers
        List<Integer> transfersRankList = leagueEventReportEntityList
                .stream()
                .filter(o -> o.getEvent() == event)
                .map(o ->
                        entryLeagueEventReportMap.get(o.getEntry())
                                .stream()
                                .mapToInt(LeagueEventReportEntity::getEventTransfers)
                                .sum()
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> transfersRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, transfersRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, transfersRankList, transfersRankMap);
            transfersRankMap.put(transfersRankList.get(i), rank);
        });
        data.setTransfers(
                entryLeagueEventReportMap.get(data.getEntry())
                        .stream()
                        .mapToInt(LeagueEventReportEntity::getEventTransfers)
                        .sum()
        );
        data.setTransfersRank(transfersRankMap.get(data.getTransfers()));
        // transfers cost
        List<Integer> transfersCostRankList = leagueEventReportEntityList
                .stream()
                .filter(o -> o.getEvent() == event)
                .map(o ->
                        entryLeagueEventReportMap.get(o.getEntry())
                                .stream()
                                .mapToInt(LeagueEventReportEntity::getEventTransfersCost)
                                .sum()
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> transfersCostRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, transfersCostRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, transfersCostRankList, transfersCostRankMap);
            transfersCostRankMap.put(transfersCostRankList.get(i), rank);
        });
        data.setTransfersCost(
                entryLeagueEventReportMap.get(data.getEntry())
                        .stream()
                        .mapToInt(LeagueEventReportEntity::getEventTransfersCost)
                        .sum()
        );
        data.setTransfersCostRank(transfersCostRankMap.get(data.getTransfersCost()));
        // bench points
        List<Integer> benchPointsRankList = leagueEventReportEntityList
                .stream()
                .filter(o -> o.getEvent() == event)
                .map(o ->
                        entryLeagueEventReportMap.get(o.getEntry())
                                .stream()
                                .mapToInt(LeagueEventReportEntity::getEventBenchPoints)
                                .sum()
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> benchPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, benchPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, benchPointsRankList, benchPointsRankMap);
            benchPointsRankMap.put(benchPointsRankList.get(i), rank);
        });
        data.setBenchPoints(
                entryLeagueEventReportMap.get(data.getEntry())
                        .stream()
                        .mapToInt(LeagueEventReportEntity::getEventBenchPoints)
                        .sum()
        );
        data.setBenchPointsRank(benchPointsRankMap.get(data.getBenchPoints()));
        // autoSubs points
        List<Integer> autoSubsPointsRankList = leagueEventReportEntityList
                .stream()
                .filter(o -> o.getEvent() == event)
                .map(o ->
                        entryLeagueEventReportMap.get(o.getEntry())
                                .stream()
                                .mapToInt(LeagueEventReportEntity::getEventAutoSubPoints)
                                .sum()
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> autoSubsPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, autoSubsPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, autoSubsPointsRankList, autoSubsPointsRankMap);
            autoSubsPointsRankMap.put(autoSubsPointsRankList.get(i), rank);
        });
        data.setAutoSubsPoints(
                entryLeagueEventReportMap.get(data.getEntry())
                        .stream()
                        .mapToInt(LeagueEventReportEntity::getEventAutoSubPoints)
                        .sum()
        );
        data.setAutoSubsPointsRank(autoSubsPointsRankMap.get(data.getAutoSubsPoints()));
    }

    private void setEntryLeagueCaptainData(int entry, List<LeagueEventReportEntity> leagueEventReportEntityList, Multimap<Integer, LeagueEventReportEntity> entryLeagueEventReportMap, LeagueSeasonEntryData data) {
        Multimap<Integer, Integer> entryLeagueEventCaptainMap = HashMultimap.create();
        leagueEventReportEntityList.forEach(o -> entryLeagueEventCaptainMap.put(o.getEntry(), this.calcLeagueCaptainPoints(o)));
        data
                .setCaptainPoints(
                        entryLeagueEventCaptainMap.get(entry)
                                .stream()
                                .mapToInt(Integer::intValue)
                                .sum()
                )
                .setMostCaptainPoints(
                        entryLeagueEventReportMap.get(entry)
                                .stream()
                                .map(this::getLeaguePlayedCaptainPoints)
                                .max(Comparator.comparing(Integer::intValue))
                                .orElse(0)
                )
                .setTcCaptainPoints(
                        entryLeagueEventReportMap.get(entry)
                                .stream()
                                .filter(o -> StringUtils.equalsIgnoreCase(Chip.TC.getValue(), o.getEventChip()))
                                .map(this::getLeaguePlayedCaptainPoints)
                                .findFirst()
                                .orElse(0)
                );
        // captain points
        List<Integer> captainPointsRankList = entryLeagueEventCaptainMap.keySet()
                .stream()
                .map(o ->
                        entryLeagueEventCaptainMap.get(o)
                                .stream()
                                .mapToInt(Integer::intValue)
                                .sum()
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> captainPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, captainPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, captainPointsRankList, captainPointsRankMap);
            captainPointsRankMap.put(captainPointsRankList.get(i), rank);
        });
        data.setCaptainRank(captainPointsRankMap.get(data.getCaptainPoints()));
        // most captain points
        List<Integer> mostCaptainPointsRankList = entryLeagueEventCaptainMap.keySet()
                .stream()
                .map(o ->
                        entryLeagueEventReportMap.get(o)
                                .stream()
                                .map(this::getLeaguePlayedCaptainPoints)
                                .max(Comparator.comparing(Integer::intValue))
                                .orElse(0)
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> mostCaptainPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, mostCaptainPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, mostCaptainPointsRankList, mostCaptainPointsRankMap);
            mostCaptainPointsRankMap.put(mostCaptainPointsRankList.get(i), rank);
        });
        data.setMostCaptainPointsRank(mostCaptainPointsRankMap.get(data.getMostCaptainPoints()));
        // tc captain points
        List<Integer> tcCaptainPointsRankList = entryLeagueEventCaptainMap.keySet()
                .stream()
                .map(o ->
                        entryLeagueEventReportMap.get(o)
                                .stream()
                                .filter(i -> StringUtils.equalsIgnoreCase(Chip.TC.getValue(), i.getEventChip()))
                                .map(this::getLeaguePlayedCaptainPoints)
                                .max(Comparator.comparing(Integer::intValue))
                                .orElse(0)
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> tcCaptainPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, tcCaptainPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, tcCaptainPointsRankList, tcCaptainPointsRankMap);
            tcCaptainPointsRankMap.put(tcCaptainPointsRankList.get(i), rank);
        });
        data.setTcCaptainPointsRank(tcCaptainPointsRankMap.get(data.getTcCaptainPoints()));
    }

    private int getLeaguePlayedCaptainPoints(LeagueEventReportEntity leagueEventReportEntity) {
        int points = leagueEventReportEntity.getCaptainPoints();
        if (leagueEventReportEntity.getPlayedCaptain() == leagueEventReportEntity.getViceCaptain()) {
            points = leagueEventReportEntity.getViceCaptainPoints();
        }
        return points;
    }

    private int calcLeagueCaptainPoints(LeagueEventReportEntity leagueEventReportEntity) {
        return this.calcCaptainPoints(this.getLeaguePlayedCaptainPoints(leagueEventReportEntity), leagueEventReportEntity.getEventChip());
    }

    private void setEntryLeagueScoreData(int entry, List<LeagueEventReportEntity> leagueEventReportEntityList, Multimap<Integer, LeagueEventReportEntity> entryLeagueEventReportMap, LeagueSeasonEntryData data) {
    }

    private <T> int calcRealRank(int index, List<T> rankList, LinkedHashMap<T, Integer> rankMap) {
        int rank = 1;
        if (index == 0) {
            return rank;
        }
        T lastValue = rankList.get(index - 1);
        int lastRank = rankMap.get(lastValue);
        if (rankList.get(index) == lastValue) {
            rank = lastRank;
        } else {
            int size = (int) rankList
                    .stream()
                    .filter(o -> o == lastValue)
                    .count();
            rank = lastRank + size;
        }
        return rank;
    }

}

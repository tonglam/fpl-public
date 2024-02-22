package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.constant.enums.Chip;
import com.tong.fpl.constant.enums.Position;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.*;
import com.tong.fpl.domain.letletme.global.MapData;
import com.tong.fpl.domain.letletme.summary.entry.*;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonCaptainData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonInfoData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonScoreData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonSummaryData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.ISummaryService;
import com.tong.fpl.service.db.EntryEventResultService;
import com.tong.fpl.service.db.EntryEventTransfersService;
import com.tong.fpl.service.db.EventLiveService;
import com.tong.fpl.service.db.LeagueEventReportService;
import com.tong.fpl.utils.CommonUtils;
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
    private final IQueryService queryService;

    private final EventLiveService eventLiveService;
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
        if (entry <= 0) {
            return new EntrySeasonInfoData();
        }
        // prepare
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(entry);
        if (entryInfoData == null) {
            return new EntrySeasonInfoData();
        }
        List<EntryEventResultEntity> entryEventResultEntityList = this.entryEventResultService.list(new QueryWrapper<EntryEventResultEntity>().lambda()
                .eq(EntryEventResultEntity::getEntry, entry));
        if (CollectionUtils.isEmpty(entryEventResultEntityList)) {
            return new EntrySeasonInfoData();
        }
        return new EntrySeasonInfoData()
                .setEntry(entry)
                .setEntryName(entryInfoData.getEntryName())
                .setPlayerName(entryInfoData.getPlayerName())
                .setOverallPoints(entryInfoData.getOverallPoints())
                .setOverallRank(entryInfoData.getOverallRank())
                .setTotalTransfers(entryInfoData.getTotalTransfers())
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
                .setValue(entryInfoData.getTeamValue() / 10.0)
                .setBank(entryInfoData.getBank() / 10.0)
                .setTeamValue((entryInfoData.getTeamValue() - entryInfoData.getBank()) / 10.0);
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
        if (entry <= 0) {
            return data;
        }
        // prepare
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(entry);
        if (entryInfoData == null) {
            return data;
        }
        Map<String, String> webNameMap = this.queryService.qryPlayerWebNameMap();
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
                .setEntryName(entryInfoData.getEntryName())
                .setPlayerName(entryInfoData.getPlayerName());
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
                                        .setEntryName(entryInfoData.getEntryName())
                                        .setPlayerName(entryInfoData.getPlayerName())
                                        .setPoint(o.getEventPoints())
                                        .setTransfers(o.getEventTransfers())
                                        .setCost(o.getEventTransfersCost())
                                        .setNetPoints(o.getEventNetPoints())
                                        .setChip(o.getEventChip())
                        )
                        .collect(Collectors.toList())
        );
        // bench
        List<EntryBenchData> highestBench = Lists.newArrayList();
        int highestBenchPoints = entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getEventBenchPoints))
                .map(EntryEventResultEntity::getEventBenchPoints)
                .orElse(0);
        if (highestBenchPoints == 0) {
            data.setHighestBench(highestBench);
        }
        highestBench = entryEventResultEntityList
                .stream()
                .filter(o -> o.getEventBenchPoints() == highestBenchPoints)
                .map(o -> {
                    Map<String, EventLiveEntity> eventLivePointsMap = this.queryService.getEventLiveByEvent(o.getEvent());
                    List<EntryPickData> pickList = JsonUtils.json2Collection(o.getEventPicks(), List.class, EntryPickData.class);
                    if (CollectionUtils.isEmpty(pickList)) {
                        return null;
                    }
                    return new EntryBenchData()
                            .setEvent(o.getEvent())
                            .setBenchPoints(o.getEventBenchPoints())
                            .setElementList(
                                    pickList
                                            .stream()
                                            .filter(i -> i.getPosition() > 11)
                                            .map(i -> i
                                                    .setEvent(i.getEvent())
                                                    .setEntry(entry)
                                                    .setWebName(webNameMap.getOrDefault(String.valueOf(i.getElement()), ""))
                                                    .setPoints(eventLivePointsMap.get(String.valueOf(i.getElement())).getTotalPoints())
                                            )
                                            .collect(Collectors.toList())
                            );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        data.setHighestBench(highestBench);
        // autoSub
        List<EntryAutoSubsData> highestAutoSubs = Lists.newArrayList();
        int highestAutoSubsPoints = entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getEventAutoSubPoints))
                .map(EntryEventResultEntity::getEventAutoSubPoints)
                .orElse(0);
        if (highestAutoSubsPoints == 0) {
            data.setHighestAutoSubs(highestAutoSubs);
        }
        highestAutoSubs = entryEventResultEntityList
                .stream()
                .filter(o -> o.getEventAutoSubPoints() == highestAutoSubsPoints)
                .map(o -> {
                    List<EntryEventAutoSubsData> list = JsonUtils.json2Collection(o.getEventAutoSubs(), List.class, EntryEventAutoSubsData.class);
                    if (CollectionUtils.isEmpty(list)) {
                        return null;
                    }
                    list.forEach(i ->
                            i
                                    .setEvent(o.getEvent())
                                    .setEntry(entry)
                                    .setElementInWebName(webNameMap.getOrDefault(String.valueOf(i.getElementIn()), ""))
                                    .setElementOutWebName(webNameMap.getOrDefault(String.valueOf(i.getElementOut()), ""))
                    );
                    return new EntryAutoSubsData()
                            .setEvent(o.getEvent())
                            .setAutoSubsPoints(o.getEventAutoSubPoints())
                            .setElementList(list);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        data.setHighestAutoSubs(highestAutoSubs);
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
        if (entry <= 0) {
            return data;
        }
        // prepare
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(entry);
        if (entryInfoData == null) {
            return data;
        }
        Map<String, String> shortNameMap = this.queryService.getTeamShortNameMap();
        if (CollectionUtils.isEmpty(shortNameMap)) {
            return data;
        }
        Map<String, String> webNameMap = this.queryService.qryPlayerWebNameMap();
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
                .setEntryName(entryInfoData.getEntryName())
                .setPlayerName(entryInfoData.getPlayerName());
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
                                .setMostPointsWebName(webNameMap.getOrDefault(String.valueOf(o.getPlayedCaptain()), ""))
                );
        // min
        entryEventResultEntityList
                .stream()
                .min(Comparator.comparing(EntryEventResultEntity::getCaptainPoints))
                .ifPresent(o ->
                        data
                                .setLeastPointsEvent(o.getEvent())
                                .setLeastPoints(o.getCaptainPoints())
                                .setLeastPointsWebName(webNameMap.getOrDefault(String.valueOf(o.getPlayedCaptain()), ""))
                );
        // tc
        entryEventResultEntityList
                .stream()
                .filter(o -> StringUtils.equals(Chip.TC.getValue(), o.getEventChip()))
                .findFirst()
                .ifPresent(o ->
                        data
                                .setTcPlayed(true)
                                .setTcEvent(o.getEvent())
                                .setTcPoints(o.getCaptainPoints())
                                .setTcPointsWebName(webNameMap.getOrDefault(String.valueOf(o.getPlayedCaptain()), ""))
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
                        .map(o -> {
                            EntrySelectedCaptainData entrySelectedCaptainData = new EntrySelectedCaptainData()
                                    .setElement(o.getKey())
                                    .setWebName(webNameMap.getOrDefault(String.valueOf(o.getKey()), ""))
                                    .setEvent(0)
                                    .setPoints(0)
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
                                    );
                            entrySelectedCaptainData.setTotalPointsByPercent(CommonUtils.getPercentResult(entrySelectedCaptainData.getTotalPoints(), data.getTotalPoints()));
                            return entrySelectedCaptainData;
                        })
                        .collect(Collectors.toList())
        );
        // captain_list
        data.setCaptainList(
                entryEventResultEntityList
                        .stream()
                        .map(o -> {
                            int element = o.getPlayedCaptain();
                            EntrySelectedCaptainData entrySelectedCaptainData = new EntrySelectedCaptainData()
                                    .setElement(element)
                                    .setWebName(webNameMap.getOrDefault(String.valueOf(element), ""))
                                    .setEvent(o.getEvent())
                                    .setPoints(o.getCaptainPoints())
                                    .setTimes(1)
                                    .setTotalPoints(0);
                            entrySelectedCaptainData.setTotalPointsByPercent(CommonUtils.getPercentResult(entrySelectedCaptainData.getTotalPoints(), data.getTotalPoints()));
                            return entrySelectedCaptainData;
                        })
                        .sorted(Comparator.comparing(EntrySelectedCaptainData::getEvent))
                        .collect(Collectors.toList())
        );
        int overallPoints = entryEventResultEntityList
                .stream()
                .max(Comparator.comparing(EntryEventResultEntity::getOverallPoints))
                .map(EntryEventResultEntity::getOverallPoints)
                .orElse(0);
        data
                .setTotalPointsByPercent(CommonUtils.getPercentResult(data.getTotalPoints(), overallPoints))
                .setViceTotalPointsByPercent(CommonUtils.getPercentResult(data.getViceTotalPoints(), overallPoints));
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
        if (entry <= 0) {
            return data;
        }
        if (this.queryService.getCurrentEvent() <= 1) {
            return data;
        }
        // prepare
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(entry);
        if (entryInfoData == null) {
            return data;
        }
        Map<String, String> shortNameMap = this.queryService.getTeamShortNameMap();
        if (CollectionUtils.isEmpty(shortNameMap)) {
            return data;
        }
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
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
                .setEntryName(entryInfoData.getEntryName())
                .setPlayerName(entryInfoData.getPlayerName());
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

    private EntryEventTransfersData initEntryEventTransfersData(EntryEventTransfersEntity
                                                                        entryEventTransfersEntity, Map<String, String> shortNameMap, Map<String, PlayerEntity> playerMap) {
        PlayerEntity elementInEntity = playerMap.getOrDefault(String.valueOf(entryEventTransfersEntity.getElementIn()), new PlayerEntity());
        PlayerEntity elementOutEntity = playerMap.getOrDefault(String.valueOf(entryEventTransfersEntity.getElementOut()), new PlayerEntity());
        return BeanUtil.copyProperties(entryEventTransfersEntity, EntryEventTransfersData.class)
                .setElementInWebName(elementInEntity.getWebName())
                .setElementInType(elementInEntity.getElementType())
                .setElementInTypeName(Position.getNameFromElementType(elementInEntity.getElementType()))
                .setElementInTeamId(elementInEntity.getTeamId())
                .setElementInTeamShortName(shortNameMap.getOrDefault(String.valueOf(elementInEntity.getTeamId()), ""))
                .setElementInCost(entryEventTransfersEntity.getElementInCost() / 10.0)
                .setElementOutWebName(elementOutEntity.getWebName())
                .setElementOutType(elementOutEntity.getElementType())
                .setElementOutTypeName(Position.getNameFromElementType(elementOutEntity.getElementType()))
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
        if (entry <= 0) {
            return data;
        }
        // prepare
        EntryInfoData entryInfoData = this.queryService.qryEntryInfo(entry);
        if (entryInfoData == null) {
            return data;
        }
        Map<String, String> shortNameMap = this.queryService.getTeamShortNameMap();
        if (CollectionUtils.isEmpty(shortNameMap)) {
            return data;
        }
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
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
                PlayerEntity playerEntity = playerMap.get(String.valueOf(pick.getElement()));
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
                .setEntryName(entryInfoData.getEntryName())
                .setPlayerName(entryInfoData.getPlayerName())
                .setOverallPoints(entryInfoData.getOverallPoints());
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
        data.setGkpTotalPointsByPercent(CommonUtils.getPercentResult(data.getGkpTotalPoints(), data.getOverallPoints()));
        Map<String, Long> mostSelectedGkpCountMap = gkpPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(String.valueOf(o.getElement())).getWebName(), Collectors.counting()));
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
        data.setDefTotalPointsByPercent(CommonUtils.getPercentResult(data.getDefTotalPoints(), data.getOverallPoints()));
        Map<String, Long> mostSelectedDefCountMap = defPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(String.valueOf(o.getElement())).getWebName(), Collectors.counting()));
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
        data.setMidTotalPointsByPercent(CommonUtils.getPercentResult(data.getMidTotalPoints(), data.getOverallPoints()));
        Map<String, Long> mostSelectedMidCountMap = midPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(String.valueOf(o.getElement())).getWebName(), Collectors.counting()));
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
        data.setFwdTotalPointsByPercent(CommonUtils.getPercentResult(data.getFwdTotalPoints(), data.getOverallPoints()));
        Map<String, Long> mostSelectedFwdCountMap = fwdPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(String.valueOf(o.getElement())).getWebName(), Collectors.counting()));
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
            value = "api::qryLeagueSeasonInfo",
            key = "#leagueName",
            cacheManager = "apiCacheManager",
            unless = "#result.leagueName == ''"
    )
    @Override
    public LeagueSeasonInfoData qryLeagueSeasonInfo(String leagueName) {
        LeagueSeasonInfoData data = new LeagueSeasonInfoData();
        if (StringUtils.isEmpty(leagueName)) {
            return data;
        }
        // prepare
        int event = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueName, leagueName)
                .ne(LeagueEventReportEntity::getEventPoints, 0)
                .orderByDesc(LeagueEventReportEntity::getOverallPoints))
                .stream()
                .findFirst()
                .map(LeagueEventReportEntity::getEvent)
                .orElse(0);
        if (event == 0) {
            return data;
        }
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueName, leagueName)
                .eq(LeagueEventReportEntity::getEvent, event));
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
        return data
                .setLeagueName(leagueName)
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
                                map.values()
                                        .stream()
                                        .sorted(Comparator.comparing(EntrySeasonInfoData::getOverallPoints).reversed())
                                        .limit(5)
                                        .mapToDouble(EntrySeasonInfoData::getOverallPoints)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
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
                                map.values()
                                        .stream()
                                        .sorted(Comparator.comparing(EntrySeasonInfoData::getTeamValue).reversed())
                                        .limit(5)
                                        .mapToDouble(EntrySeasonInfoData::getTeamValue)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
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
                                map.values()
                                        .stream()
                                        .sorted(Comparator.comparing(EntrySeasonInfoData::getTotalTransfersCost).reversed())
                                        .limit(5)
                                        .mapToDouble(EntrySeasonInfoData::getTotalTransfersCost)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
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
                                map.values()
                                        .stream()
                                        .sorted(Comparator.comparing(EntrySeasonInfoData::getTotalBenchPoints).reversed())
                                        .limit(5)
                                        .mapToDouble(EntrySeasonInfoData::getTotalBenchPoints)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
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
                                map.values()
                                        .stream()
                                        .sorted(Comparator.comparing(EntrySeasonInfoData::getTotalAutoSubsPoints).reversed())
                                        .limit(5)
                                        .mapToDouble(EntrySeasonInfoData::getTotalAutoSubsPoints)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                );
    }

    @Cacheable(
            value = "api::qryLeagueSeasonSummary",
            key = "#leagueName+'::'+#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.leagueName == ''"
    )
    @Override
    public LeagueSeasonSummaryData qryLeagueSeasonSummary(String leagueName, int entry) {
        LeagueSeasonSummaryData data = new LeagueSeasonSummaryData();
        if (StringUtils.isEmpty(leagueName) || entry <= 0) {
            return data;
        }
        // prepare
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueName, leagueName)
                .orderByAsc(LeagueEventReportEntity::getEvent));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return data;
        }
        List<Integer> entryList = leagueEventReportEntityList
                .stream()
                .map(LeagueEventReportEntity::getEntry)
                .distinct()
                .collect(Collectors.toList());
        if (!entryList.contains(entry)) {
            return data;
        }
        int defaultNum = Math.min(leagueEventReportEntityList.size(), 5);
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
        data.setLeagueName(leagueName);
        // overall points
        data.setTopRank(
                map.values()
                        .stream()
                        .sorted(Comparator.comparing(EntrySeasonInfoData::getOverallRank))
                        .limit(defaultNum)
                        .collect(Collectors.toList())
        );
        List<Integer> overallRankList = map.values()
                .stream()
                .map(EntrySeasonInfoData::getOverallRank)
                .sorted(Comparator.comparing(Integer::intValue))
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> leagueRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, overallRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, overallRankList, leagueRankMap);
            leagueRankMap.put(overallRankList.get(i), rank);
        });
        data.setEntryOverallRank(map.get(entry).getOverallRank());
        data.setEntryLeagueRank(leagueRankMap.get(data.getEntryOverallRank()));
        // value
        data.setTopValue(
                map.values()
                        .stream()
                        .sorted(
                                Comparator.comparing(EntrySeasonInfoData::getValue)
                                        .reversed()
                                        .thenComparing(EntrySeasonInfoData::getOverallRank)
                        )
                        .limit(defaultNum)
                        .collect(Collectors.toList())
        );
        List<Double> valueList = map.values()
                .stream()
                .map(EntrySeasonInfoData::getValue)
                .sorted(Comparator.comparing(Double::doubleValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Double, Integer> valueRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, valueList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, valueList, valueRankMap);
            valueRankMap.put(valueList.get(i), rank);
        });
        data.setEntryValue(map.get(entry).getValue());
        data.setEntryValueRank(valueRankMap.get(data.getEntryValue()));
        // team_value
        data.setTopTeamValue(
                map.values()
                        .stream()
                        .sorted(
                                Comparator.comparing(EntrySeasonInfoData::getTeamValue)
                                        .reversed()
                                        .thenComparing(EntrySeasonInfoData::getOverallRank)
                        )
                        .limit(defaultNum)
                        .collect(Collectors.toList())
        );
        List<Double> teamValueList = map.values()
                .stream()
                .map(EntrySeasonInfoData::getTeamValue)
                .sorted(Comparator.comparing(Double::doubleValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Double, Integer> teamValueRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, teamValueList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, teamValueList, teamValueRankMap);
            teamValueRankMap.put(teamValueList.get(i), rank);
        });
        data.setEntryTeamValue(map.get(entry).getTeamValue());
        data.setEntryTeamValueRank(teamValueRankMap.get(data.getEntryTeamValue()));
        // bank
        data.setTopBank(
                map.values()
                        .stream()
                        .sorted(
                                Comparator.comparing(EntrySeasonInfoData::getBank)
                                        .reversed()
                                        .thenComparing(EntrySeasonInfoData::getOverallRank)
                        )
                        .limit(defaultNum)
                        .collect(Collectors.toList())
        );
        List<Double> bankList = map.values()
                .stream()
                .map(EntrySeasonInfoData::getBank)
                .sorted(Comparator.comparing(Double::doubleValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Double, Integer> bankRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, valueList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, bankList, bankRankMap);
            bankRankMap.put(bankList.get(i), rank);
        });
        data.setEntryBank(map.get(entry).getBank());
        data.setEntryBankRank(bankRankMap.get(data.getEntryBank()));
        // transfers
        data.setTopTransfers(
                map.values()
                        .stream()
                        .filter(o -> o.getTotalTransfers() > 0)
                        .sorted(Comparator.comparing(EntrySeasonInfoData::getTotalTransfers).reversed())
                        .limit(defaultNum)
                        .collect(Collectors.toList())
        );
        List<Integer> transfersRankList = map.values()
                .stream()
                .map(EntrySeasonInfoData::getTotalTransfers)
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> transfersRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, transfersRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, transfersRankList, transfersRankMap);
            transfersRankMap.put(transfersRankList.get(i), rank);
        });
        data.setEntryTransfers(map.get(entry).getTotalTransfers());
        data.setEntryTransfersRank(transfersRankMap.get(data.getEntryTransfers()));
        // cost
        data.setTopCost(
                map.values()
                        .stream()
                        .filter(o -> o.getTotalTransfersCost() > 0)
                        .sorted(Comparator.comparing(EntrySeasonInfoData::getTotalTransfersCost).reversed())
                        .limit(defaultNum)
                        .collect(Collectors.toList())
        );
        List<Integer> costRankList = map.values()
                .stream()
                .map(EntrySeasonInfoData::getTotalTransfersCost)
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> costRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, costRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, costRankList, costRankMap);
            costRankMap.put(costRankList.get(i), rank);
        });
        data.setEntryCost(map.get(entry).getTotalTransfersCost());
        data.setEntryCostRank(costRankMap.get(data.getEntryCost()));
        // bench
        data.setTopBench(
                map.values()
                        .stream()
                        .filter(o -> o.getBank() > 0)
                        .sorted(Comparator.comparing(EntrySeasonInfoData::getTotalBenchPoints).reversed())
                        .limit(defaultNum)
                        .collect(Collectors.toList())
        );
        List<Integer> benchPointsRankList = map.values()
                .stream()
                .map(EntrySeasonInfoData::getTotalBenchPoints)
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> benchPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, benchPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, benchPointsRankList, benchPointsRankMap);
            benchPointsRankMap.put(benchPointsRankList.get(i), rank);
        });
        data.setEntryBenchPoints(map.get(entry).getTotalBenchPoints());
        data.setEntryBenchPointsRank(benchPointsRankMap.get(data.getEntryBenchPoints()));
        // autoSubs
        data.setTopAutoSubs(
                map.values()
                        .stream()
                        .filter(o -> o.getTotalAutoSubsPoints() > 0)
                        .sorted(Comparator.comparing(EntrySeasonInfoData::getTotalAutoSubsPoints).reversed())
                        .limit(defaultNum)
                        .collect(Collectors.toList())
        );
        List<Integer> autoSubsPointsRankList = map.values()
                .stream()
                .map(EntrySeasonInfoData::getTotalAutoSubsPoints)
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> autoSubsPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, autoSubsPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, autoSubsPointsRankList, autoSubsPointsRankMap);
            autoSubsPointsRankMap.put(autoSubsPointsRankList.get(i), rank);
        });
        data.setEntryAutoSubsPoints(map.get(entry).getTotalAutoSubsPoints());
        data.setEntryAutoSubsPointsRank(autoSubsPointsRankMap.get(data.getEntryAutoSubsPoints()));
        // above hundred
        Multimap<Integer, EntryAboveHundredData> aboveHundredEntityMap = HashMultimap.create();
        leagueEventReportEntityList
                .stream()
                .filter(o -> o.getEventPoints() >= 100)
                .forEach(o -> aboveHundredEntityMap.put(o.getEntry(),
                        new EntryAboveHundredData()
                                .setEvent(o.getEvent())
                                .setEntry(o.getEntry())
                                .setEntryName(o.getEntryName())
                                .setPlayerName(o.getPlayerName())
                                .setPoint(o.getEventPoints())
                                .setTransfers(o.getEventTransfers())
                                .setCost(o.getEventTransfersCost())
                                .setNetPoints(o.getEventNetPoints())
                                .setChip(o.getEventChip()))
                );
        Map<Integer, Long> entryAboveHundredCountMap = aboveHundredEntityMap.values()
                .stream()
                .collect(Collectors.groupingBy(EntryAboveHundredData::getEntry, Collectors.counting()));
        List<EntryAboveHundredData> topAboveHundred = Lists.newArrayList();
        entryAboveHundredCountMap.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(Math.min(entryAboveHundredCountMap.size(), defaultNum))
                .forEach(o -> topAboveHundred.addAll(aboveHundredEntityMap.get(o.getKey())));
        data.setTopAboveHundred(topAboveHundred);
        List<Integer> aboveHundredRankList = aboveHundredEntityMap.keySet()
                .stream()
                .map(o -> aboveHundredEntityMap.get(o).size())
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> aboveHundredRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, aboveHundredRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, aboveHundredRankList, aboveHundredRankMap);
            aboveHundredRankMap.put(aboveHundredRankList.get(i), rank);
        });
        if (aboveHundredEntityMap.containsKey(entry)) {
            data.setEntryAboveHundredTimes(aboveHundredEntityMap.get(entry).size());
            data.setEntryAboveHundredRank(aboveHundredRankMap.get(data.getEntryAboveHundredTimes()));
        } else {
            data.setEntryAboveHundredTimes(0);
            data.setEntryAboveHundredRank(aboveHundredRankList.size() + 1);
        }
        return data;
    }

    private EntrySeasonInfoData initLeagueEntryInfoData(LeagueEventReportEntity
                                                                leagueEventReportEntity, Map<Integer, EntrySeasonInfoData> map) {
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
            data
                    .setTotalTransfers(data.getTotalTransfers() + leagueEventReportEntity.getEventTransfers())
                    .setTotalTransfersCost(data.getTotalTransfersCost() + leagueEventReportEntity.getEventTransfersCost())
                    .setTotalBenchPoints(data.getTotalBenchPoints() + leagueEventReportEntity.getEventBenchPoints())
                    .setTotalAutoSubsPoints(data.getTotalAutoSubsPoints() + leagueEventReportEntity.getEventAutoSubPoints());
            if (data.getEvent() < leagueEventReportEntity.getEvent()) {
                data
                        .setEvent(leagueEventReportEntity.getEvent())
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
            key = "#leagueName+'::'+#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.leagueName == ''"
    )
    @Override
    public LeagueSeasonCaptainData qryLeagueSeasonCaptain(String leagueName, int entry) {
        LeagueSeasonCaptainData data = new LeagueSeasonCaptainData();
        if (StringUtils.isEmpty(leagueName) || entry <= 0) {
            return data;
        }
        // prepare
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueName, leagueName)
                .ne(LeagueEventReportEntity::getEventPoints, 0)
                .orderByAsc(LeagueEventReportEntity::getEvent));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return data;
        }
        List<Integer> entryList = leagueEventReportEntityList
                .stream()
                .map(LeagueEventReportEntity::getEntry)
                .distinct()
                .collect(Collectors.toList());
        if (!entryList.contains(entry)) {
            return data;
        }
        Multimap<Integer, EntryEventCaptainData> map = HashMultimap.create();
        leagueEventReportEntityList.forEach(o -> {
            EntryEventCaptainData entryEventCaptainData = new EntryEventCaptainData()
                    .setEvent(o.getEvent())
                    .setEntry(o.getEntry())
                    .setCaptain(o.getCaptain())
                    .setViceCaptain(o.getViceCaptain())
                    .setPlayedCaptain(o.getPlayedCaptain())
                    .setPlayedCaptainPoints(o.getPlayedCaptain() == o.getCaptain() ? o.getCaptainPoints() : o.getViceCaptainPoints())
                    .setPoints(o.getEventPoints())
                    .setChip(o.getEventChip())
                    .setOverallPoints(o.getOverallPoints());
            entryEventCaptainData.setCaptainPoints(StringUtils.equalsIgnoreCase(Chip.TC.getValue(), entryEventCaptainData.getChip()) ?
                    3 * entryEventCaptainData.getPlayedCaptainPoints() : 2 * entryEventCaptainData.getPlayedCaptainPoints());
            map.put(o.getEntry(), entryEventCaptainData);
        });
        int defaultNum = Math.min(map.size(), 5);
        Map<Integer, Integer> entryCaptainPointsMap = map.keySet()
                .stream()
                .collect(Collectors.toMap(k -> k,
                        v -> map.get(v)
                                .stream()
                                .mapToInt(EntryEventCaptainData::getCaptainPoints)
                                .sum()
                ));
        Map<String, String> webNameMap = this.queryService.qryPlayerWebNameMap();
        if (CollectionUtils.isEmpty(webNameMap)) {
            return data;
        }
        // collect
        data
                .setLeagueName(leagueName)
                .setTotalCaptainNum(
                        (int) map.values()
                                .stream()
                                .map(EntryEventCaptainData::getPlayedCaptain)
                                .distinct()
                                .count()
                )
                .setAverageCaptainNum(
                        NumberUtil.round(
                                map.keySet()
                                        .stream()
                                        .mapToLong(o ->
                                                map.get(o)
                                                        .stream()
                                                        .map(EntryEventCaptainData::getPlayedCaptain)
                                                        .distinct()
                                                        .count()
                                        )
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setTotalCaptainPoints(
                        map.values()
                                .stream()
                                .mapToInt(EntryEventCaptainData::getCaptainPoints)
                                .sum()
                )
                .setAverageCaptainPoints(
                        NumberUtil.round(
                                map.keySet()
                                        .stream()
                                        .mapToDouble(o ->
                                                map.get(o)
                                                        .stream()
                                                        .mapToInt(EntryEventCaptainData::getCaptainPoints)
                                                        .average()
                                                        .orElse(0)
                                        )
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .setEntryCaptainTotalPoints(entryCaptainPointsMap.get(entry))
                .setEntryAverageCaptainPoints(
                        NumberUtil.round(
                                map.get(entry)
                                        .stream()
                                        .mapToInt(EntryEventCaptainData::getCaptainPoints)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                );
        // captain points rank
        List<Integer> captainTotalPointsRankList = map.keySet()
                .stream()
                .map(o ->
                        map.get(o)
                                .stream()
                                .mapToInt(EntryEventCaptainData::getCaptainPoints)
                                .sum()
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> captainTotalPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, captainTotalPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, captainTotalPointsRankList, captainTotalPointsRankMap);
            captainTotalPointsRankMap.put(captainTotalPointsRankList.get(i), rank);
        });
        data.setEntryCaptainTotalPointsRank(captainTotalPointsRankMap.get(data.getEntryCaptainTotalPoints()));
        // average captain points rank
        List<Double> averageCaptainTotalPointsRankList = map.keySet()
                .stream()
                .map(o ->
                        NumberUtil.round(
                                map.get(o)
                                        .stream()
                                        .mapToInt(EntryEventCaptainData::getCaptainPoints)
                                        .average()
                                        .orElse(0)
                                , 2)
                                .doubleValue()
                )
                .sorted(Comparator.comparing(Double::doubleValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Double, Integer> averageCaptainTotalPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, averageCaptainTotalPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, averageCaptainTotalPointsRankList, averageCaptainTotalPointsRankMap);
            averageCaptainTotalPointsRankMap.put(averageCaptainTotalPointsRankList.get(i), rank);
        });
        data.setEntryAverageCaptainPointsRank(averageCaptainTotalPointsRankMap.get(data.getEntryAverageCaptainPoints()))
                .setMostPointsCaptain(
                        map.values()
                                .stream()
                                .collect(Collectors.toMap(EntryEventCaptainData::getPlayedCaptain, EntryEventCaptainData::getCaptainPoints, Integer::sum, HashMap::new))
                                .entrySet()
                                .stream()
                                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                                .limit(defaultNum)
                                .map(o ->
                                        new EntrySelectedCaptainData()
                                                .setElement(o.getKey())
                                                .setWebName(webNameMap.getOrDefault(String.valueOf(o.getKey()), ""))
                                                .setTimes(
                                                        map.values()
                                                                .stream()
                                                                .filter(i -> i.getPlayedCaptain() == o.getKey())
                                                                .count()
                                                )
                                                .setTotalPoints(o.getValue())
                                                .setTotalPointsByPercent(CommonUtils.getPercentResult(o.getValue(), data.getTotalCaptainPoints()))
                                )
                                .collect(Collectors.toList())
                )
                .setMostSelectedCaptain(
                        map.values()
                                .stream()
                                .collect(Collectors.groupingBy(EntryEventCaptainData::getCaptain, Collectors.counting()))
                                .entrySet()
                                .stream()
                                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                                .limit(defaultNum)
                                .map(o ->
                                        new EntrySelectedCaptainData()
                                                .setElement(o.getKey())
                                                .setWebName(webNameMap.getOrDefault(String.valueOf(o.getKey()), ""))
                                                .setTimes(o.getValue())
                                                .setTotalPoints(0)
                                                .setTotalPointsByPercent("")
                                )
                                .collect(Collectors.toList())
                )
                .setMostTcSelectedCaptain(
                        map.values()
                                .stream()
                                .filter(o -> StringUtils.equalsIgnoreCase(Chip.TC.getValue(), o.getChip()))
                                .collect(Collectors.groupingBy(EntryEventCaptainData::getPlayedCaptain, Collectors.counting()))
                                .entrySet()
                                .stream()
                                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                                .limit(3)
                                .map(o ->
                                        new EntrySelectedCaptainData()
                                                .setElement(o.getKey())
                                                .setWebName(webNameMap.getOrDefault(String.valueOf(o.getKey()), ""))
                                                .setTimes(o.getValue())
                                                .setTotalPoints(0)
                                                .setTotalPointsByPercent("")
                                )
                                .collect(Collectors.toList())
                )
                .setBestCaptainEntry(
                        entryCaptainPointsMap.entrySet()
                                .stream()
                                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                                .limit(defaultNum)
                                .map(o -> {
                                    int leagueEntry = o.getKey();
                                    EntryInfoData entryInfoData = this.queryService.qryEntryInfo(leagueEntry);
                                    if (entryInfoData == null) {
                                        return null;
                                    }
                                    return new EntrySeasonCaptainData()
                                            .setEntry(leagueEntry)
                                            .setEntryName(entryInfoData.getEntryName())
                                            .setPlayerName(entryInfoData.getPlayerName())
                                            .setTotalPoints(o.getValue())
                                            .setTotalPointsByPercent(CommonUtils.getPercentResult(o.getValue(), entryInfoData.getOverallPoints()));
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                )
                .setWorstCaptainEntry(
                        entryCaptainPointsMap.entrySet()
                                .stream()
                                .sorted(Map.Entry.comparingByValue())
                                .limit(defaultNum)
                                .map(o -> {
                                    int leagueEntry = o.getKey();
                                    EntryInfoData entryInfoData = this.queryService.qryEntryInfo(leagueEntry);
                                    if (entryInfoData == null) {
                                        return null;
                                    }
                                    return new EntrySeasonCaptainData()
                                            .setEntry(leagueEntry)
                                            .setEntryName(entryInfoData.getEntryName())
                                            .setPlayerName(entryInfoData.getPlayerName())
                                            .setTotalPoints(o.getValue())
                                            .setTotalPointsByPercent(CommonUtils.getPercentResult(o.getValue(), entryInfoData.getOverallPoints()));
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
        // captain points by percent
        Map<Integer, Double> entryCaptainPointsByPercentMap = Maps.newHashMap();
        entryCaptainPointsMap.keySet().forEach(leagueEntry -> entryCaptainPointsByPercentMap.put(leagueEntry,
                NumberUtil.round(
                        NumberUtil.div(entryCaptainPointsMap.get(leagueEntry) * 1.0,
                                map.get(leagueEntry)
                                        .stream()
                                        .max(Comparator.comparing(EntryEventCaptainData::getEvent))
                                        .orElse(new EntryEventCaptainData())
                                        .getOverallPoints()),
                        4).doubleValue()
        ));
        data
                .setMostPointsByPercentEntry(
                        entryCaptainPointsByPercentMap.entrySet()
                                .stream()
                                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                                .limit(defaultNum)
                                .map(o -> {
                                    int leagueEntry = o.getKey();
                                    EntryInfoData entryInfoData = this.queryService.qryEntryInfo(leagueEntry);
                                    if (entryInfoData == null) {
                                        return null;
                                    }
                                    return new EntrySeasonCaptainData()
                                            .setEntry(leagueEntry)
                                            .setEntryName(entryInfoData.getEntryName())
                                            .setPlayerName(entryInfoData.getPlayerName())
                                            .setTotalPoints(entryCaptainPointsMap.get(leagueEntry))
                                            .setTotalPointsByPercent(NumberUtil.formatPercent(o.getValue(), 2));
                                })
                                .collect(Collectors.toList())
                )
                .setLeastPointsByPercentEntry(
                        entryCaptainPointsByPercentMap.entrySet()
                                .stream()
                                .sorted(Map.Entry.comparingByValue())
                                .limit(defaultNum)
                                .map(o -> {
                                    int leagueEntry = o.getKey();
                                    EntryInfoData entryInfoData = this.queryService.qryEntryInfo(leagueEntry);
                                    if (entryInfoData == null) {
                                        return null;
                                    }
                                    return new EntrySeasonCaptainData()
                                            .setEntry(leagueEntry)
                                            .setEntryName(entryInfoData.getEntryName())
                                            .setPlayerName(entryInfoData.getPlayerName())
                                            .setTotalPoints(entryCaptainPointsMap.get(leagueEntry))
                                            .setTotalPointsByPercent(NumberUtil.formatPercent(o.getValue(), 2));
                                })
                                .collect(Collectors.toList())
                );
        // entry most captain points
        data.setEntryMostCaptainPoints(
                map.get(entry)
                        .stream()
                        .map(EntryEventCaptainData::getPlayedCaptainPoints)
                        .max(Comparator.comparing(Integer::intValue))
                        .orElse(0)
        );
        List<Integer> entryMostCaptainPointsRankList = map.keySet()
                .stream()
                .map(o ->
                        map.get(o)
                                .stream()
                                .map(EntryEventCaptainData::getPlayedCaptainPoints)
                                .max(Comparator.comparing(Integer::intValue))
                                .orElse(0)
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> entryMostCaptainPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, entryMostCaptainPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, entryMostCaptainPointsRankList, entryMostCaptainPointsRankMap);
            entryMostCaptainPointsRankMap.put(entryMostCaptainPointsRankList.get(i), rank);
        });
        data.setEntryMostCaptainPointsRank(entryMostCaptainPointsRankMap.get(data.getEntryMostCaptainPoints()));
        // entry tc captain points
        EntryEventCaptainData tcCaptainData = map.get(entry)
                .stream()
                .filter(o -> StringUtils.equalsIgnoreCase(Chip.TC.getValue(), o.getChip()))
                .max(Comparator.comparing(EntryEventCaptainData::getPlayedCaptainPoints))
                .orElse(null);
        if (tcCaptainData != null) {
            data
                    .setEntryTcCaptainPlayed(true)
                    .setEntryTcCaptainPoints(tcCaptainData.getPlayedCaptainPoints());
            List<Integer> entryTcCaptainPointsRankList = map.values()
                    .stream()
                    .filter(o -> StringUtils.equalsIgnoreCase(Chip.TC.getValue(), o.getChip()))
                    .map(EntryEventCaptainData::getPlayedCaptainPoints)
                    .sorted(Comparator.comparing(Integer::intValue).reversed())
                    .collect(Collectors.toList());
            LinkedHashMap<Integer, Integer> entryTcCaptainPointsRankMap = Maps.newLinkedHashMap();
            IntStream.range(0, entryTcCaptainPointsRankList.size()).forEach(i -> {
                int rank = this.calcRealRank(i, entryTcCaptainPointsRankList, entryTcCaptainPointsRankMap);
                entryTcCaptainPointsRankMap.put(entryTcCaptainPointsRankList.get(i), rank);
            });
            data.setEntryTcCaptainPointsRank(entryTcCaptainPointsRankMap.get(data.getEntryTcCaptainPoints()));
        }
        // entry captain points by percent
        data.setEntryCaptainPointsByPercent(NumberUtil.formatPercent(entryCaptainPointsByPercentMap.get(entry), 2));
        List<Double> entryCaptainPointsByPercentRankList = entryCaptainPointsByPercentMap.values()
                .stream()
                .sorted(Comparator.comparing(Double::doubleValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Double, Integer> entryCaptainPointsByPercentRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, entryCaptainPointsByPercentRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, entryCaptainPointsByPercentRankList, entryCaptainPointsByPercentRankMap);
            entryCaptainPointsByPercentRankMap.put(entryCaptainPointsByPercentRankList.get(i), rank);
        });
        data.setEntryCaptainPointsByPercentRank(entryCaptainPointsByPercentRankMap.get(entryCaptainPointsByPercentMap.get(entry)));
        return data;
    }

    @Cacheable(
            value = "api::qryLeagueSeasonScore",
            key = "#leagueName+'::'+#entry",
            cacheManager = "apiCacheManager",
            unless = "#result.leagueName == ''"
    )
    @Override
    public LeagueSeasonScoreData qryLeagueSeasonScore(String leagueName, int entry) {
        LeagueSeasonScoreData data = new LeagueSeasonScoreData();
        if (StringUtils.isEmpty(leagueName) || entry <= 0) {
            return data;
        }
        // prepare
        int current = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueName, leagueName)
                .ne(LeagueEventReportEntity::getEventPoints, 0)
                .orderByDesc(LeagueEventReportEntity::getOverallPoints))
                .stream()
                .findFirst()
                .map(LeagueEventReportEntity::getEvent)
                .orElse(0);
        if (current == 0) {
            return data;
        }
        List<LeagueEventReportEntity> leagueEventReportEntityList = this.leagueEventReportService.list(new QueryWrapper<LeagueEventReportEntity>().lambda()
                .eq(LeagueEventReportEntity::getLeagueName, leagueName)
                .eq(LeagueEventReportEntity::getEvent, current));
        if (CollectionUtils.isEmpty(leagueEventReportEntityList)) {
            return data;
        }
        List<Integer> entryList = leagueEventReportEntityList
                .stream()
                .map(LeagueEventReportEntity::getEntry)
                .distinct()
                .collect(Collectors.toList());
        if (!entryList.contains(entry)) {
            return data;
        }
        int defaultNum = Math.min(leagueEventReportEntityList.size(), 5);
        Map<String, String> shortNameMap = this.queryService.getTeamShortNameMap();
        if (CollectionUtils.isEmpty(shortNameMap)) {
            return data;
        }
        Map<String, PlayerEntity> playerMap = this.queryService.getPlayerMap();
        if (CollectionUtils.isEmpty(playerMap)) {
            return data;
        }
        Table<Integer, Integer, EventLiveEntity> eventLiveTable = HashBasedTable.create(); // element -> event -> data
        this.eventLiveService.list().forEach(o -> eventLiveTable.put(o.getElement(), o.getEvent(), o));
        // collect
        List<EntryPickData> entryPickDataList = Lists.newArrayList();
        leagueEventReportEntityList.forEach(o -> {
            int event = o.getEvent();
            int leagueEntry = o.getEntry();
            if (StringUtils.equalsIgnoreCase(Chip.BB.getValue(), o.getEventChip())) {
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition1(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition2(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition3(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition4(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition5(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition6(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition7(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition8(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition9(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition10(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition11(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition12(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition13(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition14(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition15(), shortNameMap, playerMap, eventLiveTable));
            } else {
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition1(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition2(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition3(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition4(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition5(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition6(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition7(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition8(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition9(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition10(), shortNameMap, playerMap, eventLiveTable));
                entryPickDataList.add(this.initLeagueEntryPickData(event, leagueEntry, o.getPosition11(), shortNameMap, playerMap, eventLiveTable));
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
            int leagueEntry = o.getEntry();
            Map<Integer, List<EntryPickData>> map = Maps.newHashMap();
            if (entryPickTable.contains(leagueEntry, event)) {
                map = entryPickTable.get(leagueEntry, event);
            }
            if (CollectionUtils.isEmpty(map)) {
                map = Maps.newHashMap();
            }
            int elementType = o.getElementType();
            List<EntryPickData> list = map.getOrDefault(elementType, Lists.newArrayList());
            list.add(o);
            map.put(elementType, list);
            entryPickTable.put(leagueEntry, event, map);
        });
        data
                .setLeagueName(leagueName)
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
        data.setGkpTotalPointsByPercent(CommonUtils.getPercentResult(data.getGkpTotalPoints(), data.getTotalOverallPoints()));
        Map<String, Long> mostSelectedGkpCountMap = gkpPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(String.valueOf(o.getElement())).getWebName(), Collectors.counting()));
        data.setMostSelectedGkpByPercent(
                mostSelectedGkpCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(defaultNum)
                        .map(o ->
                                new MapData<String>()
                                        .setKey(o.getKey())
                                        .setValue(CommonUtils.getPercentResult(o.getValue().intValue(), gkpPickList.size()))
                        )
                        .collect(Collectors.toList())
        );
        // league entry gkp
        Multimap<Integer, EntryPickData> entryGkpPickMap = HashMultimap.create();
        entryPickTable.rowKeySet().forEach(leagueEntry -> entryPickTable.row(leagueEntry).values().forEach(i -> i.get(Position.GKP.getElementType()).forEach(j -> entryGkpPickMap.put(leagueEntry, j))));
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
                                .limit(defaultNum)
                                .map(o -> {
                                    EntryInfoData entryInfoData = this.apiQueryService.qryEntryInfo(o.getKey());
                                    if (entryInfoData == null) {
                                        return null;
                                    }
                                    return new EntryElementTypeScoreData()
                                            .setEntry(o.getKey())
                                            .setEntryName(entryInfoData.getEntryName())
                                            .setPlayerName(entryInfoData.getPlayerName())
                                            .setTotalPoints(o.getValue());
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
        // entry gkp
        data
                .setEntryGkpTotalNum(
                        (int) entryGkpPickMap.get(entry)
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                )
                .setEntryGkpTotalPoints(entryGkpPointsMap.get(entry));
        List<Integer> entryGkpNumRankList = entryGkpPickMap.keySet()
                .stream()
                .map(o ->
                        (int) entryGkpPickMap.get(o)
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> entryGkpNumRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, entryGkpNumRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, entryGkpNumRankList, entryGkpNumRankMap);
            entryGkpNumRankMap.put(entryGkpNumRankList.get(i), rank);
        });
        data.setEntryGkpTotalNumRank(entryGkpNumRankMap.get(data.getEntryGkpTotalNum()));
        List<Integer> entryGkpPointsRankList = entryGkpPointsMap.values()
                .stream()
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> entryGkpPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, entryGkpPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, entryGkpPointsRankList, entryGkpPointsRankMap);
            entryGkpPointsRankMap.put(entryGkpPointsRankList.get(i), rank);
        });
        data.setEntryGkpTotalPointsRank(entryGkpPointsRankMap.get(data.getEntryGkpTotalPoints()));
        // def
        List<EntryPickData> defPickList = Lists.newArrayList();
        pickTable.row(Position.DEF.getElementType()).values().forEach(defPickList::addAll);
        data.setDefTotalPoints(
                defPickList
                        .stream()
                        .mapToInt(EntryPickData::getPoints)
                        .sum()
        );
        data.setDefTotalPointsByPercent(CommonUtils.getPercentResult(data.getDefTotalPoints(), data.getTotalOverallPoints()));
        Map<String, Long> mostSelectedDefCountMap = defPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(String.valueOf(o.getElement())).getWebName(), Collectors.counting()));
        data.setMostSelectedDefByPercent(
                mostSelectedDefCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(defaultNum)
                        .map(o ->
                                new MapData<String>()
                                        .setKey(o.getKey())
                                        .setValue(CommonUtils.getPercentResult(o.getValue().intValue(), defPickList.size()))
                        )
                        .collect(Collectors.toList())
        );
        // league entry def
        Multimap<Integer, EntryPickData> entryDefPickMap = HashMultimap.create();
        entryPickTable.rowKeySet().forEach(leagueEntry -> entryPickTable.row(leagueEntry).values().forEach(i -> i.get(Position.DEF.getElementType()).forEach(j -> entryDefPickMap.put(leagueEntry, j))));
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
                                .limit(defaultNum)
                                .map(o -> {
                                    EntryInfoData entryInfoData = this.apiQueryService.qryEntryInfo(o.getKey());
                                    if (entryInfoData == null) {
                                        return null;
                                    }
                                    return new EntryElementTypeScoreData()
                                            .setEntry(o.getKey())
                                            .setEntryName(entryInfoData.getEntryName())
                                            .setPlayerName(entryInfoData.getPlayerName())
                                            .setTotalPoints(o.getValue());
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
        // entry def
        data
                .setEntryDefTotalNum(
                        (int) entryDefPickMap.get(entry)
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                )
                .setEntryDefTotalPoints(entryDefPointsMap.get(entry));
        List<Integer> entryDefNumRankList = entryDefPickMap.keySet()
                .stream()
                .map(o ->
                        (int) entryDefPickMap.get(o)
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> entryDefNumRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, entryDefNumRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, entryDefNumRankList, entryDefNumRankMap);
            entryDefNumRankMap.put(entryDefNumRankList.get(i), rank);
        });
        data.setEntryDefTotalNumRank(entryDefNumRankMap.get(data.getEntryDefTotalNum()));
        List<Integer> entryDefPointsRankList = entryDefPointsMap.values()
                .stream()
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> entryDefPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, entryDefPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, entryDefPointsRankList, entryDefPointsRankMap);
            entryDefPointsRankMap.put(entryDefPointsRankList.get(i), rank);
        });
        data.setEntryDefTotalPointsRank(entryDefPointsRankMap.get(data.getEntryDefTotalPoints()));
        // mid
        List<EntryPickData> midPickList = Lists.newArrayList();
        pickTable.row(Position.MID.getElementType()).values().forEach(midPickList::addAll);
        data.setMidTotalPoints(
                midPickList
                        .stream()
                        .mapToInt(EntryPickData::getPoints)
                        .sum()
        );
        data.setMidTotalPointsByPercent(CommonUtils.getPercentResult(data.getMidTotalPoints(), data.getTotalOverallPoints()));
        Map<String, Long> mostSelectedMidCountMap = midPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(String.valueOf(o.getElement())).getWebName(), Collectors.counting()));
        data.setMostSelectedMidByPercent(
                mostSelectedMidCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(defaultNum)
                        .map(o ->
                                new MapData<String>()
                                        .setKey(o.getKey())
                                        .setValue(CommonUtils.getPercentResult(o.getValue().intValue(), midPickList.size()))
                        )
                        .collect(Collectors.toList())
        );
        // league entry mid
        Multimap<Integer, EntryPickData> entryMidPickMap = HashMultimap.create();
        entryPickTable.rowKeySet().forEach(leagueEntry -> entryPickTable.row(leagueEntry).values().forEach(i -> i.get(Position.MID.getElementType()).forEach(j -> entryMidPickMap.put(leagueEntry, j))));
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
                                .limit(defaultNum)
                                .map(o -> {
                                    EntryInfoData entryInfoData = this.apiQueryService.qryEntryInfo(o.getKey());
                                    if (entryInfoData == null) {
                                        return null;
                                    }
                                    return new EntryElementTypeScoreData()
                                            .setEntry(o.getKey())
                                            .setEntryName(entryInfoData.getEntryName())
                                            .setPlayerName(entryInfoData.getPlayerName())
                                            .setTotalPoints(o.getValue());
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
        // entry mid
        data
                .setEntryMidTotalNum(
                        (int) entryMidPickMap.get(entry)
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                )
                .setEntryMidTotalPoints(entryMidPointsMap.get(entry));
        List<Integer> entryMidNumRankList = entryMidPickMap.keySet()
                .stream()
                .map(o ->
                        (int) entryMidPickMap.get(o)
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> entryMidNumRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, entryMidNumRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, entryMidNumRankList, entryMidNumRankMap);
            entryMidNumRankMap.put(entryMidNumRankList.get(i), rank);
        });
        data.setEntryMidTotalNumRank(entryMidNumRankMap.get(data.getEntryMidTotalNum()));
        List<Integer> entryMidPointsRankList = entryMidPointsMap.values()
                .stream()
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> entryMidPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, entryMidPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, entryMidPointsRankList, entryMidPointsRankMap);
            entryMidPointsRankMap.put(entryMidPointsRankList.get(i), rank);
        });
        data.setEntryMidTotalPointsRank(entryMidPointsRankMap.get(data.getEntryMidTotalPoints()));
        // fwd
        List<EntryPickData> fwdPickList = Lists.newArrayList();
        pickTable.row(Position.FWD.getElementType()).values().forEach(fwdPickList::addAll);
        data.setFwdTotalPoints(
                fwdPickList
                        .stream()
                        .mapToInt(EntryPickData::getPoints)
                        .sum()
        );
        data.setFwdTotalPointsByPercent(CommonUtils.getPercentResult(data.getFwdTotalPoints(), data.getTotalOverallPoints()));
        Map<String, Long> mostSelectedFwdCountMap = fwdPickList.
                stream()
                .collect(Collectors.groupingBy(o -> playerMap.get(String.valueOf(o.getElement())).getWebName(), Collectors.counting()));
        data.setMostSelectedFwdByPercent(
                mostSelectedFwdCountMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(defaultNum)
                        .map(o ->
                                new MapData<String>()
                                        .setKey(o.getKey())
                                        .setValue(CommonUtils.getPercentResult(o.getValue().intValue(), fwdPickList.size()))
                        )
                        .collect(Collectors.toList())
        );
        // league entry fwd
        Multimap<Integer, EntryPickData> entryFwdPickMap = HashMultimap.create();
        entryPickTable.rowKeySet().forEach(leagueEntry -> entryPickTable.row(leagueEntry).values().forEach(i -> i.get(Position.FWD.getElementType()).forEach(j -> entryFwdPickMap.put(leagueEntry, j))));
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
                                .limit(defaultNum)
                                .map(o -> {
                                    EntryInfoData entryInfoData = this.apiQueryService.qryEntryInfo(o.getKey());
                                    if (entryInfoData == null) {
                                        return null;
                                    }
                                    return new EntryElementTypeScoreData()
                                            .setEntry(o.getKey())
                                            .setEntryName(entryInfoData.getEntryName())
                                            .setPlayerName(entryInfoData.getPlayerName())
                                            .setTotalPoints(o.getValue());
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                );
        // entry fwd
        data
                .setEntryFwdTotalNum(
                        (int) entryFwdPickMap.get(entry)
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                )
                .setEntryFwdTotalPoints(entryFwdPointsMap.get(entry));
        List<Integer> entryFwdNumRankList = entryFwdPickMap.keySet()
                .stream()
                .map(o ->
                        (int) entryFwdPickMap.get(o)
                                .stream()
                                .map(EntryPickData::getElement)
                                .distinct()
                                .count()
                )
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> entryFwdNumRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, entryFwdNumRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, entryFwdNumRankList, entryFwdNumRankMap);
            entryFwdNumRankMap.put(entryFwdNumRankList.get(i), rank);
        });
        data.setEntryFwdTotalNumRank(entryFwdNumRankMap.get(data.getEntryFwdTotalNum()));
        List<Integer> entryFwdPointsRankList = entryFwdPointsMap.values()
                .stream()
                .sorted(Comparator.comparing(Integer::intValue).reversed())
                .collect(Collectors.toList());
        LinkedHashMap<Integer, Integer> entryFwdPointsRankMap = Maps.newLinkedHashMap();
        IntStream.range(0, entryFwdPointsRankList.size()).forEach(i -> {
            int rank = this.calcRealRank(i, entryFwdPointsRankList, entryFwdPointsRankMap);
            entryFwdPointsRankMap.put(entryFwdPointsRankList.get(i), rank);
        });
        data.setEntryFwdTotalPointsRank(entryFwdPointsRankMap.get(data.getEntryFwdTotalPoints()));
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
                        .limit(defaultNum)
                        .map(o ->
                                new MapData<String>()
                                        .setKey(o.getKey())
                                        .setValue(CommonUtils.getPercentResult(o.getValue().intValue(), formationList.size()))
                        )
                        .collect(Collectors.toList())
        );
        return data;
    }

    private EntryPickData initLeagueEntryPickData(int event, int entry, int element, Map<
            String, String> shortNameMap, Map<String, PlayerEntity> playerMap, Table<Integer, Integer, EventLiveEntity> eventLiveTable) {
        PlayerEntity playerEntity = playerMap.getOrDefault(String.valueOf(element), null);
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

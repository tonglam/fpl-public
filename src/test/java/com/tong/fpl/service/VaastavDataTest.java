package com.tong.fpl.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.EventLiveExplainEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.VaastavDataGwEntity;
import com.tong.fpl.service.db.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by tong on 2022/07/07
 */
public class VaastavDataTest extends FplApplicationTests {

    @Autowired
    private VaastavDataGwService vaastavDataGwService;
    @Autowired
    private VaastavDataGwSingleService vaastavDataGwSingleService;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private EventLiveService eventLiveService;
    @Autowired
    private EventLiveExplainService eventLiveExplainService;

    @ParameterizedTest
    @CsvSource("2122, 37")
    void fixMergeTable(String season, int gw) {
        MybatisPlusConfig.season.set(season);
        List<VaastavDataGwEntity> list = this.vaastavDataGwSingleService.list()
                .stream()
                .map(o -> {
                    VaastavDataGwEntity vaastavDataGwEntity = new VaastavDataGwEntity();
                    BeanUtil.copyProperties(o, vaastavDataGwEntity);
                    vaastavDataGwEntity
                            .setId(null)
                            .setGw(gw);
                    return vaastavDataGwEntity;
                })
                .collect(Collectors.toList());
        this.vaastavDataGwService.saveBatch(list);
        MybatisPlusConfig.season.remove();
    }

    @ParameterizedTest
    @CsvSource("2021")
    void test(String season) {
        MybatisPlusConfig.season.set(season);
        List<VaastavDataGwEntity> list = this.vaastavDataGwService.list(new QueryWrapper<VaastavDataGwEntity>().lambda()
                .eq(VaastavDataGwEntity::getElement, 254)
                .orderByAsc(VaastavDataGwEntity::getGw));
        MybatisPlusConfig.season.remove();
        System.out.println(
                list
                        .stream()
                        .mapToInt(VaastavDataGwEntity::getTotalPoints)
                        .sum()
        );
    }

    @ParameterizedTest
    @CsvSource("2122")
    void eventLive(String season) {
        MybatisPlusConfig.season.set(season);
        List<EventLiveEntity> list = Lists.newArrayList();
        // prepare
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o)); // element -> data
        List<VaastavDataGwEntity> vaastavDataGwEntityList = this.vaastavDataGwService.list();
        Map<Integer, Multimap<Integer, VaastavDataGwEntity>> dataMap = Maps.newHashMap(); // event -> element -> data
        vaastavDataGwEntityList.forEach(o -> {
            int event = o.getGw();
            int element = o.getElement();
            Multimap<Integer, VaastavDataGwEntity> eventMultiMap;
            if (!dataMap.containsKey(event)) {
                eventMultiMap = HashMultimap.create();
            } else {
                eventMultiMap = dataMap.get(event);
            }
            eventMultiMap.put(element, o);
            dataMap.put(event, eventMultiMap);
        });
        // create event_live
        int lastEvent = vaastavDataGwEntityList
                .stream()
                .map(VaastavDataGwEntity::getGw)
                .max(Comparator.naturalOrder())
                .orElse(0);
        if (lastEvent <= 1) {
            return;
        }
        IntStream.rangeClosed(1, lastEvent).forEach(event -> {
            Multimap<Integer, VaastavDataGwEntity> eventDataMap = dataMap.get(event);
            if (eventDataMap == null || eventDataMap.size() == 0) {
                System.out.println("lack of data of gw:" + event);
                return;
            }
            List<EventLiveEntity> eventLiveList = this.initEventLiveList(event, playerMap, dataMap.get(event));
            if (CollectionUtils.isEmpty(eventLiveList)) {
                return;
            }
            list.addAll(eventLiveList);
        });
        // insert
        this.eventLiveService.getBaseMapper().truncate();
        this.eventLiveService.saveBatch(list);
        System.out.println("insert event_live size is " + list.size() + "!");
        MybatisPlusConfig.season.remove();
    }

    private List<EventLiveEntity> initEventLiveList(int event, Map<Integer, PlayerEntity> playerMap, Multimap<Integer, VaastavDataGwEntity> eventDataMap) {
        List<EventLiveEntity> list = Lists.newArrayList();
        playerMap.keySet().forEach(element -> {
            PlayerEntity playerEntity = playerMap.get(element);
            if (playerEntity == null) {
                return;
            }
            Collection<VaastavDataGwEntity> eventElementDataCollection = eventDataMap.get(element);
            if (CollectionUtils.isEmpty(eventElementDataCollection)) {
                return;
            }
            eventElementDataCollection.forEach(dataGwEntity ->
                    list.add(
                            new EventLiveEntity()
                                    .setElement(element)
                                    .setElementType(playerEntity.getElementType())
                                    .setTeamId(playerEntity.getTeamId())
                                    .setEvent(event)
                                    .setFixture(String.valueOf(dataGwEntity.getFixture()))
                                    .setMinutes(dataGwEntity.getMinutes())
                                    .setGoalsScored(dataGwEntity.getGoalsScored())
                                    .setAssists(dataGwEntity.getAssist())
                                    .setCleanSheets(dataGwEntity.getCleanSheets())
                                    .setGoalsConceded(dataGwEntity.getGoalsConceded())
                                    .setOwnGoals(dataGwEntity.getOwnGoals())
                                    .setPenaltiesSaved(dataGwEntity.getPenaltiesSaved())
                                    .setPenaltiesMissed(dataGwEntity.getPenaltiesMissed())
                                    .setYellowCards(dataGwEntity.getYellowCards())
                                    .setRedCards(dataGwEntity.getRedCards())
                                    .setSaves(dataGwEntity.getSaves())
                                    .setBonus(dataGwEntity.getBonus())
                                    .setBps(dataGwEntity.getBps())
                                    .setTotalPoints(dataGwEntity.getTotalPoints())
                    ));
        });
        return list
                .stream()
                .sorted(Comparator.comparing(EventLiveEntity::getElement))
                .collect(Collectors.toList());
    }

    @ParameterizedTest
    @CsvSource("1617")
    void eventLiveExplain(String season) {
        MybatisPlusConfig.season.set(season);
        List<EventLiveExplainEntity> list = Lists.newArrayList();
        // prepare
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o)); // element -> data
        List<VaastavDataGwEntity> vaastavDataGwEntityList = this.vaastavDataGwService.list();
        Map<Integer, Multimap<Integer, VaastavDataGwEntity>> dataMap = Maps.newHashMap(); // event -> element -> data
        vaastavDataGwEntityList.forEach(o -> {
            int event = o.getGw();
            int element = o.getElement();
            Multimap<Integer, VaastavDataGwEntity> eventMultiMap;
            if (!dataMap.containsKey(event)) {
                eventMultiMap = HashMultimap.create();
            } else {
                eventMultiMap = dataMap.get(event);
            }
            eventMultiMap.put(element, o);
            dataMap.put(event, eventMultiMap);
        });
        // create event_live_explain
        int lastEvent = vaastavDataGwEntityList
                .stream()
                .map(VaastavDataGwEntity::getGw)
                .max(Comparator.naturalOrder())
                .orElse(0);
        if (lastEvent <= 1) {
            return;
        }
        IntStream.rangeClosed(1, lastEvent).forEach(event -> {
            Multimap<Integer, VaastavDataGwEntity> eventDataMap = dataMap.get(event);
            if (eventDataMap == null || eventDataMap.size() == 0) {
                System.out.println("lack of data of gw:" + event);
                return;
            }
            List<EventLiveExplainEntity> eventLiveExplainList = this.initEventLiveExplainList(event, playerMap, dataMap.get(event));
            if (CollectionUtils.isEmpty(eventLiveExplainList)) {
                return;
            }
            list.addAll(eventLiveExplainList);
        });
        // insert
        this.eventLiveExplainService.getBaseMapper().truncate();
        this.eventLiveExplainService.saveBatch(list);
        System.out.println("insert event_live_explain size is " + list.size() + "!");
        MybatisPlusConfig.season.remove();
    }

    private List<EventLiveExplainEntity> initEventLiveExplainList(int event, Map<Integer, PlayerEntity> playerMap, Multimap<Integer, VaastavDataGwEntity> eventDataMap) {
        List<EventLiveExplainEntity> list = Lists.newArrayList();
        playerMap.keySet().forEach(element -> {
            PlayerEntity playerEntity = playerMap.get(element);
            if (playerEntity == null) {
                return;
            }
            int elementType = playerEntity.getElementType();
            Collection<VaastavDataGwEntity> eventElementDataCollection = eventDataMap.get(element);
            if (CollectionUtils.isEmpty(eventElementDataCollection)) {
                return;
            }
            eventElementDataCollection.forEach(dataGwEntity ->
                    list.add(
                            new EventLiveExplainEntity()
                                    .setElement(element)
                                    .setElementType(elementType)
                                    .setTeamId(playerEntity.getTeamId())
                                    .setEvent(event)
                                    .setFixture(String.valueOf(dataGwEntity.getFixture()))
                                    .setTotalPoints(dataGwEntity.getTotalPoints())
                                    .setBps(dataGwEntity.getBps())
                                    .setBonus(dataGwEntity.getBonus())
                                    .setMinutes(dataGwEntity.getMinutes())
                                    .setMinutesPoints(this.calcPlayingPoints(dataGwEntity.getMinutes()))
                                    .setGoalsScored(dataGwEntity.getGoalsScored())
                                    .setGoalsScoredPoints(this.calcGoalsScoredPoints(elementType, dataGwEntity.getGoalsScored()))
                                    .setAssists(dataGwEntity.getAssist())
                                    .setAssistsPoints(this.calcGoalsAssistPoints(dataGwEntity.getAssist()))
                                    .setCleanSheets(dataGwEntity.getCleanSheets())
                                    .setCleanSheetsPoints(this.calcCleanSheetsPoints(elementType, dataGwEntity.getCleanSheets()))
                                    .setGoalsConceded(dataGwEntity.getGoalsConceded())
                                    .setGoalsConcededPoints(this.calcGoalsConcededPoints(elementType, dataGwEntity.getGoalsConceded()))
                                    .setOwnGoals(dataGwEntity.getOwnGoals())
                                    .setOwnGoalsPoints(this.calcOwnGoalsPoints(dataGwEntity.getOwnGoals()))
                                    .setPenaltiesSaved(dataGwEntity.getPenaltiesSaved())
                                    .setPenaltiesSavedPoints(this.calcPenaltiesSavedPoints(dataGwEntity.getPenaltiesSaved()))
                                    .setPenaltiesMissed(dataGwEntity.getPenaltiesMissed())
                                    .setPenaltiesMissedPoints(this.calcPenaltiesMissedPoints(dataGwEntity.getPenaltiesMissed()))
                                    .setYellowCards(dataGwEntity.getYellowCards())
                                    .setYellowCardsPoints(this.calcYellowCardsPoints(dataGwEntity.getYellowCards()))
                                    .setRedCards(dataGwEntity.getRedCards())
                                    .setRedCardsPoints(this.calcRedCardsPoints(dataGwEntity.getRedCards()))
                                    .setSaves(dataGwEntity.getSaves())
                                    .setSavesPoints(this.calcSavesPoints(dataGwEntity.getSaves()))
                    ));
        });
        return list
                .stream()
                .sorted(Comparator.comparing(EventLiveExplainEntity::getElement))
                .collect(Collectors.toList());
    }

    private int calcPlayingPoints(int minutes) {
        if (minutes > 0 && minutes < 60) {
            return 1;
        } else if (minutes >= 60 && minutes <= 90) {
            return 2;
        }
        return 0;
    }

    private int calcGoalsScoredPoints(int elementType, int goalsScored) {
        switch (elementType) {
            case 1:
            case 2: {
                return 6 * goalsScored;
            }
            case 3: {
                return 5 * goalsScored;
            }
            case 4: {
                return 4 * goalsScored;
            }
        }
        return 0;
    }

    private int calcGoalsAssistPoints(int assists) {
        return 3 * assists;
    }

    private int calcCleanSheetsPoints(int elementType, int cleanSheet) {
        switch (elementType) {
            case 1:
            case 2: {
                return 4 * cleanSheet;
            }
            case 3: {
                return cleanSheet;
            }
        }
        return 0;
    }

    private int calcGoalsConcededPoints(int elementType, int goalsConceded) {
        if (elementType == 1 || elementType == 2) {
            return -1 * ((int) Math.floor(goalsConceded * 1.0 / 2));
        }
        return 0;
    }

    private int calcPenaltiesSavedPoints(int penaltiesSaved) {
        return 5 * penaltiesSaved;
    }

    private int calcPenaltiesMissedPoints(int penaltiesMissed) {
        return -2 * penaltiesMissed;
    }

    private int calcOwnGoalsPoints(int ownGoals) {
        return -2 * ownGoals;
    }

    private int calcYellowCardsPoints(int yellowCards) {
        return -1 * yellowCards;
    }

    private int calcRedCardsPoints(int redCards) {
        return -3 * redCards;
    }

    private int calcSavesPoints(int saves) {
        return (int) Math.floor(saves * 1.0 / 3);
    }

}

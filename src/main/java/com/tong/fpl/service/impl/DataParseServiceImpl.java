package com.tong.fpl.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.fantasynutmeg.EventResponseData;
import com.tong.fpl.domain.fantasynutmeg.SeasonResponseData;
import com.tong.fpl.service.IDataParseService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Create by tong on 2021/8/26
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DataParseServiceImpl implements IDataParseService {

    private final PlayerService playerService;
    private final PlayerStatService playerStatService;
    private final EventService eventService;
    private final EventFixtureService eventFixtureService;
    private final EventLiveService eventLiveService;

    @Override
    public void parseNutmegSeasonData(String season, String fileName) {
        // read json
        try {
            String result = Files.readAllLines(Paths.get(fileName))
                    .stream()
                    .findFirst()
                    .orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            List<SeasonResponseData> dataList = mapper.readValue(result, new TypeReference<List<SeasonResponseData>>() {
            });
            if (CollectionUtils.isEmpty(dataList)) {
                return;
            }
            MybatisPlusConfig.season.set(season);
            // player
            this.insertIntoPlayer(dataList);
            // player stat
            this.insertIntoPlayerStat(dataList);
            MybatisPlusConfig.season.remove();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertIntoPlayer(List<SeasonResponseData> dataList) {
        List<PlayerEntity> insertList = Lists.newArrayList();
        List<PlayerEntity> updateList = Lists.newArrayList();
        Map<Integer, PlayerEntity> map = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        dataList
                .stream()
                .map(this::initPlayerData)
                .forEach(o -> {
                    if (!map.containsKey(o.getElement())) {
                        insertList.add(o);
                    } else {
                        updateList.add(o);
                    }
                });
//        this.playerService.saveBatch(insertList);
        log.info("insert player size:{}", insertList.size());
//        this.playerService.updateBatchById(updateList);
        log.info("update player size:{}", updateList.size());
    }

    private PlayerEntity initPlayerData(SeasonResponseData data) {
        return new PlayerEntity()
                .setElement(data.getId())
                .setCode(data.getCode())
                .setPrice((int) (data.getNowCost() * 10))
                .setStartPrice((int) (data.getNowCost() * 10) - data.getCostChangeStart())
                .setElementType(data.getElementType())
                .setFirstName(data.getFirstName())
                .setSecondName(data.getSecondName())
                .setWebName(data.getWebName())
                .setTeamId(data.getTeam());
    }

    private void insertIntoPlayerStat(List<SeasonResponseData> dataList) {
        List<PlayerStatEntity> insertList = Lists.newArrayList();
        List<PlayerStatEntity> updateList = Lists.newArrayList();
        Map<Integer, PlayerStatEntity> map = this.playerStatService.list()
                .stream()
                .collect(Collectors.toMap(PlayerStatEntity::getElement, o -> o));
        dataList
                .stream()
                .map(this::initPlayerStatData)
                .forEach(o -> {
                    int element = o.getElement();
                    if (!map.containsKey(element)) {
                        insertList.add(o);
                    } else {
                        o.setId(map.get(element).getId());
                        updateList.add(o);
                    }
                });
        this.playerStatService.saveBatch(insertList);
        log.info("insert player stat size:{}", insertList.size());
        this.playerStatService.updateBatchById(updateList);
        log.info("update player stat size:{}", updateList.size());
    }

    private PlayerStatEntity initPlayerStatData(SeasonResponseData data) {
        return new PlayerStatEntity()
                .setEvent(0)
                .setElement(data.getId())
                .setCode(data.getCode())
                .setMatchPlayed(0)
                .setChanceOfPlayingNextRound(StringUtils.equalsAnyIgnoreCase("none", data.getChanceOfPlayingNextRound()) ? 0 :
                        Integer.parseInt(data.getChanceOfPlayingNextRound()))
                .setChanceOfPlayingThisRound(StringUtils.equalsAnyIgnoreCase("none", data.getChanceOfPlayingThisRound()) ? 0 :
                        Integer.parseInt(data.getChanceOfPlayingThisRound()))
                .setDreamteamCount(data.getDreamteamCount())
                .setEventPoints(data.getEventPoints())
                .setForm(String.valueOf(data.getForm()))
                .setInDreamteam(data.isInDreamteam())
                .setNews(data.getNews())
                .setNewsAdded(null)
                .setPointsPerGame(String.valueOf(data.getPointsPerGame()))
                .setSelectedByPercent(String.valueOf(data.getSelectedByPercent()))
                .setMinutes(data.getMinutes())
                .setGoalsScored(data.getGoalsScored())
                .setAssists(data.getAssists())
                .setCleanSheets(data.getCleanSheets())
                .setGoalsConceded(data.getGoalsConceded())
                .setOwnGoals(data.getOwnGoals())
                .setPenaltiesSaved(data.getPenaltiesSaved())
                .setPenaltiesMissed(data.getPenaltiesMissed())
                .setYellowCards(data.getYellowCards())
                .setRedCards(data.getRedCards())
                .setSaves(data.getSaves())
                .setBonus(data.getBonus())
                .setBps(data.getBps())
                .setInfluence(String.valueOf(data.getInfluence()))
                .setCreativity(String.valueOf(data.getCreativity()))
                .setThreat(String.valueOf(data.getThreat()))
                .setIctIndex(String.valueOf(data.getIctIndex()))
                .setTransfersInEvent(data.getTransfersInEvent())
                .setTransfersOutEvent(data.getTransfersOutEvent())
                .setTransfersIn(data.getTransfersIn())
                .setTransfersOut(data.getTransfersOut())
                .setCornersAndIndirectFreekicksOrder(0)
                .setDirectFreekicksOrder(0)
                .setPenaltiesOrder(0);
    }

    @Override
    public void parseNutmegEventData(String season) {
        MybatisPlusConfig.season.set(season);
        Map<Integer, EventEntity> eventMap = Maps.newHashMap();
        Map<String, EventFixtureEntity> eventFixtureMap = Maps.newHashMap();
        Map<String, EventLiveEntity> eventLiveMap = Maps.newHashMap();
        Map<String, PlayerStatEntity> playerStatMap = Maps.newHashMap();
        this.playerService.list().forEach(o -> this.parseSingleNutmegEventData(season, o, eventMap, eventFixtureMap, eventLiveMap, playerStatMap));
        // event
        List<EventEntity> eventList = new ArrayList<>(eventMap.values())
                .stream()
                .sorted(Comparator.comparing(EventEntity::getId))
                .collect(Collectors.toList());
        this.eventService.getBaseMapper().truncate();
        this.eventService.saveBatch(eventList);
        log.info("insert event size:{}", eventList.size());
        // event_fixture
        List<EventFixtureEntity> eventFixtureSortList = new ArrayList<>(eventFixtureMap.values())
                .stream()
                .sorted(Comparator.comparing(EventFixtureEntity::getEvent)
                        .thenComparing(o -> LocalDateTime.parse(o.getKickoffTime().replaceAll(" ", "T"))))
                .collect(Collectors.toList());
        List<EventFixtureEntity> eventFixtureList = Lists.newArrayList();
        for (int i = 1; i < eventFixtureSortList.size() + 1; i++) {
            eventFixtureList.add(eventFixtureSortList.get(i - 1).setId(i));
        }
        this.eventFixtureService.getBaseMapper().truncate();
        this.eventFixtureService.saveBatch(eventFixtureList);
        log.info("insert event fixture size:{}", eventFixtureList);
        // event_live
        List<EventLiveEntity> eventLiveList = new ArrayList<>(eventLiveMap.values())
                .stream()
                .sorted(Comparator.comparing(EventLiveEntity::getEvent)
                        .thenComparing(EventLiveEntity::getElement))
                .collect(Collectors.toList());
        this.eventLiveService.getBaseMapper().truncate();
        this.eventLiveService.saveBatch(eventLiveList);
        log.info("insert event live size:{}", eventLiveList.size());
        // player_stat
        List<PlayerStatEntity> playerStatList = new ArrayList<>(playerStatMap.values())
                .stream()
                .sorted(Comparator.comparing(PlayerStatEntity::getEvent)
                        .thenComparing(PlayerStatEntity::getElement))
                .collect(Collectors.toList());
        this.playerStatService.getBaseMapper().truncate();
        this.playerStatService.saveBatch(playerStatList);
        log.info("insert player stat size:{}", playerStatList.size());
        MybatisPlusConfig.season.remove();
    }

    private void parseSingleNutmegEventData(String season, PlayerEntity playerEntity,
                                            Map<Integer, EventEntity> eventMap, Map<String, EventFixtureEntity> eventFixtureMap,
                                            Map<String, EventLiveEntity> eventLiveMap, Map<String, PlayerStatEntity> playerStatMap) {
        try {
            // get json
            String result = HttpUtils.httpGet(this.getNutmegPlayerUrl(season, playerEntity.getElement())).orElse("");
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            List<EventResponseData> dataList = mapper.readValue(result, new TypeReference<List<EventResponseData>>() {
            });
            if (CollectionUtils.isEmpty(dataList)) {
                return;
            }
            // event
            this.insertIntoEvent(dataList, eventMap);
            // event_fixture
            this.insertIntoEventFixture(playerEntity, dataList, eventFixtureMap);
            // event_live
            this.insertIntoEventLive(playerEntity, dataList, eventLiveMap);
            // player_stat
            this.insertIntoEventPlayerStat(playerEntity, dataList, playerStatMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getNutmegPlayerUrl(String season, int element) {
        String seasonStr = StringUtils.join("20", season.substring(0, 2), "-", season.substring(2, 4));
        return String.format("https://www.fantasynutmeg.com/api/history/%s?player=%s", seasonStr, element);
    }

    private void insertIntoEvent(List<EventResponseData> dataList, Map<Integer, EventEntity> eventMap) {
        dataList
                .stream()
                .filter(o -> !eventMap.containsKey(o.getRound()))
                .map(this::initEventData)
                .forEach(o -> eventMap.put(o.getId(), o));
    }

    private EventEntity initEventData(EventResponseData data) {
        return new EventEntity()
                .setId(data.getRound())
                .setName("Gameweek " + data.getRound())
                .setDeadlineTime(data.getKickoffTime())
                .setAverageEntryScore(0)
                .setFinished(true)
                .setHighestScore(0)
                .setHighestScoringEntry(0)
                .setIsPrevious(false)
                .setIsCurrent(false)
                .setIsNext(false)
                .setMostSelected(0)
                .setMostTransferredIn(0)
                .setMostCaptained(0)
                .setMostViceCaptained(0);
    }

    private void insertIntoEventFixture(PlayerEntity playerEntity, List<EventResponseData> dataList, Map<String, EventFixtureEntity> eventFixtureMap) {
        dataList
                .stream()
                .filter(o -> {
                    int teamId = playerEntity.getTeamId();
                    int againstId = o.getOpponentTeam();
                    int teamH = o.isWasHome() ? teamId : againstId;
                    int teamA = o.isWasHome() ? againstId : teamId;
                    String key = StringUtils.joinWith("-", teamH, teamA);
                    return !eventFixtureMap.containsKey(key) && teamH != teamA;
                })
                .map(o -> this.initEventFixtureData(playerEntity, o))
                .forEach(o -> {
                    String key = StringUtils.joinWith("-", o.getTeamH(), o.getTeamA());
                    eventFixtureMap.put(key, o);
                });
    }

    private EventFixtureEntity initEventFixtureData(PlayerEntity playerEntity, EventResponseData data) {
        int teamId = playerEntity.getTeamId();
        int againstId = data.getOpponentTeam();
        return new EventFixtureEntity()
                .setCode(0)
                .setEvent(data.getRound())
                .setKickoffTime(StringUtils.substringBefore(data.getKickoffTime(), "Z").replaceAll("T", " "))
                .setStarted(true)
                .setFinished(true)
                .setProvisionalStartTime(false)
                .setFinishedProvisional(true)
                .setMinutes(90)
                .setTeamH(data.isWasHome() ? teamId : againstId)
                .setTeamHDifficulty(0)
                .setTeamHScore(data.getTeamHScore())
                .setTeamA(data.isWasHome() ? againstId : teamId)
                .setTeamADifficulty(0)
                .setTeamAScore(data.getTeamAScore());
    }

    private void insertIntoEventLive(PlayerEntity playerEntity, List<EventResponseData> dataList, Map<String, EventLiveEntity> eventLiveMap) {
        dataList
                .stream()
                .filter(o -> {
                    String key = StringUtils.joinWith("-", o.getRound(), o.getElement());
                    return !eventLiveMap.containsKey(key);
                })
                .map(o -> this.initEventLiveData(playerEntity, o))
                .forEach(o -> {
                    String key = StringUtils.joinWith("-", o.getEvent(), o.getElement());
                    eventLiveMap.put(key, o);
                });
    }

    private EventLiveEntity initEventLiveData(PlayerEntity playerEntity, EventResponseData data) {
        return new EventLiveEntity()
                .setElement(data.getElement())
                .setElementType(playerEntity.getElementType())
                .setTeamId(playerEntity.getTeamId())
                .setEvent(data.getRound())
                .setMinutes(data.getMinutes())
                .setGoalsScored(data.getGoalsScored())
                .setAssists(data.getAssists())
                .setCleanSheets(data.getCleanSheets())
                .setGoalsConceded(data.getGoalsConceded())
                .setOwnGoals(data.getOwnGoals())
                .setPenaltiesSaved(data.getPenaltiesSaved())
                .setPenaltiesMissed(data.getPenaltiesMissed())
                .setYellowCards(data.getYellowCards())
                .setRedCards(data.getRedCards())
                .setSaves(data.getSaves())
                .setBonus(data.getBonus())
                .setBps(data.getBps())
                .setTotalPoints(data.getTotalPoints());
    }

    private void insertIntoEventPlayerStat(PlayerEntity playerEntity, List<EventResponseData> dataList, Map<String, PlayerStatEntity> playerStatMap) {
        dataList
                .stream()
                .filter(o -> {
                    String key = StringUtils.joinWith("-", o.getRound(), o.getElement());
                    return !playerStatMap.containsKey(key);
                })
                .map(o -> this.initEventPlayerStatData(playerEntity, o))
                .forEach(o -> {
                    String key = StringUtils.joinWith("-", o.getEvent(), o.getElement());
                    playerStatMap.put(key, o);
                });
    }

    private PlayerStatEntity initEventPlayerStatData(PlayerEntity playerEntity, EventResponseData data) {
        return new PlayerStatEntity()
                .setEvent(data.getRound())
                .setElement(data.getElement())
                .setCode(playerEntity.getCode())
                .setMatchPlayed(0)
                .setChanceOfPlayingNextRound(0)
                .setChanceOfPlayingThisRound(0)
                .setDreamteamCount(0)
                .setEventPoints(data.getTotalPoints())
                .setForm("")
                .setInDreamteam(false)
                .setNews("")
                .setNewsAdded(null)
                .setPointsPerGame("")
                .setSelectedByPercent("")
                .setMinutes(data.getMinutes())
                .setGoalsScored(data.getGoalsScored())
                .setAssists(data.getAssists())
                .setCleanSheets(data.getCleanSheets())
                .setGoalsConceded(data.getGoalsConceded())
                .setOwnGoals(data.getOwnGoals())
                .setPenaltiesSaved(data.getPenaltiesSaved())
                .setPenaltiesMissed(data.getPenaltiesMissed())
                .setYellowCards(data.getYellowCards())
                .setRedCards(data.getRedCards())
                .setSaves(data.getSaves())
                .setBonus(data.getBonus())
                .setBps(data.getBps())
                .setInfluence(String.valueOf(data.getInfluence()))
                .setCreativity(String.valueOf(data.getCreativity()))
                .setThreat(String.valueOf(data.getThreat()))
                .setIctIndex(String.valueOf(data.getIctIndex()))
                .setTransfersInEvent(0)
                .setTransfersOutEvent(0)
                .setTransfersIn(data.getTransfersIn())
                .setTransfersOut(data.getTransfersOut())
                .setCornersAndIndirectFreekicksOrder(0)
                .setDirectFreekicksOrder(0)
                .setPenaltiesOrder(0);
    }

}

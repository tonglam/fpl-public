package com.tong.fpl.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
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
        this.playerService.saveBatch(insertList);
        log.info("insert player size:{}", insertList.size());
        this.playerService.updateBatchById(updateList);
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
                .setTeamId(data.getTeam())
                .setSquadNumber(data.getSquadNumber());
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
        this.playerService.list().forEach(o -> this.parseSingleNutmegEventData(season, o));
        MybatisPlusConfig.season.remove();
    }

    private void parseSingleNutmegEventData(String season, PlayerEntity playerEntity) {
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
            this.insertIntoEvent(dataList);
            // event_fixture
            this.insertIntoEventFixture(playerEntity, dataList);
            // event_live
            this.insertIntoEventLive(playerEntity, dataList);
            // player_stat
            this.insertIntoEventPlayerStat(playerEntity, dataList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getNutmegPlayerUrl(String season, int element) {
        String seasonStr = StringUtils.join("20", season.substring(0, 2), "-", season.substring(2, 4));
        return String.format("https://www.fantasynutmeg.com/api/history/%s?player=%s", seasonStr, element);
    }

    private void insertIntoEvent(List<EventResponseData> dataList) {
        List<EventEntity> insertList = Lists.newArrayList();
        List<EventEntity> updateList = Lists.newArrayList();
        Map<Integer, EventEntity> map = this.eventService.list()
                .stream()
                .collect(Collectors.toMap(EventEntity::getId, o -> o));
        dataList
                .stream()
                .map(this::initEventData)
                .forEach(o -> {
                    if (!map.containsKey(o.getId())) {
                        insertList.add(o);
                    } else {
                        updateList.add(o);
                    }
                });
//        this.eventService.saveBatch(insertList);
        log.info("insert event size:{}", insertList.size());
//        this.eventService.updateBatchById(updateList);
        log.info("update event size:{}", updateList.size());
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

    private void insertIntoEventFixture(PlayerEntity playerEntity, List<EventResponseData> dataList) {
        List<EventFixtureEntity> insertList = Lists.newArrayList();
        List<EventFixtureEntity> updateList = Lists.newArrayList();
        Map<Integer, EventFixtureEntity> map = this.eventFixtureService.list()
                .stream()
                .collect(Collectors.toMap(EventFixtureEntity::getCode, o -> o));
        dataList
                .stream()
                .map(o -> this.initEventFixtureData(playerEntity, o))
                .forEach(o -> {
                    int code = o.getCode();
                    if (!map.containsKey(code)) {
                        insertList.add(o);
                    } else {
                        o.setId(map.get(code).getId());
                        updateList.add(o);
                    }
                });
//        this.eventFixtureService.saveBatch(insertList);
        log.info("insert event fixture size:{}", insertList.size());
//        this.eventFixtureService.updateBatchById(updateList);
        log.info("update event fixture size:{}", updateList.size());
    }

    private EventFixtureEntity initEventFixtureData(PlayerEntity playerEntity, EventResponseData data) {
        int teamId = playerEntity.getTeamId();
        int againstId = data.getOpponentTeam();
        return new EventFixtureEntity()
                .setCode(0)
                .setEvent(data.getRound())
                .setKickoffTime(data.getKickoffTime())
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

    private void insertIntoEventLive(PlayerEntity playerEntity, List<EventResponseData> dataList) {
        List<EventLiveEntity> insertList = Lists.newArrayList();
        List<EventLiveEntity> updateList = Lists.newArrayList();
        Map<String, EventLiveEntity> map = this.eventLiveService.list()
                .stream()
                .collect(Collectors.toMap(k -> StringUtils.joinWith("-", k.getElement(), k.getEvent()), v -> v));
        dataList
                .stream()
                .map(o -> this.initEventLiveData(playerEntity, o))
                .forEach(o -> {
                    String key = StringUtils.joinWith("-", o.getElement(), o.getEvent());
                    if (!map.containsKey(key)) {
                        insertList.add(o);
                    } else {
                        o.setId(map.get(key).getId());
                        updateList.add(o);
                    }
                });
//        this.eventLiveService.saveBatch(insertList);
        log.info("insert event live size:{}", insertList.size());
//        this.eventLiveService.updateBatchById(updateList);
        log.info("update event live size:{}", updateList.size());
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

    private void insertIntoEventPlayerStat(PlayerEntity playerEntity, List<EventResponseData> dataList) {
        List<PlayerStatEntity> insertList = Lists.newArrayList();
        List<PlayerStatEntity> updateList = Lists.newArrayList();
        Map<String, PlayerStatEntity> map = this.playerStatService.list()
                .stream()
                .collect(Collectors.toMap(k -> StringUtils.joinWith("-", k.getElement(), k.getEvent()), v -> v));
        dataList
                .stream()
                .map(o -> this.initEventPlayerStatData(playerEntity, o))
                .forEach(o -> {
                    String key = StringUtils.joinWith("-", o.getElement(), o.getEvent());
                    if (!map.containsKey(key)) {
                        insertList.add(o);
                    } else {
                        o.setId(map.get(key).getId());
                        updateList.add(o);
                    }
                });
//        this.playerStatService.saveBatch(insertList);
        log.info("insert player stat size:{}", insertList.size());
//        this.playerStatService.updateBatchById(updateList);
        log.info("update player stat size:{}", updateList.size());
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

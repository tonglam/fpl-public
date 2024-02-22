package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.config.collector.LiveFixtureCollector;
import com.tong.fpl.config.collector.PlayerValueCollector;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.MatchPlayStatus;
import com.tong.fpl.constant.enums.ValueChangeType;
import com.tong.fpl.domain.data.bootstrapStaic.Player;
import com.tong.fpl.domain.data.eventLive.ElementStat;
import com.tong.fpl.domain.data.response.EventFixturesRes;
import com.tong.fpl.domain.data.response.EventLiveRes;
import com.tong.fpl.domain.data.response.StaticRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.event.EventChipData;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.event.EventTopElementData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerHistoryData;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/8/23
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RedisCacheServiceImpl implements IRedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final EventFixtureService eventFixtureService;
    private final PlayerService playerService;
    private final PlayerStatService playerStatService;
    private final PlayerValueService playerValueService;
    private final EventLiveService eventLiveService;

    /**
     * @implNote insert
     */
    @Override
    public void insertSingleEventFixture(int event, List<EventFixturesRes> eventFixturesResList) {
        if (CollectionUtils.isEmpty(eventFixturesResList)) {
            return;
        }
        this.eventFixtureService.remove(new QueryWrapper<EventFixtureEntity>().lambda().eq(EventFixtureEntity::getEvent, event));
        List<EventFixtureEntity> eventFixtureList = Lists.newArrayList();
        eventFixturesResList.forEach(o -> {
            EventFixtureEntity eventFixtureEntity = new EventFixtureEntity();
            BeanUtil.copyProperties(o, eventFixtureEntity, CopyOptions.create().ignoreNullValue());
            eventFixtureEntity.setKickoffTime(CommonUtils.getLocalZoneDateTime(o.getKickoffTime()));
            eventFixtureList.add(eventFixtureEntity);
        });
        this.eventFixtureService.saveBatch(eventFixtureList);
        log.info("insert event:{}, event_fixture size:{}!", event, eventFixtureList.size());
        // set cache by event
        Map<String, Set<Object>> cacheMap = Maps.newHashMap();
        this.setEventFixtureCacheBySingleEvent(cacheMap, CommonUtils.getCurrentSeason(), event, eventFixtureList);
        RedisUtils.pipelineSetCache(cacheMap, -1, null);
        // set cache by team
        Map<String, String> teamNameMap = this.getTeamNameMap(CommonUtils.getCurrentSeason());
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap(CommonUtils.getCurrentSeason());
        IntStream.rangeClosed(1, 20).forEach(teamId ->
                this.insertEventFixtureCacheBySingleTeamAndEvent(CommonUtils.getCurrentSeason(), eventFixtureList, teamId, teamNameMap, teamShortNameMap, event));
    }

    @Override
    public void insertSingleEventFixtureCache(int event, List<EventFixturesRes> eventFixturesResList) {
        if (CollectionUtils.isEmpty(eventFixturesResList)) {
            return;
        }
        List<EventFixtureEntity> eventFixtureList = Lists.newArrayList();
        eventFixturesResList.forEach(o -> {
            EventFixtureEntity eventFixtureEntity = new EventFixtureEntity();
            BeanUtil.copyProperties(o, eventFixtureEntity, CopyOptions.create().ignoreNullValue());
            eventFixtureEntity.setKickoffTime(CommonUtils.getLocalZoneDateTime(o.getKickoffTime()));
            eventFixtureList.add(eventFixtureEntity);
        });
        // set cache by event
        Map<String, Set<Object>> cacheMap = Maps.newHashMap();
        this.setEventFixtureCacheBySingleEvent(cacheMap, CommonUtils.getCurrentSeason(), event, eventFixtureList);
        RedisUtils.pipelineSetCache(cacheMap, -1, null);
        // set cache by team
        Map<String, String> teamNameMap = this.getTeamNameMap(CommonUtils.getCurrentSeason());
        Map<String, String> teamShortNameMap = this.getTeamShortNameMap(CommonUtils.getCurrentSeason());
        IntStream.rangeClosed(1, 20).forEach(teamId ->
                this.insertEventFixtureCacheBySingleTeamAndEvent(CommonUtils.getCurrentSeason(), eventFixtureList, teamId, teamNameMap, teamShortNameMap, event));
    }

    private void setEventFixtureCacheBySingleEvent(Map<String, Set<Object>> cacheMap, String season, int event, Collection<EventFixtureEntity> eventFixtureList) {
        String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "event", event);
        Set<Object> valueSet = Sets.newHashSet();
        RedisUtils.removeCacheByKey(key);
        valueSet.addAll(eventFixtureList);
        cacheMap.put(key, valueSet);
    }

    @Override
    public void insertLiveFixtureCache() {
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        String season = CommonUtils.getCurrentSeason();
        int event = this.getCurrentEvent();
        Table<Integer, MatchPlayStatus, List<LiveFixtureData>> table = this.getEventFixtureByEvent(season, event)
                .stream()
                .collect(new LiveFixtureCollector(this.getTeamNameMap(season), this.getTeamShortNameMap(season)));
        Map<String, Object> valueMap = Maps.newHashMap();
        table.rowKeySet().forEach(teamId -> {
            Map<MatchPlayStatus, List<LiveFixtureData>> map = Maps.newHashMap();
            // playing
            List<LiveFixtureData> playingList = Lists.newArrayList();
            if (table.contains(teamId, MatchPlayStatus.Playing)) {
                playingList = table.get(teamId, MatchPlayStatus.Playing);
            }
            map.put(MatchPlayStatus.Playing, playingList);
            // not start
            List<LiveFixtureData> notStartList = Lists.newArrayList();
            if (table.contains(teamId, MatchPlayStatus.Not_Start)) {
                notStartList = table.get(teamId, MatchPlayStatus.Not_Start);
            }
            map.put(MatchPlayStatus.Not_Start, notStartList);
            // finished
            List<LiveFixtureData> finishedList = Lists.newArrayList();
            if (table.contains(teamId, MatchPlayStatus.Finished)) {
                finishedList = table.get(teamId, MatchPlayStatus.Finished);
            }
            map.put(MatchPlayStatus.Finished, finishedList);
            valueMap.put(String.valueOf(teamId), map);
        });
        // set cache
        String key = StringUtils.joinWith("::", LiveFixtureData.class.getSimpleName());
        RedisUtils.removeCacheByKey(key);
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private void insertEventFixtureCacheBySingleTeamAndEvent(String season, Collection<EventFixtureEntity> fixtureList, int teamId, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap, int event) {
        String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "teamId", teamId);
        String HashKey = String.valueOf(event);
        this.redisTemplate.opsForHash().delete(key, HashKey);
        Map<String, Object> valueMap = this.setEventFixtureValueBySingleTeam(fixtureList, teamId, teamNameMap, teamShortNameMap);
        this.redisTemplate.opsForHash().put(key, HashKey, valueMap.get(HashKey));
    }

    private Map<String, Object> setEventFixtureValueBySingleTeam(Collection<EventFixtureEntity> fixtureList, int teamId, Map<String, String> teamNameMap, Map<String, String> teamShortNameMap) {
        Multimap<String, Object> eventFixtureMap = HashMultimap.create();
        // home game
        fixtureList.stream()
                .filter(o -> o.getTeamH() == teamId)
                .forEach(o -> eventFixtureMap.put(String.valueOf(o.getEvent()), new PlayerFixtureData()
                        .setEvent(o.getEvent())
                        .setTeamId(teamId)
                        .setTeamName(teamNameMap.getOrDefault(String.valueOf(teamId), ""))
                        .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(teamId), ""))
                        .setAgainstTeamId(o.getTeamA())
                        .setAgainstTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamA()), ""))
                        .setAgainstTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamA()), ""))
                        .setDifficulty(o.getTeamHDifficulty())
                        .setKickoffTime(o.getKickoffTime())
                        .setStarted(o.getStarted())
                        .setFinished(o.getFinished())
                        .setWasHome(true)
                        .setTeamScore(o.getTeamHScore())
                        .setAgainstTeamScore(o.getTeamAScore())
                        .setScore(o.getTeamHScore() + "-" + o.getTeamAScore())
                        .setResult(this.getTeamEventFixtureResult(o.getFinished(), o.getTeamHScore(), o.getTeamAScore()))
                ));
        // away game
        fixtureList.stream()
                .filter(o -> o.getTeamA() == teamId)
                .forEach(o -> eventFixtureMap.put(String.valueOf(o.getEvent()), new PlayerFixtureData()
                        .setEvent(o.getEvent())
                        .setTeamId(teamId)
                        .setTeamName(teamNameMap.getOrDefault(String.valueOf(teamId), ""))
                        .setTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(teamId), ""))
                        .setAgainstTeamId(o.getTeamH())
                        .setAgainstTeamName(teamNameMap.getOrDefault(String.valueOf(o.getTeamH()), ""))
                        .setAgainstTeamShortName(teamShortNameMap.getOrDefault(String.valueOf(o.getTeamH()), ""))
                        .setDifficulty(o.getTeamADifficulty())
                        .setKickoffTime(o.getKickoffTime())
                        .setStarted(o.getStarted())
                        .setFinished(o.getFinished())
                        .setWasHome(false)
                        .setTeamScore(o.getTeamAScore())
                        .setAgainstTeamScore(o.getTeamHScore())
                        .setScore(o.getTeamAScore() + "-" + o.getTeamHScore())
                        .setResult(this.getTeamEventFixtureResult(o.getFinished(), o.getTeamAScore(), o.getTeamHScore()))
                ));
        Map<String, Object> valueMap = Maps.newHashMap();
        eventFixtureMap.keySet().forEach(event -> {
            List<PlayerFixtureData> list = Lists.newArrayList();
            eventFixtureMap.get(event).forEach(o -> list.add((PlayerFixtureData) o));
            valueMap.put(event, list);
        });
        return valueMap;
    }

    private String getTeamEventFixtureResult(boolean finished, int teamScore, int againstTeamScore) {
        if (!finished) {
            return "";
        }
        if (teamScore > againstTeamScore) {
            return "W";
        } else if (teamScore < againstTeamScore) {
            return "L";
        }
        return "D";
    }

    @Override
    public void insertPlayer(StaticRes staticRes) {
        if (staticRes == null) {
            return;
        }
        Map<String, PlayerEntity> playerMap = this.getPlayerMap(CommonUtils.getCurrentSeason());
        int tableSize = (int) this.playerService.count();
        if (staticRes.getElements().size() == playerMap.size() && staticRes.getElements().size() == tableSize) {
            log.info("no new players");
            return;
        }
        List<PlayerEntity> insertList = Lists.newArrayList();
        staticRes.getElements().forEach(o -> {
            int element = o.getId();
            if (playerMap.containsKey(String.valueOf(element))) {
                return;
            }
            insertList.add(
                    new PlayerEntity()
                            .setElement(element)
                            .setCode(o.getCode())
                            .setPrice(o.getNowCost())
                            .setStartPrice(o.getNowCost() - o.getCostChangeStart())
                            .setElementType(o.getElementType())
                            .setFirstName(o.getFirstName())
                            .setSecondName(o.getSecondName())
                            .setWebName(o.getWebName())
                            .setTeamId(o.getTeam())
            );
        });
        this.playerService.saveBatch(insertList);
        log.info("insert player size:{}!", insertList.size());
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
        Map<String, Object> valueMap = Maps.newHashMap();
        RedisUtils.removeCacheByKey(key);
        this.playerService.list().forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertEventLive(int event, EventLiveRes eventLiveRes) {
        if (eventLiveRes == null) {
            return;
        }
        List<EventLiveEntity> eventLiveList = Lists.newArrayList();
        List<EventLiveEntity> insertList = Lists.newArrayList();
        List<EventLiveEntity> updateList = Lists.newArrayList();
        // prepare
        Map<Integer, EventLiveEntity> eventLiveMap = this.eventLiveService.list(new QueryWrapper<EventLiveEntity>().lambda()
                        .eq(EventLiveEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(EventLiveEntity::getElement, o -> o));
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        eventLiveRes.getElements().forEach(o -> {
            if (o.getStats() == null) {
                return;
            }
            int element = o.getId();
            ElementStat elementStat = o.getStats();
            EventLiveEntity eventLive = new EventLiveEntity();
            BeanUtil.copyProperties(elementStat, eventLive, CopyOptions.create().ignoreNullValue());
            eventLive
                    .setElement(element)
                    .setElementType(playerMap.containsKey(element) ?
                            playerMap.get(element).getElementType() : 0)
                    .setTeamId(playerMap.containsKey(element) ? playerMap.get(element).getTeamId() : 0)
                    .setEvent(event);
            eventLiveList.add(eventLive);
        });
        // insert or update
        eventLiveList.forEach(o -> {
            int element = o.getElement();
            if (!eventLiveMap.containsKey(element)) {
                insertList.add(o);
            } else {
                o.setId(eventLiveMap.get(element).getId());
                updateList.add(o);
            }
        });
        this.eventLiveService.saveBatch(insertList);
        log.info("insert event_live size is " + insertList.size() + "!");
        this.eventLiveService.updateBatchById(updateList);
        log.info("update event_live size is " + updateList.size() + "!");
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventLiveEntity.class.getSimpleName(), event);
        RedisUtils.removeCacheByKey(key);
        eventLiveList.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertPlayerStat(StaticRes staticRes) {
        if (staticRes == null) {
            return;
        }
        List<PlayerStatEntity> insertList = Lists.newArrayList();
        List<PlayerStatEntity> updateList = Lists.newArrayList();
        // prepare
        int event = this.getCurrentEvent();
        Map<Integer, Integer> insertTeamMap = this.getInsertTeamList();
        Map<Integer, Integer> playerStatIdMap = this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
                        .eq(PlayerStatEntity::getEvent, event))
                .stream()
                .collect(Collectors.toMap(PlayerStatEntity::getElement, PlayerStatEntity::getId));
        // get from fpl server
        staticRes.getElements().forEach(o -> {
            if (!insertTeamMap.containsKey(o.getTeam())) {
                return;
            }
            // insert or update table
            PlayerStatEntity playerStatEntity = this.initPlayStat(event, o, insertTeamMap);
            int element = playerStatEntity.getElement();
            if (!playerStatIdMap.containsKey(element)) {
                insertList.add(playerStatEntity);
            } else {
                playerStatEntity.setId(playerStatIdMap.get(element));
                updateList.add(playerStatEntity);
            }
        });
        // insert
        this.playerStatService.saveBatch(insertList);
        log.info("insert player_stat size:{}", insertList.size());
        // update
        this.playerStatService.updateBatchById(updateList);
        log.info("update player_stat size:{}", updateList.size());
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerStatEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
        Map<String, Object> valueMap = Maps.newHashMap();
        RedisUtils.removeCacheByKey(key);
        this.playerStatService.list(new QueryWrapper<PlayerStatEntity>().lambda()
                        .eq(PlayerStatEntity::getEvent, event))
                .forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private Map<Integer, Integer> getInsertTeamList() {
        Map<Integer, Integer> insertTeamMap = Maps.newHashMap();
        IntStream.rangeClosed(1, 20).forEach(teamId -> {
            // match_played
            int matchPlayed = (int) this.eventFixtureService.count(new QueryWrapper<EventFixtureEntity>().lambda()
                    .eq(EventFixtureEntity::getFinished, 1)
                    .and(a -> a.eq(EventFixtureEntity::getTeamH, teamId)
                            .or(i -> i.eq(EventFixtureEntity::getTeamA, teamId)))
            );
            insertTeamMap.put(teamId, matchPlayed);
        });
        return insertTeamMap;
    }

    private PlayerStatEntity initPlayStat(int event, Player player, Map<Integer, Integer> insertTeamMap) {
        return new PlayerStatEntity()
                .setEvent(event)
                .setElement(player.getId())
                .setCode(player.getCode())
                .setMatchPlayed(insertTeamMap.get(player.getTeam()))
                .setChanceOfPlayingNextRound(player.getChanceOfPlayingNextRound())
                .setChanceOfPlayingThisRound(player.getChanceOfPlayingThisRound())
                .setDreamteamCount(player.getDreamteamCount())
                .setEventPoints(player.getEventPoints())
                .setForm(player.getForm())
                .setInDreamteam(player.isInDreamteam())
                .setNews(player.getNews())
                .setNewsAdded(player.getNewsAdded())
                .setPointsPerGame(player.getPointsPerGame())
                .setSelectedByPercent(player.getSelectedByPercent())
                .setMinutes(player.getMinutes())
                .setGoalsScored(player.getGoalsScored())
                .setAssists(player.getAssists())
                .setCleanSheets(player.getCleanSheets())
                .setGoalsConceded(player.getGoalsConceded())
                .setOwnGoals(player.getOwnGoals())
                .setPenaltiesSaved(player.getPenaltiesSaved())
                .setPenaltiesMissed(player.getPenaltiesMissed())
                .setYellowCards(player.getYellowCards())
                .setRedCards(player.getRedCards())
                .setSaves(player.getSaves())
                .setBonus(player.getBonus())
                .setBps(player.getBps())
                .setInfluence(player.getInfluence())
                .setCreativity(player.getCreativity())
                .setThreat(player.getThreat())
                .setIctIndex(player.getIctIndex())
                .setTransfersInEvent(player.getTransfersInEvent())
                .setTransfersOutEvent(player.getTransfersOutEvent())
                .setTransfersIn(player.getTransfersIn())
                .setTransfersOut(player.getTransfersOut())
                .setCornersAndIndirectFreekicksOrder(player.getCornersAndIndirectFreekicksOrder())
                .setDirectFreekicksOrder(player.getDirectFreekicksOrder())
                .setPenaltiesOrder(player.getPenaltiesOrder());
    }

    @Override
    public void insertPlayerValue(StaticRes staticRes) {
        if (staticRes == null) {
            return;
        }
        String changeDate = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
        // prepare
        Map<Integer, PlayerValueEntity> lastValueMap = this.playerValueService.list()
                .stream()
                .collect(new PlayerValueCollector());
        Map<Integer, PlayerValueEntity> valueMap = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>().lambda()
                        .eq(PlayerValueEntity::getChangeDate, changeDate))
                .stream()
                .collect(Collectors.toMap(PlayerValueEntity::getElement, o -> o));
        int event = this.getCurrentEvent();
        // calc
        List<PlayerValueEntity> playerValueList = Lists.newArrayList();
        staticRes.getElements()
                .stream()
                .filter(o -> !lastValueMap.containsKey(o.getId()) || o.getNowCost() != lastValueMap.get(o.getId()).getValue())
                .forEach(bootstrapPlayer -> {
                    int element = bootstrapPlayer.getId();
                    if (valueMap.containsKey(element)) {
                        return;
                    }
                    PlayerValueEntity lastEntity = lastValueMap.getOrDefault(element, null);
                    int lastValue = lastEntity != null ? lastEntity.getValue() : 0;
                    playerValueList.add(
                            new PlayerValueEntity()
                                    .setElement(element)
                                    .setElementType(bootstrapPlayer.getElementType())
                                    .setEvent(event)
                                    .setValue(bootstrapPlayer.getNowCost())
                                    .setChangeDate(changeDate)
                                    .setChangeType(this.getChangeType(bootstrapPlayer.getNowCost(), lastValue))
                                    .setLastValue(lastValue)
                    );
                });
        if (CollectionUtils.isEmpty(playerValueList)) {
            return;
        }
        // insert
        this.playerValueService.saveBatch(playerValueList);
        log.info("insert player value size:{}", playerValueList.size());
        // set cache
        Map<String, Set<Object>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerValueEntity.class.getSimpleName(), changeDate);
        Set<Object> valueSet = Sets.newHashSet();
        valueSet.addAll(playerValueList);
        cacheMap.put(key, valueSet);
        RedisUtils.pipelineSetCache(cacheMap, 1, TimeUnit.DAYS);
        // update price in table player
        this.updatePriceOfPlayer(playerValueList);
    }

    private String getChangeType(int nowCost, int lastCost) {
        if (lastCost == 0) {
            return ValueChangeType.Start.name();
        }
        return nowCost > lastCost ? ValueChangeType.Rise.name() : ValueChangeType.Faller.name();
    }

    private void updatePriceOfPlayer(List<PlayerValueEntity> playerValueList) {
        List<PlayerEntity> updatePlayerList = Lists.newArrayList();
        playerValueList.forEach(o -> {
            // update table
            PlayerEntity playerEntity = this.playerService.getById(o.getElement());
            playerEntity.setPrice(o.getValue());
            if (playerEntity.getStartPrice() == 0) {
                // start price
                PlayerValueEntity playerValueEntity = this.playerValueService.getOne(new QueryWrapper<PlayerValueEntity>().lambda()
                        .eq(PlayerValueEntity::getElement, o.getElement())
                        .eq(PlayerValueEntity::getChangeType, ValueChangeType.Start.name()));
                if (playerValueEntity != null) {
                    playerEntity.setStartPrice(playerValueEntity.getValue());
                }
            }
            updatePlayerList.add(playerEntity);
            // set cache
            String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
            this.redisTemplate.opsForHash().put(key, String.valueOf(o.getElement()), playerEntity);
        });
        this.playerService.updateBatchById(updatePlayerList);
        log.info("insert player value size:{}", playerValueList.size());
    }

    @Override
    public void insertEventLiveCache(int event, EventLiveRes eventLiveRes) {
        if (eventLiveRes == null) {
            return;
        }
        Map<Integer, PlayerEntity> playerMap = this.playerService.list()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getElement, o -> o));
        List<EventLiveEntity> eventLiveList = Lists.newArrayList();
        eventLiveRes.getElements().forEach(o -> {
            int element = o.getId();
            ElementStat elementStat = o.getStats();
            EventLiveEntity eventLive = new EventLiveEntity();
            BeanUtil.copyProperties(elementStat, eventLive, CopyOptions.create().ignoreNullValue());
            eventLive.setElement(element)
                    .setElementType(playerMap.containsKey(element) ? playerMap.get(element).getElementType() : 0)
                    .setTeamId(playerMap.containsKey(element) ? playerMap.get(element).getTeamId() : 0)
                    .setEvent(event);
            eventLiveList.add(eventLive);
        });
        log.info("event_live_cache size is " + eventLiveList.size() + "!");
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventLiveEntity.class.getSimpleName(), event);
        RedisUtils.removeCacheByKey(key);
        eventLiveList.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertLiveBonusCache() {
        // get playing fixtures
        Map<Integer, Integer> livePlayingMap = Maps.newHashMap(); // key:team -> value:against_team
        this.getEventLiveFixtureMap().forEach((teamIdStr, map) -> {
            int teamId = Integer.parseInt(teamIdStr);
            map.keySet().forEach(status -> {
                List<LiveFixtureData> liveFixtureList = Lists.newArrayList();
                liveFixtureList.addAll(map.get(MatchPlayStatus.Playing.name()));
                liveFixtureList.addAll(map.get(MatchPlayStatus.Finished.name()));
                if (CollectionUtils.isEmpty(liveFixtureList)) {
                    return;
                }
                if (livePlayingMap.containsKey(teamId)) {
                    return;
                }
                livePlayingMap.put(teamId, liveFixtureList.get(0).getAgainstId());
                livePlayingMap.put(liveFixtureList.get(0).getAgainstId(), teamId);
            });
        });
        // get event_live
        List<Integer> bonusInTeamList = Lists.newArrayList();
        Map<Integer, List<EventLiveEntity>> teamEventLiveMap = Maps.newHashMap();
        this.getEventLiveByEvent(this.getCurrentEvent()).values()
                .forEach(o -> {
                    if (o.getMinutes() <= 0) {
                        return;
                    }
                    int teamId = o.getTeamId();
                    if (!livePlayingMap.containsKey(teamId)) {
                        return;
                    }
                    int againstId = livePlayingMap.get(teamId);
                    // check bonus in
                    if (o.getBonus() > 0) {
                        if (teamEventLiveMap.containsKey(teamId) && !bonusInTeamList.contains(teamId)) {
                            bonusInTeamList.add(teamId);
                        }
                        if (teamEventLiveMap.containsKey(againstId) && !bonusInTeamList.contains(againstId)) {
                            bonusInTeamList.add(againstId);
                        }
                        return;
                    }
                    // home team
                    List<EventLiveEntity> homeList = Lists.newArrayList();
                    if (teamEventLiveMap.containsKey(teamId)) {
                        homeList = teamEventLiveMap.get(teamId);
                    }
                    homeList.add(o);
                    teamEventLiveMap.put(teamId, homeList);
                    // away team
                    List<EventLiveEntity> awayList = Lists.newArrayList();
                    if (teamEventLiveMap.containsKey(againstId)) {
                        awayList = teamEventLiveMap.get(againstId);
                    }
                    awayList.add(o);
                    teamEventLiveMap.put(againstId, awayList);
                });
        //  set cache
        String key = StringUtils.joinWith("::", "LiveBonusData");
        RedisUtils.removeCacheByKey(key);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        teamEventLiveMap.keySet().forEach(teamId -> {
            if (bonusInTeamList.contains(teamId)) {
                return;
            }
            // sort teamEventLiveMap by bps
            List<EventLiveEntity> sortList = teamEventLiveMap.get(teamId)
                    .stream()
                    .sorted(Comparator.comparing(EventLiveEntity::getBps).reversed())
                    .collect(Collectors.toList());
            // set bonus points
            Map<Integer, Integer> bonusMap = this.setBonusPoints(teamId, sortList);
            if (!CollectionUtils.isEmpty(bonusMap)) {
                valueMap.put(String.valueOf(teamId), bonusMap);
            }
        });
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private Map<Integer, Integer> setBonusPoints(int teamId, List<EventLiveEntity> sortList) {
        int count = 0;
        Map<Integer, Integer> bonusMap = Maps.newConcurrentMap();
        // 最高分
        EventLiveEntity first = sortList.get(0);
        int highestBps = first.getBps();
        this.setBonusMap(teamId, first, 3, bonusMap);
        count += 1;
        // bps同分
        List<EventLiveEntity> firstList = sortList
                .stream()
                .filter(o -> !o.getElement().equals(first.getElement()))
                .filter(o -> o.getBps() == highestBps)
                .toList();
        for (EventLiveEntity eventLiveEntity :
                firstList) {
            count += 1;
            this.setBonusMap(teamId, eventLiveEntity, 3, bonusMap);
        }
        if (count >= 3) {
            return bonusMap;
        }
        // 次高分
        if (count < 2) {
            EventLiveEntity second = sortList.get(count);
            int runnerUpBps = second.getBps();
            this.setBonusMap(teamId, second, 2, bonusMap);
            count += 1;
            // bps同分
            List<EventLiveEntity> secondList = sortList
                    .stream()
                    .filter(o -> !o.getElement().equals(second.getElement()))
                    .filter(o -> o.getBps() == runnerUpBps)
                    .toList();
            for (EventLiveEntity eventLiveEntity :
                    secondList) {
                count += 1;
                this.setBonusMap(teamId, eventLiveEntity, 2, bonusMap);
            }
            if (count >= 3) {
                return bonusMap;
            }
        }
        // 第三高分
        EventLiveEntity third = sortList.get(count);
        int secondRunnerUpBps = third.getBps();
        this.setBonusMap(teamId, third, 1, bonusMap);
        count += 1;
        // bps同分
        List<EventLiveEntity> thirdList = sortList
                .stream()
                .filter(o -> !o.getElement().equals(third.getElement()))
                .filter(o -> o.getBps() == secondRunnerUpBps)
                .toList();
        for (EventLiveEntity eventLiveEntity :
                thirdList) {
            count += 1;
            this.setBonusMap(teamId, eventLiveEntity, 1, bonusMap);
        }
        return bonusMap;
    }

    private void setBonusMap(int teamId, EventLiveEntity eventLiveEntity, int bonus, Map<Integer, Integer> bonusMap) {
        if (teamId != eventLiveEntity.getTeamId()) {
            return;
        }
        bonusMap.put(eventLiveEntity.getElement(), bonus);
    }

    @Override
    public void insertEventOverallResult(StaticRes staticRes) {
        if (staticRes == null || CollectionUtils.isEmpty(staticRes.getEvents())) {
            return;
        }
        String key = StringUtils.joinWith("::", "EventOverallResult", CommonUtils.getCurrentSeason());
        RedisUtils.removeCacheByKey(key);
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        staticRes.getEvents().forEach(o -> {
            int event = o.getId();
            EventOverallResultData data = new EventOverallResultData()
                    .setEvent(event)
                    .setAverageEntryScore(o.getAverageEntryScore())
                    .setFinished(o.isFinished())
                    .setHighestScoringEntry(o.getHighestScoringEntry())
                    .setHighestScore(o.getHighestScore())
                    .setChipPlays(
                            o.getChipPlays()
                                    .stream()
                                    .map(i ->
                                            new EventChipData()
                                                    .setChipName(i.getChipName())
                                                    .setNumberPlayed(i.getNumPlayed())
                                    )
                                    .collect(Collectors.toList())
                    )
                    .setMostSelected(o.getMostSelected())
                    .setMostTransferredIn(o.getMostTransferredIn())
                    .setTopElementInfo(
                            o.getTopElementInfo() == null ? null :
                                    new EventTopElementData()
                                            .setElement(o.getTopElementInfo().getId())
                                            .setPoints(o.getTopElementInfo().getPoints())
                    )
                    .setTransfersMade(o.getTransfersMade())
                    .setMostCaptained(o.getMostCaptained())
                    .setMostViceCaptained(o.getMostViceCaptained());
            valueMap.put(String.valueOf(event), data);
        });
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    /**
     * @implNote get
     */
    @Override
    public int getCurrentEvent() {
        String season = CommonUtils.getCurrentSeason();
        int event = 1;
        for (int i = 1; i < 39; i++) {
            String deadline = this.getDeadlineByEvent(season, i);
            if (LocalDateTime.now().isAfter(LocalDateTime.parse(deadline, DateTimeFormatter.ofPattern(Constant.DATETIME)))) {
                event = i;
            } else {
                break;
            }
        }
        return event;
    }

    @Override
    public Map<String, String> getTeamNameMap(String season) {
        Map<String, String> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "name");
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (String) v));
        return map;
    }

    @Override
    public Map<String, String> getTeamShortNameMap(String season) {
        Map<String, String> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "shortName");
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (String) v));
        return map;
    }

    @Override
    public String getUtcDeadlineByEvent(String season, int event) {
        String key = StringUtils.joinWith("::", EventEntity.class.getSimpleName(), season);
        return (String) this.redisTemplate.opsForHash().get(key, String.valueOf(event));
    }

    @Override
    public String getDeadlineByEvent(String season, int event) {
        String key = StringUtils.joinWith("::", EventEntity.class.getSimpleName(), season);
        return CommonUtils.getLocalZoneDateTime((String) this.redisTemplate.opsForHash().get(key, String.valueOf(event)));
    }

    @Override
    public List<EventFixtureEntity> getEventFixtureByEvent(String season, int event) {
        List<EventFixtureEntity> list = Lists.newArrayList();
        String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "event", event);
        Set<Object> set = this.redisTemplate.opsForSet().members(key);
        if (CollectionUtils.isEmpty(set)) {
            return list;
        }
        set.forEach(o -> list.add((EventFixtureEntity) o));
        return list.stream().sorted(Comparator.comparing(EventFixtureEntity::getId)).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<PlayerFixtureData>> getEventFixtureByTeamId(String season, int teamId) {
        Map<String, List<PlayerFixtureData>> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "teamId", teamId);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), v == null ? Lists.newArrayList() : (List<PlayerFixtureData>) v));
        return map;
    }

    @Override
    public Map<Integer, Map<String, List<PlayerFixtureData>>> getTeamEventFixtureMap(String season) {
        Map<Integer, Map<String, List<PlayerFixtureData>>> map = Maps.newHashMap();
        List<Integer> teamList = Lists.newArrayList();
        IntStream.rangeClosed(1, 20).forEach(teamList::add);
        this.redisTemplate.executePipelined((RedisCallback<Object>) redisConnection -> {
            teamList.forEach(teamId -> {
                String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "teamId", teamId);
                Map<String, List<PlayerFixtureData>> teamMap = Maps.newHashMap();
                this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> teamMap.put(String.valueOf(k), v == null ? Lists.newArrayList() : (List<PlayerFixtureData>) v));
                map.put(teamId, teamMap);
            });
            return null;
        });
        return map;
    }

    @Override
    public Map<String, Map<String, List<LiveFixtureData>>> getEventLiveFixtureMap() {
        Map<String, Map<String, List<LiveFixtureData>>> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", LiveFixtureData.class.getSimpleName());
        this.redisTemplate.opsForHash().keys(key).forEach(teamId ->
                map.put(teamId.toString(), (Map<String, List<LiveFixtureData>>) this.redisTemplate.opsForHash().get(key, teamId)));
        return map;
    }

    @Override
    public Map<String, PlayerEntity> getPlayerMap(String season) {
        Map<String, PlayerEntity> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), season);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), (PlayerEntity) v));
        return map;
    }

    @Override
    public PlayerEntity getPlayerByElement(String season, int element) {
        String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), season);
        return (PlayerEntity) this.redisTemplate.opsForHash().get(key, String.valueOf(element));
    }

    @Override
    public Map<String, PlayerStatEntity> getPlayerStatMap(String season) {
        Map<String, PlayerStatEntity> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerStatEntity.class.getSimpleName(), season);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), (PlayerStatEntity) v));
        return map;
    }

    @Override
    public PlayerStatEntity getPlayerStatByElement(String season, int element) {
        String key = StringUtils.joinWith("::", PlayerStatEntity.class.getSimpleName(), season);
        return (PlayerStatEntity) this.redisTemplate.opsForHash().get(key, String.valueOf(element));
    }

    @Override
    public Map<String, EventLiveEntity> getEventLiveByEvent(int event) {
        Map<String, EventLiveEntity> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventLiveEntity.class.getSimpleName(), event);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (EventLiveEntity) v));
        return map;
    }

    @Override
    public Map<String, EventLiveExplainEntity> getEventLiveExplainByEvent(int event) {
        Map<String, EventLiveExplainEntity> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventLiveExplainEntity.class.getSimpleName(), event);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (EventLiveExplainEntity) v));
        return map;
    }

    @Override
    public Map<String, EventLiveSummaryEntity> getEventLiveSummaryMap(String season) {
        Map<String, EventLiveSummaryEntity> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventLiveSummaryEntity.class.getSimpleName(), season);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (EventLiveSummaryEntity) v));
        return map;
    }

    @Override
    public Map<String, Map<String, Integer>> getLiveBonusCacheMap() {
        Map<String, Map<String, Integer>> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", "LiveBonusData");
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), (Map<String, Integer>) v));
        return map;
    }

    @Override
    public Map<String, EventOverallResultData> getEventOverallResultMap(String season) {
        Map<String, EventOverallResultData> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", "EventOverallResult", season);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (EventOverallResultData) v));
        return map;
    }

    @Override
    public EventOverallResultData getEventOverallResultByEvent(String season, int event) {
        String key = StringUtils.joinWith("::", "EventOverallResult", season);
        return (EventOverallResultData) this.redisTemplate.opsForHash().get(key, String.valueOf(event));
    }

    @Override
    public Map<String, List<PlayerHistoryData>> getPlayerHistoryMap() {
        Map<String, List<PlayerHistoryData>> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerHistoryEntity.class.getSimpleName());
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (List<PlayerHistoryData>) v));
        return map;
    }

    @Override
    public Map<String, List<PlayerSummaryEntity>> getPlayerSummaryMap() {
        Map<String, List<PlayerSummaryEntity>> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerSummaryEntity.class.getSimpleName());
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), (List<PlayerSummaryEntity>) v));
        return map;
    }

}

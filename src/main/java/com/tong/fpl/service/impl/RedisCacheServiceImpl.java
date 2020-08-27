package com.tong.fpl.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.*;
import com.tong.fpl.config.collector.PlayerValueCollector;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.ValueChangeType;
import com.tong.fpl.domain.data.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.data.response.EventFixturesRes;
import com.tong.fpl.domain.data.response.StaticRes;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.service.IInterfaceService;
import com.tong.fpl.service.IRedisCacheSerive;
import com.tong.fpl.service.db.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
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
public class RedisCacheServiceImpl implements IRedisCacheSerive {

    private final RedisTemplate<String, Object> redisTemplate;
    private final IInterfaceService interfaceService;
    private final TeamService teamNameService;
    private final EventService eventService;
    private final EventFixtureService eventFixtureService;
    private final PlayerService playerService;
    private final PlayerStatService playerStatService;
    private final PlayerValueService playerValueService;

    @Override
    public void insertTeam() {
        Optional<StaticRes> result = this.interfaceService.getBootstrapStaic();
        result.ifPresent(staticRes -> {
            // insert table
            this.teamNameService.remove(new QueryWrapper<TeamEntity>().eq("1", 1));
            List<TeamEntity> teamList = Lists.newArrayList();
            staticRes.getTeams().forEach(bootstrapTeam -> {
                TeamEntity teamEntity = new TeamEntity();
                teamEntity.setId(bootstrapTeam.getId());
                teamEntity.setName(bootstrapTeam.getName());
                teamEntity.setShortName(bootstrapTeam.getShortName());
                teamList.add(teamEntity);
            });
            this.teamNameService.saveBatch(teamList);
            log.info("insert team size:{}!", teamList.size());
            // set cache
            Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
            String season = CommonUtils.getCurrentSeason();
            // set team_name cache
            this.setTeamNameCache(cacheMap, teamList, season);
            // set team_short_name cache
            this.setTeamShortNameCache(cacheMap, teamList, season);
            RedisUtils.pipelineHashCache(cacheMap, -1, null);
        });
    }

    @Override
    public void insertHisTeam(String season) {
        MybatisPlusConfig.season.set(season);
        List<TeamEntity> teamList = this.teamNameService.list();
        MybatisPlusConfig.season.remove();
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        // set team_name cache
        this.setTeamNameCache(cacheMap, teamList, season);
        // set team_short_name cache
        this.setTeamShortNameCache(cacheMap, teamList, season);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    private void setTeamNameCache(Map<String, Map<String, Object>> cacheMap, List<TeamEntity> teamList, String season) {
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "name");
        RedisUtils.removeCacheByKey(key);
        teamList.forEach(o -> valueMap.put(String.valueOf(o.getId()), o.getName()));
        cacheMap.put(key, valueMap);
    }

    private void setTeamShortNameCache(Map<String, Map<String, Object>> cacheMap, List<TeamEntity> teamList, String season) {
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "shortName");
        RedisUtils.removeCacheByKey(key);
        teamList.forEach(o -> valueMap.put(String.valueOf(o.getId()), o.getShortName()));
        cacheMap.put(key, valueMap);
    }

    @Override
    public void insertEvent() {
        Optional<StaticRes> result = this.interfaceService.getBootstrapStaic();
        result.ifPresent(staticRes -> {
            // insert table
            this.eventService.remove(new QueryWrapper<EventEntity>().eq("1", 1));
            List<EventEntity> eventList = Lists.newArrayList();
            staticRes.getEvents().forEach(bootstrapEvent -> {
                EventEntity eventEntity = new EventEntity();
                BeanUtil.copyProperties(bootstrapEvent, eventEntity, CopyOptions.create().ignoreNullValue());
                eventEntity.setId(bootstrapEvent.getId())
                        .setDeadlineTime(CommonUtils.getZoneDate(bootstrapEvent.getDeadlineTime()));
                eventList.add(eventEntity);
            });
            this.eventService.saveBatch(eventList);
            log.info("insert event size:{}!", eventList.size());
            // set cache
            Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
            Map<String, Object> valueMap = Maps.newHashMap();
            String key = StringUtils.joinWith("::", EventEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
            RedisUtils.removeCacheByKey(key);
            eventList.forEach(o -> valueMap.put(String.valueOf(o.getId()), o.getDeadlineTime()));
            cacheMap.put(key, valueMap);
            RedisUtils.pipelineHashCache(cacheMap, -1, null);
        });
    }

    @Override
    public void insertHisEvent(String season) {
        MybatisPlusConfig.season.set(season);
        List<EventEntity> eventList = this.eventService.list();
        MybatisPlusConfig.season.remove();
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        Map<String, Object> valueMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventEntity.class.getSimpleName(), season);
        RedisUtils.removeCacheByKey(key);
        eventList.forEach(o -> valueMap.put(String.valueOf(o.getId()), o.getDeadlineTime()));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertEventFixture() {
        List<EventFixtureEntity> fixtureList = Lists.newArrayList();
        Map<String, Set<Object>> cacheMap = Maps.newHashMap();
        // set cache by event
        IntStream.range(1, 39).forEach(event -> fixtureList.addAll(this.insertEventFixtureByEvent(cacheMap, event)));
        // set cache by team
        this.setEventFixtureCacheByTeam(cacheMap, CommonUtils.getCurrentSeason(), fixtureList);
        RedisUtils.pipelineSetCache(cacheMap, -1, null);
    }

    private List<EventFixtureEntity> insertEventFixtureByEvent(Map<String, Set<Object>> cacheMap, int event) {
        log.info("start insert gw{} fixtures!", event);
        this.eventFixtureService.remove(new QueryWrapper<EventFixtureEntity>().lambda().eq(EventFixtureEntity::getEvent, event));
        List<EventFixtureEntity> eventFixtureList = Lists.newArrayList();
        Optional<List<EventFixturesRes>> result = this.interfaceService.getEventFixture(event);
        result.ifPresent(eventFixturesRes -> {
            eventFixturesRes.forEach(o -> {
                EventFixtureEntity eventFixtureEntity = new EventFixtureEntity();
                BeanUtil.copyProperties(o, eventFixtureEntity, CopyOptions.create().ignoreNullValue());
                eventFixtureEntity.setKickoffTime(CommonUtils.getZoneDate(o.getKickoffTime()));
                eventFixtureList.add(eventFixtureEntity);
            });
            this.eventFixtureService.saveBatch(eventFixtureList);
            log.info("insert event_fixture size:{}!", eventFixtureList.size());
            // set cache by event
            this.setEventFixtureCacheBySingleEvent(cacheMap, CommonUtils.getCurrentSeason(), event, eventFixtureList);
        });
        return eventFixtureList;
    }

    @Override
    public void insertHisEventFixture(String season) {
        MybatisPlusConfig.season.set(season);
        Multimap<Integer, EventFixtureEntity> eventFixtureMap = HashMultimap.create();
        this.eventFixtureService.list().forEach(o -> eventFixtureMap.put(o.getEvent(), o));
        MybatisPlusConfig.season.remove();
        // set cache by event
        Map<String, Set<Object>> cacheMap = Maps.newHashMap();
        eventFixtureMap.keySet().forEach(event -> this.setEventFixtureCacheBySingleEvent(cacheMap, season, event, eventFixtureMap.get(event)));
        // set cache by team
        this.setEventFixtureCacheByTeam(cacheMap, season, eventFixtureMap.values());
        RedisUtils.pipelineSetCache(cacheMap, -1, null);
    }

    private void setEventFixtureCacheBySingleEvent(Map<String, Set<Object>> cacheMap, String season, int event, Collection<EventFixtureEntity> eventFixtureList) {
        String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "event", event);
        Set<Object> valueSet = Sets.newHashSet();
        RedisUtils.removeCacheByKey(key);
        valueSet.addAll(eventFixtureList);
        cacheMap.put(key, valueSet);
    }

    private void setEventFixtureCacheByTeam(Map<String, Set<Object>> cacheMap, String season, Collection<EventFixtureEntity> fixtureList) {
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season));
        IntStream.range(1, 21).forEach(teamId -> {
            String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "teamId", teamId);
            Set<Object> valueSet = this.setEventFixtureValueByTeam(fixtureList, teamId);
            cacheMap.put(key, valueSet);
        });
    }

    private Set<Object> setEventFixtureValueByTeam(Collection<EventFixtureEntity> fixtureList, int teamId) {
        Set<Object> valueSet = Sets.newHashSet();
        // home game
        fixtureList.stream()
                .filter(o -> o.getTeamH() == teamId)
                .forEach(o -> valueSet.add(new PlayerFixtureData()
                        .setTeamId(teamId)
                        .setEvent(o.getEvent())
                        .setAgainstTeamId(o.getTeamA())
                        .setDifficulty(o.getTeamHDifficulty())
                        .setKickoffTime(o.getKickoffTime())
                        .setStarted(o.isStarted())
                        .setFinished(o.isFinished())
                        .setWasHome(true)
                        .setScore(o.getTeamHScore() + "-" + o.getTeamAScore())
                ));
        // away game
        fixtureList.stream()
                .filter(o -> o.getTeamA() == teamId)
                .forEach(o -> valueSet.add(new PlayerFixtureData()
                        .setTeamId(teamId)
                        .setEvent(o.getEvent())
                        .setAgainstTeamId(o.getTeamH())
                        .setDifficulty(o.getTeamADifficulty())
                        .setKickoffTime(o.getKickoffTime())
                        .setStarted(o.isStarted())
                        .setFinished(o.isFinished())
                        .setWasHome(false)
                        .setScore(o.getTeamAScore() + "-" + o.getTeamHScore())
                ));
        return valueSet;
    }

    @Override
    public void insertPlayer() {
        Optional<StaticRes> result = this.interfaceService.getBootstrapStaic();
        result.ifPresent(staticRes -> {
            // insert table
            this.playerService.remove(new QueryWrapper<PlayerEntity>().eq("1", 1));
            List<PlayerEntity> playerList = Lists.newArrayList();
            staticRes.getElements().forEach(o ->
                    playerList.add(new PlayerEntity()
                            .setElement(o.getId())
                            .setCode(o.getCode())
                            .setPrice(this.getPlayerCurrentPrice(o.getId()))
                            .setElementType(o.getElementType())
                            .setFirstName(o.getFirstName())
                            .setSecondName(o.getSecondName())
                            .setWebName(o.getWebName())
                            .setTeamId(o.getTeam())
                    ));
            this.playerService.saveBatch(playerList);
            log.info("insert player size:{}!", playerList.size());
            // set cache
            Map<String, Map<Object, Double>> cacheMap = Maps.newHashMap();
            String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
            Map<Object, Double> valueMap = Maps.newConcurrentMap();
            RedisUtils.removeCacheByKey(key);
            playerList.forEach(o -> valueMap.put(o, (double) o.getElement()));
            cacheMap.put(key, valueMap);
            RedisUtils.pipelineSortedSetCache(cacheMap, -1, null);
        });
    }

    private int getPlayerCurrentPrice(int element) {
        List<PlayerValueEntity> playerValueEntityList = this.playerValueService.list(new QueryWrapper<PlayerValueEntity>()
                .lambda()
                .eq(PlayerValueEntity::getElement, element)
                .orderByDesc(PlayerValueEntity::getUpdateTime));
        if (CollectionUtils.isEmpty(playerValueEntityList)) {
            return 0;
        }
        return playerValueEntityList.get(0).getValue();
    }

    @Override
    public void insertHisPlayer(String season) {
        MybatisPlusConfig.season.set(season);
        List<PlayerEntity> playerList = this.playerService.list();
        MybatisPlusConfig.season.remove();
        // set cache
        Map<String, Map<Object, Double>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), season);
        Map<Object, Double> valueMap = Maps.newConcurrentMap();
        RedisUtils.removeCacheByKey(key);
        playerList.forEach(o -> valueMap.put(o, (double) o.getElement()));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineSortedSetCache(cacheMap, -1, null);
    }

    @Override
    public void insertPlayerStat() {
        Map<Integer, Integer> insertTeamMap = this.getInsertTeamList();
        Optional<StaticRes> result = this.interfaceService.getBootstrapStaic();
        result.ifPresent(staticRes -> {
            List<PlayerStatEntity> playerStatList = Lists.newArrayList();
            staticRes.getElements().forEach(o -> {
                if (!insertTeamMap.containsKey(o.getTeam())) {
                    return;
                }
                // insert table
                PlayerStatEntity playerStatEntity = new PlayerStatEntity();
                playerStatEntity.setElement(o.getId());
                playerStatEntity.setMatchPlayed(insertTeamMap.get(o.getTeam()));
                BeanUtil.copyProperties(o, playerStatEntity, CopyOptions.create().ignoreNullValue());
                playerStatList.add(playerStatEntity);
            });
            this.playerStatService.saveBatch(playerStatList);
            log.info("insert player_stat size:{}", playerStatList.size());
            // set cache
            Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
            String key = StringUtils.joinWith("::", PlayerStatEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
            Map<String, Object> valueMap = Maps.newConcurrentMap();
            RedisUtils.removeCacheByKey(key);
            playerStatList.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
            cacheMap.put(key, valueMap);
            RedisUtils.pipelineHashCache(cacheMap, -1, null);
        });
    }

    private Map<Integer, Integer> getInsertTeamList() {
        Map<Integer, Integer> insertTeamMap = Maps.newHashMap();
        IntStream.range(1, 21).forEach(teamId -> {
            // match_played
            int matchPlayed = this.eventFixtureService.count(new QueryWrapper<EventFixtureEntity>().lambda()
                    .eq(EventFixtureEntity::isFinished, 1)
                    .and(a -> a.eq(EventFixtureEntity::getTeamH, teamId)
                            .or(i -> i.eq(EventFixtureEntity::getTeamA, teamId)))
            );
            this.playerStatService.remove(new QueryWrapper<PlayerStatEntity>().lambda()
                    .eq(PlayerStatEntity::getMatchPlayed, matchPlayed)
            );
            insertTeamMap.put(teamId, matchPlayed);
        });
        return insertTeamMap;
    }

    @Override
    public void insertHisPlayerStat(String season) {
        MybatisPlusConfig.season.set(season);
        List<PlayerStatEntity> playerStatList = this.playerStatService.list();
        MybatisPlusConfig.season.remove();
        // set cache
        Map<String, Map<String, Object>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerStatEntity.class.getSimpleName(), season);
        Map<String, Object> valueMap = Maps.newConcurrentMap();
        RedisUtils.removeCacheByKey(key);
        playerStatList.forEach(o -> valueMap.put(String.valueOf(o.getElement()), o));
        cacheMap.put(key, valueMap);
        RedisUtils.pipelineHashCache(cacheMap, -1, null);
    }

    @Override
    public void insertPlayerValue() {
        Optional<StaticRes> result = this.interfaceService.getBootstrapStaic();
        result.ifPresent(staticRes -> {
            // insert table
            String changeDate = LocalDate.now().format(DateTimeFormatter.ofPattern(Constant.SHORTDAY));
            this.playerValueService.remove(new QueryWrapper<PlayerValueEntity>().lambda()
                    .eq(PlayerValueEntity::getChangeDate, changeDate));
            Map<Integer, PlayerValueEntity> lastValueMap = this.playerValueService.list()
                    .stream()
                    .collect(new PlayerValueCollector());
            List<PlayerValueEntity> playerValueList = Lists.newArrayList();
            staticRes.getElements()
                    .stream()
                    .filter(o -> !lastValueMap.containsKey(o.getId()) || o.getNowCost() != lastValueMap.get(o.getId()).getValue())
                    .forEach(bootstrapPlayer -> {
                        int element = bootstrapPlayer.getId();
                        PlayerValueEntity lastEntity = lastValueMap.getOrDefault(element, null);
                        int lastValue = lastEntity != null ? lastEntity.getValue() : 0;
                        playerValueList.add(new PlayerValueEntity()
                                .setElement(element)
                                .setElementType(bootstrapPlayer.getElementType())
                                .setEvent(CommonUtils.getCurrentEvent())
                                .setValue(bootstrapPlayer.getNowCost())
                                .setChangeDate(changeDate)
                                .setChangeType(this.getChangeType(bootstrapPlayer.getNowCost(), lastValue))
                                .setLastValue(lastValue)
                        );
                    });
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
        });
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
            updatePlayerList.add(playerEntity);
            // set cache
            String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), CommonUtils.getCurrentSeason());
            this.redisTemplate.opsForZSet().removeRangeByScore(key, o.getElement(), o.getElement());
            this.redisTemplate.opsForZSet().add(key, playerEntity, o.getElement());
        });
        this.playerService.updateBatchById(updatePlayerList);
    }

    @Override
    public Map<Integer, String> getTeamNameMap(String season) {
        Map<Integer, String> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "name");
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(Integer.valueOf(k.toString()), (String) v));
        return map;
    }

    @Override
    public Map<Integer, String> getTeamShortNameMap(String season) {
        Map<Integer, String> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), season, "shortName");
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(Integer.valueOf(k.toString()), (String) v));
        return map;
    }

    @Override
    public Map<Integer, String> getDeadlineMap(String season) {
        Map<Integer, String> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventEntity.class.getSimpleName(), season);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(Integer.valueOf(k.toString()), v.toString()));
        return map;
    }

    @Override
    public String getDeadlineByEvent(String season, int event) {
        String key = StringUtils.joinWith("::", EventEntity.class.getSimpleName(), season);
        return (String) this.redisTemplate.opsForHash().get(key, String.valueOf(event));
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
    public List<PlayerFixtureData> getEventFixtureByTeamId(String season, int teamId) {
        List<PlayerFixtureData> list = Lists.newArrayList();
        String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "teamId", teamId);
        Set<Object> set = this.redisTemplate.opsForSet().members(key);
        if (CollectionUtils.isEmpty(set)) {
            return list;
        }
        set.forEach(o -> list.add((PlayerFixtureData) o));
        return list.stream().sorted(Comparator.comparing(PlayerFixtureData::getEvent)).collect(Collectors.toList());
    }

    @Override
    public PlayerEntity getPlayerByElememt(String season, int element) {
        String key = StringUtils.joinWith("::", PlayerEntity.class.getSimpleName(), season);
        Set<Object> set = this.redisTemplate.opsForZSet().rangeByScore(key, element, element);
        if (CollectionUtils.isEmpty(set)) {
            return null;
        }
        return set.stream().map(PlayerEntity.class::cast).findFirst().orElse(null);
    }

    @Override
    public PlayerStatEntity getPlayerStatByElement(String season, int element) {
        String key = StringUtils.joinWith("::", PlayerStatEntity.class.getSimpleName(), season);
        return (PlayerStatEntity) this.redisTemplate.opsForHash().get(key, String.valueOf(element));
    }

    @Override
    public List<PlayerValueEntity> getPlayerValueByChangeDay(String changeDay) {
        List<PlayerValueEntity> list = Lists.newArrayList();
        String key = StringUtils.joinWith("::", PlayerValueEntity.class.getSimpleName(), changeDay);
        Set<Object> set = this.redisTemplate.opsForSet().members(key);
        if (CollectionUtils.isEmpty(set)) {
            return list;
        }
        set.forEach(o -> list.add((PlayerValueEntity) o));
        return list;
    }

}

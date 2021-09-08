package com.tong.fpl.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerHistoryData;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, List<PlayerFixtureData>> getEventFixtureByTeamId(String season, int teamId) {
        Map<String, List<PlayerFixtureData>> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", EventFixtureEntity.class.getSimpleName(), season, "teamId", teamId);
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(String.valueOf(k), v == null ? Lists.newArrayList() : (List<PlayerFixtureData>) v));
        return map;
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, List<PlayerHistoryData>> getPlayerHistoryMap() {
        Map<String, List<PlayerHistoryData>> map = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerHistoryEntity.class.getSimpleName());
        this.redisTemplate.opsForHash().entries(key).forEach((k, v) -> map.put(k.toString(), (List<PlayerHistoryData>) v));
        return map;
    }

}

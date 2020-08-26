package com.tong.fpl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tong.fpl.domain.entity.PlayerValueEntity;
import com.tong.fpl.service.ICacheSerive;
import com.tong.fpl.service.db.PlayerValueService;
import com.tong.fpl.utils.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Create by tong on 2020/8/22
 */
public class CacheTest extends FplApplicationTests {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ICacheSerive cacheSerive;
    @Autowired
    private PlayerValueService playerValueService;

    @Test
    void insertTeam() {
        this.cacheSerive.insertTeam();
    }

    @ParameterizedTest
    @CsvSource({"1920"})
    void insertHisTeam(String season) {
        this.cacheSerive.insertHisTeam(season);
    }

    @Test
    void insertEvent() {
        this.cacheSerive.insertEvent();
    }

    @ParameterizedTest
    @CsvSource({"1920"})
    void insertHisEvent(String season) {
        this.cacheSerive.insertHisEvent(season);
    }

    @Test
    void insertEventFixture() {
        this.cacheSerive.insertEventFixture();
    }

    @ParameterizedTest
    @CsvSource({"1920"})
    void insertHisEventFixture(String season) {
        this.cacheSerive.insertHisEventFixture(season);
    }

    @Test
    void insertPlayer() {
        this.cacheSerive.insertPlayer();
    }

    @ParameterizedTest
    @CsvSource({"1920"})
    void insertHisPlayer(String season) {
        this.cacheSerive.insertHisPlayer(season);
    }

    @Test
    void insertPlayerStat() {
        this.cacheSerive.insertPlayerStat();
    }

    @ParameterizedTest
    @CsvSource({"1920"})
    void insertHisPlayerStat(String season) {
        this.cacheSerive.insertHisPlayerStat(season);
    }

    @Test
    void insertPlayerValue() {
        this.cacheSerive.insertPlayerValue();
    }

    @Test
    void redis() {
        String changeDate = "20200827";
        List<PlayerValueEntity> playerValueEntityList = this.playerValueService.list();
        Map<String, Set<Object>> cacheMap = Maps.newHashMap();
        String key = StringUtils.joinWith("::", PlayerValueEntity.class.getSimpleName(), changeDate);
        Set<Object> valueSet = Sets.newHashSet();
        valueSet.addAll(playerValueEntityList);
        cacheMap.put(key, valueSet);
        RedisUtils.pipelineSetCache(cacheMap, 1, TimeUnit.DAYS);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"EventFixtureEntity::1920*"})
    void redisClear(String pattern) {
        RedisUtils.removeCacheByKeyPattern(pattern);
        System.out.println(1);
    }

}

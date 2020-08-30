package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.data.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/8/22
 */
public class RedisCacheTest extends FplApplicationTests {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private IRedisCacheSerive redisCacheSerive;

    @Test
    void insertTeam() {
        this.redisCacheSerive.insertTeam();
    }

    @ParameterizedTest
    @CsvSource({"1920"})
    void insertHisTeam(String season) {
        this.redisCacheSerive.insertHisTeam(season);
    }

    @Test
    void insertEvent() {
        this.redisCacheSerive.insertEvent();
    }

    @ParameterizedTest
    @CsvSource({"1920"})
    void insertHisEvent(String season) {
        this.redisCacheSerive.insertHisEvent(season);
    }

    @Test
    void insertEventFixture() {
        this.redisCacheSerive.insertEventFixture();
    }

    @ParameterizedTest
    @CsvSource({"1920"})
    void insertHisEventFixture(String season) {
        this.redisCacheSerive.insertHisEventFixture(season);
    }

    @Test
    void insertPlayer() {
        this.redisCacheSerive.insertPlayer();
    }

    @ParameterizedTest
    @CsvSource({"1920"})
    void insertHisPlayer(String season) {
        this.redisCacheSerive.insertHisPlayer(season);
    }

    @Test
    void insertPlayerStat() {
        this.redisCacheSerive.insertPlayerStat();
    }

    @ParameterizedTest
    @CsvSource({"1920"})
    void insertHisPlayerStat(String season) {
        this.redisCacheSerive.insertHisPlayerStat(season);
    }

    @Test
    void insertPlayerValue() {
        this.redisCacheSerive.insertPlayerValue();
    }

    @ParameterizedTest
    @CsvSource({"2021"})
    void getTeamNameMap(String season) {
        Map<Integer, String> map = this.redisCacheSerive.getTeamNameMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021"})
    void getTeamShrotNameMap(String season) {
        Map<Integer, String> map = this.redisCacheSerive.getTeamShortNameMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021"})
    void getDeadlineMap(String season) {
        Map<Integer, String> map = this.redisCacheSerive.getDeadlineMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 1"})
    void getDeadlineByEvent(String season, int event) {
        String a = this.redisCacheSerive.getDeadlineByEvent(season, event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 211"})
    void getPlayerByElememt(String season, int element) {
        long startTime = System.currentTimeMillis();
        PlayerEntity playerEntity = this.redisCacheSerive.getPlayerByElememt(season, element);
        long endTime = System.currentTimeMillis();
        System.out.println("escape: " + (endTime - startTime) + "ms");
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 15"})
    void getEventFixtureByEvent(String season, int event) {
        List<EventFixtureEntity> list = this.redisCacheSerive.getEventFixtureByEvent(season, event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 1"})
    void getEventFixtureByTeam(String season, int teamId) {
        List<PlayerFixtureData> list = this.redisCacheSerive.getEventFixtureByTeamId(season, teamId);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"20200827"})
    void getPlayerValueByChangeDay(String changeDate) {
        List<PlayerValueEntity> list = this.redisCacheSerive.getPlayerValueByChangeDay(changeDate);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1920, 221"})
    void getPlayerStatByElement(String season, int element) {
        long startTime = System.currentTimeMillis();
        PlayerStatEntity playerStatEntity = this.redisCacheSerive.getPlayerStatByElement(season, element);
        long endTime = System.currentTimeMillis();
        System.out.println("escape: " + (endTime - startTime) + "ms");
        System.out.println(1);
    }

    @Test
    void redis() {
        String key = StringUtils.joinWith("::", TeamEntity.class.getSimpleName(), CommonUtils.getCurrentSeason(), "name");
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"spring"})
    void redisClear(String key) {
        RedisUtils.removeCacheByKey(key);
    }

}

package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.entity.EventFixtureEntity;
import com.tong.fpl.domain.entity.EventLiveSummaryEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.PlayerStatEntity;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/8/22
 */
public class RedisCacheTest extends FplApplicationTests {

    @Autowired
    private IRedisCacheService redisCacheSerive;

    @ParameterizedTest
    @CsvSource({"2021"})
    void getTeamNameMap(String season) {
        Map<String, String> map = this.redisCacheSerive.getTeamNameMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021"})
    void getTeamShrotNameMap(String season) {
        Map<String, String> map = this.redisCacheSerive.getTeamShortNameMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 21"})
    void getUtcDeadlineByEvent(String season, int event) {
        String a = this.redisCacheSerive.getUtcDeadlineByEvent(season, event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 21"})
    void getDeadlineByEvent(String season, int event) {
        String a = this.redisCacheSerive.getDeadlineByEvent(season, event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 211"})
    void getPlayerByElememt(String season, int element) {
        long startTime = System.currentTimeMillis();
        PlayerEntity playerEntity = this.redisCacheSerive.getPlayerByElement(season, element);
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
    void getEventFixtureByTeamId(String season, int teamId) {
        Map<String, List<PlayerFixtureData>> map = this.redisCacheSerive.getEventFixtureByTeamId(season, teamId);
        System.out.println(1);
    }

    @Test
    void getEventLiveFixture() {
        Map<String, Map<String, List<LiveFixtureData>>> map = this.redisCacheSerive.getEventLiveFixtureMap();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2122"})
    void getPlayerMap(String season) {
        Map<String, PlayerEntity> map = this.redisCacheSerive.getPlayerMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2122, 233"})
    void getPlayerByElement(String season, int element) {
        PlayerEntity playerEntity = this.redisCacheSerive.getPlayerByElement(season, element);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2122"})
    void getPlayerStatMap(String season) {
        Map<String, PlayerStatEntity> map = this.redisCacheSerive.getPlayerStatMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1920, 221"})
    void getPlayerStatByElement(String season, int element) {
        PlayerStatEntity playerStatEntity = this.redisCacheSerive.getPlayerStatByElement(season, element);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2122"})
    void getEventLiveSummaryMap(String season) {
        Map<String, EventLiveSummaryEntity> map = this.redisCacheSerive.getEventLiveSummaryMap(season);
        System.out.println(1);
    }

    @Test
    void getLiveBonusCacheMap() {
        Map<String, Map<String, Integer>> map = this.redisCacheSerive.getLiveBonusCacheMap();
        System.out.println(1);
    }

    @Test
    void getTeamEventFixtureMap() {
        Map<Integer, Map<String, List<PlayerFixtureData>>> map = this.redisCacheSerive.getTeamEventFixtureMap("2021");
        System.out.println(1);
    }

    @Test
    void redis() {
        String key = "insertEventTransfers";
        int event = (int) RedisUtils.getValueByKey(key).orElse(0);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"api"})
    void redisClear(String key) {
        RedisUtils.removeCacheByKey(key);
    }

}

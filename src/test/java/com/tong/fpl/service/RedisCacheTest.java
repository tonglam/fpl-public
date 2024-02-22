package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerHistoryData;
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
    private IInterfaceService interfaceService;
    @Autowired
    private IRedisCacheService redisCacheService;

    /**
     * @apiNote insert
     */
    @ParameterizedTest
    @CsvSource({"18"})
    void insertSingleEventFixture(int event) {
        this.interfaceService.getEventFixture(event).ifPresent(eventFixturesResList -> this.redisCacheService.insertSingleEventFixture(event, eventFixturesResList));
    }

    @ParameterizedTest
    @CsvSource({"6"})
    void insertSingleEventFixtureCache(int event) {
        this.interfaceService.getEventFixture(event).ifPresent(eventFixturesResList -> this.redisCacheService.insertSingleEventFixtureCache(event, eventFixturesResList));
    }

    @Test
    void insertLiveFixtureCache() {
        this.redisCacheService.insertLiveFixtureCache();
    }

    @Test
    void insertPlayer() {
        this.interfaceService.getBootstrapStatic().ifPresent(staticRes -> this.redisCacheService.insertPlayer(staticRes));
    }

    @Test
    void insertPlayerStat() {
        this.interfaceService.getBootstrapStatic().ifPresent(staticRes -> this.redisCacheService.insertPlayerStat(staticRes));
    }

    @Test
    void insertPlayerValue() {
        this.interfaceService.getBootstrapStatic().ifPresent(staticRes -> this.redisCacheService.insertPlayerValue(staticRes));
    }

    @ParameterizedTest
    @CsvSource({"18"})
    void insertEventLive(int event) {
        this.interfaceService.getEventLive(event).ifPresent(eventLiveRes -> this.redisCacheService.insertEventLive(event, eventLiveRes));
    }

    @ParameterizedTest
    @CsvSource({"6"})
    void insertEventLiveCache(int event) {
        this.interfaceService.getEventLive(event).ifPresent(eventLiveRes -> this.redisCacheService.insertEventLiveCache(event, eventLiveRes));
    }

    @Test
    void insertLiveBonusCache() {
        this.redisCacheService.insertLiveBonusCache();
    }

    @Test
    void insertEventOverallResult() {
        this.interfaceService.getBootstrapStatic().ifPresent(staticRes -> this.redisCacheService.insertEventOverallResult(staticRes));
    }

    /**
     * @apiNote get
     */
    @Test
    void getCurrentEvent() {
        int event = this.redisCacheService.getCurrentEvent();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021"})
    void getTeamNameMap(String season) {
        Map<String, String> map = this.redisCacheService.getTeamNameMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021"})
    void getTeamShortNameMap(String season) {
        Map<String, String> map = this.redisCacheService.getTeamShortNameMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 21"})
    void getUtcDeadlineByEvent(String season, int event) {
        String a = this.redisCacheService.getUtcDeadlineByEvent(season, event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 21"})
    void getDeadlineByEvent(String season, int event) {
        String a = this.redisCacheService.getDeadlineByEvent(season, event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 15"})
    void getEventFixtureByEvent(String season, int event) {
        List<EventFixtureEntity> list = this.redisCacheService.getEventFixtureByEvent(season, event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 1"})
    void getEventFixtureByTeamId(String season, int teamId) {
        Map<String, List<PlayerFixtureData>> map = this.redisCacheService.getEventFixtureByTeamId(season, teamId);
        System.out.println(1);
    }

    @Test
    void getTeamEventFixtureMap() {
        Map<Integer, Map<String, List<PlayerFixtureData>>> map = this.redisCacheService.getTeamEventFixtureMap("2021");
        System.out.println(1);
    }

    @Test
    void getEventLiveFixture() {
        Map<String, Map<String, List<LiveFixtureData>>> map = this.redisCacheService.getEventLiveFixtureMap();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2122"})
    void getPlayerMap(String season) {
        Map<String, PlayerEntity> map = this.redisCacheService.getPlayerMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2021, 211"})
    void getPlayerByElement(String season, int element) {
        long startTime = System.currentTimeMillis();
        PlayerEntity playerEntity = this.redisCacheService.getPlayerByElement(season, element);
        long endTime = System.currentTimeMillis();
        System.out.println("escape: " + (endTime - startTime) + "ms");
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2122"})
    void getPlayerStatMap(String season) {
        Map<String, PlayerStatEntity> map = this.redisCacheService.getPlayerStatMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"1920, 221"})
    void getPlayerStatByElement(String season, int element) {
        PlayerStatEntity playerStatEntity = this.redisCacheService.getPlayerStatByElement(season, element);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"6"})
    void getEventLiveByEvent(int event) {
        Map<String, EventLiveEntity> map = this.redisCacheService.getEventLiveByEvent(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"6"})
    void getEventLiveExplainByEvent(int event) {
        Map<String, EventLiveExplainEntity> map = this.redisCacheService.getEventLiveExplainByEvent(event);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2122"})
    void getEventLiveSummaryMap(String season) {
        Map<String, EventLiveSummaryEntity> map = this.redisCacheService.getEventLiveSummaryMap(season);
        System.out.println(1);
    }

    @Test
    void getLiveBonusCacheMap() {
        Map<String, Map<String, Integer>> map = this.redisCacheService.getLiveBonusCacheMap();
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2122"})
    void getEventOverallResultMap(String season) {
        Map<String, EventOverallResultData> map = this.redisCacheService.getEventOverallResultMap(season);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"2122, 3"})
    void getEventOverallResultByEvent(String season, int event) {
        EventOverallResultData data = this.redisCacheService.getEventOverallResultByEvent(season, event);
        System.out.println(1);
    }

    @Test
    void getPlayerHistoryMap() {
        Map<String, List<PlayerHistoryData>> map = this.redisCacheService.getPlayerHistoryMap();
        System.out.println(1);
    }

    @Test
    void redis() {
        String key = "insertEventTransfers";
        int event = (int) RedisUtils.getValueByKey(key).orElse(0);
        System.out.println(1);
    }

    @ParameterizedTest
    @CsvSource({"EventLive"})
    void redisClear(String key) {
        RedisUtils.removeCacheByKey(key);
    }

}

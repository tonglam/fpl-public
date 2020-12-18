package com.tong.fpl.service;

import com.tong.fpl.FplApplicationTests;
import com.tong.fpl.domain.entity.EventFixtureEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.PlayerStatEntity;
import com.tong.fpl.domain.entity.PlayerValueEntity;
import com.tong.fpl.domain.letletme.live.LiveFixtureData;
import com.tong.fpl.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Create by tong on 2020/8/22
 */
public class RedisCacheTest extends FplApplicationTests {

	@Autowired
	private IRedisCacheService redisCacheSerive;

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

	@ParameterizedTest
	@CsvSource({"10"})
	void insertSingleEventFixture(int event) {
		this.redisCacheSerive.insertSingleEventFixture(event);
	}

	@ParameterizedTest
	@CsvSource({"13"})
	void insertSingleEventFixtureCache(int event) {
		this.redisCacheSerive.insertSingleEventFixtureCache(event);
	}

	@Test
	void insertLiveFixtureCache() {
		this.redisCacheSerive.insertLiveFixtureCache();
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
	@CsvSource({"11"})
	void insertEventLive(int event) {
		this.redisCacheSerive.insertEventLive(event);
	}

	@ParameterizedTest
	@CsvSource({"11"})
	void insertEventLiveCache(int event) {
		this.redisCacheSerive.insertEventLiveCache(event);
	}

	@Test
	void inserLiveBonusCache() {
		this.redisCacheSerive.insertLiveBonusCache();
		System.out.println(1);
	}

	@Test
	void insertPosition() {
		this.redisCacheSerive.insertPosition();
	}

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

	@Test
	void getEventLiveFixture() {
		Map<String, Map<String, List<LiveFixtureData>>> map = this.redisCacheSerive.getEventLiveFixtureMap();
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
	void getPositionMap() {
		Map<String, String> map = this.redisCacheSerive.getPositionMap();
		System.out.println(1);
	}

	@Test
	void getLiveBonusCacheMap() {
		Map<String, Map<String, Integer>> map = this.redisCacheSerive.getLiveBonusCacheMap();
		System.out.println(1);
	}

	@Test
	void redis() {
		String key = "scoutEntry";
		Map<Object, Object> map = RedisUtils.getHashByKey(key);
		Set<Object> set = map.keySet();
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"qry"})
	void redisClear(String key) {
		RedisUtils.removeCacheByKey(key);
	}

}

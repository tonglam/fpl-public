package com.tong.fpl;

import com.google.common.collect.Sets;
import com.tong.fpl.service.ICacheSerive;
import com.tong.fpl.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/8/22
 */
public class CacheTest extends FplApplicationTests {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private ICacheSerive cacheSerive;

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

	@Test
	void redis() {
		int a = RedisUtils.countCacheByKeyPattern("Player*");
		System.out.println(1);
	}

	@ParameterizedTest
	@CsvSource({"2021-EventFixtureEntity::"})
	void redisClear(String prefix) {
		Set<String> set = Sets.newHashSet();
		IntStream.range(1, 39).forEach(event -> set.add(prefix + event));
		redisTemplate.delete(set);
		System.out.println(1);
	}

}

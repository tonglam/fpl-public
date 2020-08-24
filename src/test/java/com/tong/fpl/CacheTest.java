package com.tong.fpl;

import com.google.common.collect.Sets;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.service.ICacheSerive;
import com.tong.fpl.service.db.TeamService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Create by tong on 2020/8/22
 */
public class CacheTest extends FplApplicationTests {

    @Autowired
    private TeamService teamService;
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
    void insertPlayer() {
        this.cacheSerive.insertPlayer();
    }

    @ParameterizedTest
    @CsvSource({"1920"})
    void insertHisPlayer(String season) {
        this.cacheSerive.insertHisPlayer(season);
    }

    @Test
    void insertEventFixture() {
        this.cacheSerive.insertEventFixture();
    }

    @Test
    void redis() {
        String pattern = "PlayerEntity::element::1920*";
        List<PlayerEntity> teamEntityList = this.cacheSerive.getMultiValues(pattern)
                .stream()
                .map(o -> (PlayerEntity) o)
                .sorted(Comparator.comparing(PlayerEntity::getElement))
                .collect(Collectors.toList());
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

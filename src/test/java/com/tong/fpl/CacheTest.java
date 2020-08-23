package com.tong.fpl;

import com.google.common.collect.Sets;
import com.tong.fpl.config.mp.MybatisPlusConfig;
import com.tong.fpl.service.ICacheSerive;
import com.tong.fpl.service.db.TeamNameService;
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
    private TeamNameService teamNameService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ICacheSerive cacheSerive;

    @Test
    void insertTeamName() {
        this.cacheSerive.insertTeamName();
        System.out.println(1);
    }

    @Test
    void insertEvent() {
        this.cacheSerive.insertEvent();
        System.out.println(1);
    }

    @Test
    void insertEventFixture() {
        this.cacheSerive.insertEventFixture();
        System.out.println(1);
    }

    @Test
    void redis() {
        MybatisPlusConfig.season.get();
        this.teamNameService.list().forEach(teamNameEntity -> {
            String key = this.createKey(teamNameEntity, teamNameEntity.getTeamId());
            this.redisTemplate.opsForValue().set(key, teamNameEntity);
            this.redisTemplate.persist(key);
        });
        MybatisPlusConfig.season.remove();
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

    private <T> String createKey(T entity, Object key) {
        return "1920-" + entity.getClass().getSimpleName() + "::" + key;
    }

}

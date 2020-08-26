package com.tong.fpl.utils;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Create by tong on 2020/8/25
 */
@Component
public class RedisUtils {

    private static RedisTemplate<String, Object> redisTemplate;

    public static void pipelineValueCache(Map<String, Object> cacheMap, long expire, TimeUnit timeUnit) {
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                cacheMap.forEach((k, v) -> {
                    redisTemplate.opsForValue().set(k, v);
                    if (expire != -1) {
                        redisTemplate.expire(k, expire, timeUnit);
                    }
                });
                connection.close();
                return null;
            }
        });
    }

    public static void pipelineHashCache(Map<String, Map<String, Object>> cacheMap, long expire, TimeUnit timeUnit) {
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                cacheMap.forEach((k, v) -> {
                    redisTemplate.opsForHash().putAll(k, v);
                    if (expire != -1) {
                        redisTemplate.expire(k, expire, timeUnit);
                    }
                });
                connection.close();
                return null;
            }
        });
    }

    public static void pipelineListCache(Map<String, Object> cacheMap, long expire, TimeUnit timeUnit) {
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                cacheMap.forEach((k, v) -> {
                    redisTemplate.opsForList().leftPushAll(k, v);
                    if (expire != -1) {
                        redisTemplate.expire(k, expire, timeUnit);
                    }
                });
                connection.close();
                return null;
            }
        });
    }

    public static void pipelineSetCache(Map<String, Set<Object>> cacheMap, long expire, TimeUnit timeUnit) {
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                cacheMap.forEach((k, v) ->
                        v.forEach(value -> {
                            redisTemplate.opsForSet().add(k, value);
                            if (expire != -1) {
                                redisTemplate.expire(k, expire, timeUnit);
                            }
                        }));
                connection.close();
                return null;
            }
        });
    }

    public static void pipelineSortedSetCache(Map<String, Map<Object, Double>> cacheMap, long expire, TimeUnit timeUnit) {
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                cacheMap.forEach((k, v) ->
                        v.forEach((value, score) -> {
                            redisTemplate.opsForZSet().add(k, value, score);
                            if (expire != -1) {
                                redisTemplate.expire(k, expire, timeUnit);
                            }
                        }));
                connection.close();
                return null;
            }
        });
    }

    public static List<Object> getMultiValues(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (CollectionUtils.isEmpty(keys)) {
            return null;
        }
        return redisTemplate.opsForValue().multiGet(keys);
    }

    public static <T> T getObjectFromZset(String key, int score, Class<T> clz) {
        Set<Object> set = redisTemplate.opsForZSet().rangeByScore(key, score, score);
        if (CollectionUtils.isEmpty(set)) {
            return null;
        }
        return set.stream().map(o -> JsonUtils.json2obj(o.toString(), clz)).findFirst().orElse(null);
    }

    public static <T> List<T> getAllObjectListFromZset(String key, Class<T> clz) {
        List<T> list = Lists.newArrayList();
        redisTemplate.opsForZSet().scan(key, ScanOptions.NONE).forEachRemaining(o ->
                list.add(JsonUtils.json2obj(o.toString(), clz)));
        return list;
    }

    public static <T> List<T> getObjectListFromZset(String key, double minScore, double maxScore, Class<T> classType) {
        Set<Object> set = redisTemplate.opsForZSet().rangeByScore(key, minScore, maxScore);
        if (CollectionUtils.isEmpty(set)) {
            return null;
        }
        return set.stream().map(classType::cast).collect(Collectors.toList());
    }

    public static <T> void updateZsetValue(String key, T value, double score) {
        redisTemplate.opsForZSet().removeRangeByScore(key, score, score);
        redisTemplate.opsForZSet().add(key, value, score);
    }

    public static void removeCacheByKeyPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        redisTemplate.delete(keys);
    }

    public static int countCacheByKeyPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (CollectionUtils.isEmpty(keys)) {
            return 0;
        }
        return keys.size();
    }


    @Autowired
    private void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisUtils.redisTemplate = redisTemplate;
    }

}

package com.tong.fpl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    public static void removeCacheByKey(String key) {
        Set<String> keys = redisTemplate.keys(key + "*");
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

    public static Optional<Object> getValueByKey(String key) {
        return redisTemplate.hasKey(key) ? Optional.ofNullable(redisTemplate.opsForValue().get(key)) : Optional.empty();
    }

    public static Map<Object, Object> getHashByKey(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public static Object getHashValue(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    public static void setHashValue(String key, String hashKey, String value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Autowired
    private void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisUtils.redisTemplate = redisTemplate;
    }

}

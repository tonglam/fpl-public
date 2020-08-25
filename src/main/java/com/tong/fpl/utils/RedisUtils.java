package com.tong.fpl.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Create by tong on 2020/8/25
 */
@Component
public class RedisUtils {

	private static RedisTemplate<String, Object> redisTemplate;

	public static <T> String createKey(T entity, String keyName, Object key) {
		return createKey(CommonUtils.getCurrentSeason(), entity, keyName, key);
	}

	public static <T> String createKey(String season, T entity, String keyName, Object key) {
		return StringUtils.joinWith("::", entity.getClass().getSimpleName(), keyName, season, key);
	}

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

	@Autowired
	private void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		RedisUtils.redisTemplate = redisTemplate;
	}

}

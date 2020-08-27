package com.tong.fpl.config.cache;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Create by tong on 2020/8/22
 */
@Configuration
public class RedisConfig extends CachingConfigurerSupport {

	@Bean
	public KeyGenerator keyGenerator() {
		return (target, method, params) -> {
			StringBuilder sb = new StringBuilder();
			sb.append(StringUtils.joinWith("::", target.getClass().getSimpleName(), method.getName()));
			for (Object obj : params) {
				sb.append("::").append(obj.toString());
			}
			return sb.toString();
		};
	}

	@Bean("entityKeyGenerator")
	public KeyGenerator entityKeyGenerator() {
		return (target, method, params) -> StringUtils.join(params, "::");
	}

	@Bean
	@Primary
	public CacheManager cacheManager(LettuceConnectionFactory factory) {
		RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofHours(2))
				.disableCachingNullValues()
				.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer((valueSerializer())));
		return RedisCacheManager
				.builder(RedisCacheWriter.nonLockingRedisCacheWriter(factory))
				.cacheDefaults(cacheConfig).build();
	}

	@Bean
	@Primary
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);
		template.setKeySerializer(this.keySerializer());
		template.setHashKeySerializer(this.keySerializer());
		template.setValueSerializer(this.valueSerializer());
		template.setHashValueSerializer(this.valueSerializer());
		template.afterPropertiesSet();
		return template;
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
		StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
		stringRedisTemplate.setConnectionFactory(factory);
		return stringRedisTemplate;
	}

	private RedisSerializer<String> keySerializer() {
		return new StringRedisSerializer();
	}

	private RedisSerializer<Object> valueSerializer() {
		Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
		objectMapper.registerModule(new GuavaModule());
		jacksonSerializer.setObjectMapper(objectMapper);
		return jacksonSerializer;
	}

}

package com.tong.fpl.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Create by tong on 2021/4/12
 */
@Getter
@AllArgsConstructor
public enum RedisExpirationKey {

	EventPassedDeadline;

	public static boolean needListen(String redisKey) {
		return Arrays.stream(RedisExpirationKey.values())
				.filter(o -> StringUtils.equals(o.name(), StringUtils.substringBefore(redisKey, "::")))
				.map(o -> true)
				.findFirst()
				.orElse(false);
	}

}

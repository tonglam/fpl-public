package com.tong.fpl.log;

import lombok.extern.slf4j.Slf4j;

/**
 * Create by tong on 2020/5/8
 */
@Slf4j
public class HttpCallLog {

	public static void info(String message) {
		log.info(message);
	}

	public static void info(String str, Object... var2) {
		info(String.format(str, var2));
	}

	public static void timeElapsed(long time) {
		info("time elapsed: {} ms!", time);
	}

	public static void error(String str, String errorMsg) {
		log.error(String.format(str, errorMsg));
	}

}

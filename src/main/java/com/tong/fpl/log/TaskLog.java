package com.tong.fpl.log;

import lombok.extern.slf4j.Slf4j;

/**
 * Create by tong on 2020/9/21
 */
@Slf4j
public class TaskLog {

	public static void info(String message) {
		log.info(message);
	}

	public static void info(String format, Object... arguments) {
		log.info(format, arguments);
	}

	public static void error(String message) {
		log.error(message);
	}

	public static void error(String format, Object... arguments) {
		log.error(format, arguments);
	}


}

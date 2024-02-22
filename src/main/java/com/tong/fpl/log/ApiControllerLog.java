package com.tong.fpl.log;

import lombok.extern.slf4j.Slf4j;

/**
 * Create by tong on 2021/5/10
 */
@Slf4j
public class ApiControllerLog {

	public static void info(String format, Object... arguments) {
		log.info(format, arguments);
	}

	public static void error(String format, Object... arguments) {
		log.error(format, arguments);
	}

}

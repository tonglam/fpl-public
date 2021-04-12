package com.tong.fpl.log;

import lombok.extern.slf4j.Slf4j;

/**
 * Create by tong on 2020/5/8
 */
@Slf4j
public class ControllerLog {

	public static void info(String format, Object... arguments) {
		log.info(format, arguments);
	}

	public static void error(String format, Object... arguments) {
		log.error(format, arguments);
	}

}

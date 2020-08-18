package com.tong.fpl.log;

import lombok.extern.slf4j.Slf4j;

/**
 * Create by tong on 2019/10/28
 */
@Slf4j
public class ControllerLog {

	public static void info(String message) {
		log.info(message);
	}

	public static void error(String message) {
		log.error(message);
	}

}

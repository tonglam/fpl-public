package com.tong.fpl.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create by tong on 2019/10/28
 */
public class InterfaceLog {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceLog.class);

    public static void info(String message) {
        logger.info(message);
    }

    public static void error(String message) {
        logger.error(message);
    }

}

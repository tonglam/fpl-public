package com.tong.fpl.task;

import com.tong.fpl.log.TaskLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Create by tong on 2021/3/1
 */
@Slf4j
@Component
public class TempTask {

	@Scheduled(cron = "0 10 0-23 * * *")
	public void temp() {
		TaskLog.info("run temp task, time:{}", LocalDateTime.now());
	}

}

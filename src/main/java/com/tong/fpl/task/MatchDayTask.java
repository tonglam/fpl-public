package com.tong.fpl.task;

import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IRedisCacheSerive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Create by tong on 2020/7/21
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MatchDayTask {

	private final IQuerySerivce querySerivce;
	private final IRedisCacheSerive redisCacheSerive;

	@Scheduled(cron = "0 0/5 0-4 * * *")
	public void insertEventLiveCache() {
		log.info("start insertEventLiveCache task, time:{}", LocalDateTime.now());
		int event = this.querySerivce.getCurrentEvent();
		this.redisCacheSerive.insertEventLiveCache(event);
	}

}

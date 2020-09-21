package com.tong.fpl.task;

import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IRedisCacheSerive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Create by tong on 2020/7/21
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MatchDayTask {

	private final IQuerySerivce querySerivce;
	private final IRedisCacheSerive redisCacheSerive;

	@Scheduled(cron = "0 0/5 0-6,19-23 * * *")
	public void insertEventLiveCache() {
		int event = this.querySerivce.getCurrentEvent();
		if (!this.querySerivce.isMatchDay(event)) {
			return;
		}
		log.info("start true insertEventLiveCache task");
		this.redisCacheSerive.insertEventLiveCache(event);
		this.redisCacheSerive.insertLiveBonusCache();
		this.redisCacheSerive.insertSingleEventFixtureCache(event);
		this.redisCacheSerive.insertLiveFixtureCache();
	}

	@Scheduled(cron = "0 0 9 * * *")
	public void insertEventLive() {
		int event = this.querySerivce.getCurrentEvent();
		if (!this.querySerivce.isMatchDay(event)) {
			return;
		}
		log.info("start true insertEventLive task");
		this.redisCacheSerive.insertEventLive(event);
	}

}

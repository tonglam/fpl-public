package com.tong.fpl.api.impl;

import com.tong.fpl.api.ICacheApi;
import com.tong.fpl.service.IRedisCacheService;
import com.tong.fpl.service.IUpdateEventResultService;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/8/28
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CacheApiImpl implements ICacheApi {

	private final IRedisCacheService redisCacheService;
	private final IUpdateEventResultService updateEventResultService;

	@Override
	public void insertTeam() {
		this.redisCacheService.insertTeam();
	}

	@Override
	public void insertEvent() {
		this.redisCacheService.insertEvent();
	}

	@Override
	public void insertEventFixture() {
		this.redisCacheService.insertEventFixture();
	}

	@Override
	public void insertSingleEventFixture(int event) {
		this.redisCacheService.insertSingleEventFixture(event);
	}

	@Override
	public void insertPlayer() {
		this.redisCacheService.insertPlayer();
	}

	@Override
	public void insertPlayerStat() {
		this.redisCacheService.insertPlayerStat();
	}

	@Override
	public void insertPlayerValue() {
		this.redisCacheService.insertPlayerValue();
	}

	@Override
	public void insertEventLive(int event) {
		this.redisCacheService.insertEventLive(event);
	}

	@Override
	public void updateAllEventResult(int event) {
		this.updateEventResultService.updateAllEventResult(event);
	}

	@Override
	public void deleteKeys(String pattern) {
		RedisUtils.removeCacheByKey(pattern + "*");
	}

}

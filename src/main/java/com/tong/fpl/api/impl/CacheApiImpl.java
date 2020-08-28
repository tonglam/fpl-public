package com.tong.fpl.api.impl;

import com.tong.fpl.api.ICacheApi;
import com.tong.fpl.service.IRedisCacheSerive;
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

	private final IRedisCacheSerive redisCacheSerive;

	@Override
	public void insertTeam() {
		this.redisCacheSerive.insertTeam();
	}

	@Override
	public void insertHisTeam(String season) {
		this.redisCacheSerive.insertHisTeam(season);
	}

	@Override
	public void insertEvent() {
		this.redisCacheSerive.insertEvent();
	}

	@Override
	public void insertHisEvent(String season) {
		this.redisCacheSerive.insertHisEvent(season);
	}

	@Override
	public void insertEventFixture() {
		this.redisCacheSerive.insertEventFixture();
	}

	@Override
	public void insertHisEventFixture(String season) {
		this.redisCacheSerive.insertHisEventFixture(season);
	}

	@Override
	public void insertPlayer() {
		this.redisCacheSerive.insertPlayer();
	}

	@Override
	public void insertHisPlayer(String season) {
		this.redisCacheSerive.insertHisPlayer(season);
	}

	@Override
	public void insertPlayerStat() {
		this.redisCacheSerive.insertPlayerStat();
	}

	@Override
	public void insertHisPlayerStat(String season) {
		this.redisCacheSerive.insertHisPlayerStat(season);
	}

	@Override
	public void insertPlayerValue() {
		this.redisCacheSerive.insertPlayerValue();
	}

}

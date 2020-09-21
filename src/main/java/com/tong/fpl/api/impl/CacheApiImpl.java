package com.tong.fpl.api.impl;

import com.tong.fpl.api.ICacheApi;
import com.tong.fpl.service.IRedisCacheSerive;
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

    private final IRedisCacheSerive redisCacheSerive;

    @Override
    public void insertTeam() {
        this.redisCacheSerive.insertTeam();
    }

    @Override
    public void insertEvent() {
        this.redisCacheSerive.insertEvent();
    }

    @Override
    public void insertEventFixture() {
        this.redisCacheSerive.insertEventFixture();
    }

    @Override
    public void insertSingleEventFixture(int event) {
        this.redisCacheSerive.insertSingleEventFixture(event);
    }

    @Override
    public void insertPlayer() {
        this.redisCacheSerive.insertPlayer();
    }

    @Override
    public void insertPlayerStat() {
        this.redisCacheSerive.insertPlayerStat();
    }

    @Override
    public void insertPlayerValue() {
        this.redisCacheSerive.insertPlayerValue();
    }

    @Override
    public void insertEventLive(int event) {
        this.redisCacheSerive.insertEventLive(event);
    }

    @Override
    public void deleteKeys(String pattern) {
        RedisUtils.removeCacheByKey(pattern + "*");
    }

}

package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiCommon;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.team.TeamData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IRedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/2/26
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiCommonImpl implements IApiCommon {

    private final IRedisCacheService redisCacheService;
    private final IApiQueryService apiQueryService;

    @Override
    public Map<String, String> qryCurrentEventAndNextUtcDeadline() {
        return this.apiQueryService.qryCurrentEventAndNextUtcDeadline();
    }

    @Override
    public void insertEventLiveCache(int event) {
        this.redisCacheService.insertEventLive(event);
        this.redisCacheService.insertSingleEventFixtureCache(event);
        this.redisCacheService.insertLiveFixtureCache();
        this.redisCacheService.insertLiveBonusCache();
    }

    @Override
    public Map<String, Integer> qryEventAverageScore() {
        return this.apiQueryService.qryEventAverageScore();
    }

    @Override
    public List<TeamData> qryTeamList(String season) {
        return this.apiQueryService.qryTeamList(season);
    }

    @Override
    public List<String> qryAllLeagueName(String season) {
        return this.apiQueryService.qryAllLeagueName(season);
    }

    @Override
    public void refreshPlayerValue() {
        // TODO: 2021/8/9 改为插入redis事件
    }

    @Override
    public List<PlayerFixtureData> qryNextFixture() {
        return this.apiQueryService.qryNextFixture();
    }

}

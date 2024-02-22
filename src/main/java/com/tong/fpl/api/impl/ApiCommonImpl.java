package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiCommon;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.team.TeamData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IRefreshService;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/2/26
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiCommonImpl implements IApiCommon {

    private final IApiQueryService apiQueryService;
    private final IRefreshService refreshService;

    @Override
    public Map<String, String> qryCurrentEventAndNextUtcDeadline() {
        return this.apiQueryService.qryCurrentEventAndNextUtcDeadline();
    }

    @Override
    public void refreshEventAndDeadline() {
        RedisUtils.removeCacheByKey("getCurrentEvent");
        RedisUtils.removeCacheByKey("api::qryCurrentEventAndNextUtcDeadline");
    }

    @Override
    public void insertEventLiveCache(int event) {
        this.refreshService.refreshEventLiveCache(event);
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
    public List<LeagueInfoData> qryAllLeagueName(String season) {
        return this.apiQueryService.qryAllLeagueName(season);
    }

    @Override
    public List<PlayerFixtureData> qryNextFixture(int event) {
        return this.apiQueryService.qryNextFixture(event);
    }

    @Override
    public List<String> qryAllPopularScoutSource() {
        return this.apiQueryService.qryAllPopularScoutSource();
    }

    @Override
    public String qryMiniProgramNotice() {
        String key = StringUtils.joinWith("::", "MiniProgram", "Notice");
        Map<Object, Object> map = RedisUtils.getHashByKey(key);
        if (CollectionUtils.isEmpty(map) || !StringUtils.equalsIgnoreCase("ON", map.get("switch").toString())) {
            return "";
        }
        return map.get("content").toString();
    }

}

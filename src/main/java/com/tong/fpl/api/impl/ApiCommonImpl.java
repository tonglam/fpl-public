package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiCommon;
import com.tong.fpl.domain.letletme.event.EventDreamTeamData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.team.TeamData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IDataService;
import com.tong.fpl.utils.RedisUtils;
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

    private final IApiQueryService apiQueryService;
    private final IDataService eventDataService;

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
        this.eventDataService.refreshEventLiveCache(event);
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
    public List<PlayerFixtureData> qryNextFixture(int event) {
        return this.apiQueryService.qryNextFixture(event);
    }

    @Override
    public List<EventDreamTeamData> qryEventDreamTeam(int event) {
        return this.apiQueryService.qryEventDreamTeam(event);
    }

    @Override
    public List<EventDreamTeamData> qryEventEliteElements(int event) {
        return this.apiQueryService.qryEventEliteElements(event);
    }

}

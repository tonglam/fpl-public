package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiCommon;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.team.TeamData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/2/26
 */
@Slf4j
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommonApiController {

    private final IApiCommon apiCommon;

    @GetMapping("/qryCurrentEventAndNextUtcDeadline")
    public Map<String, String> qryCurrentEventAndNextUtcDeadline() {
        return this.apiCommon.qryCurrentEventAndNextUtcDeadline();
    }

    @GetMapping("/refreshEventAndDeadline")
    public void refreshEventAndDeadline() {
        this.apiCommon.refreshEventAndDeadline();
    }

    @GetMapping("/insertEventLiveCache")
    public void insertEventLiveCache(@RequestParam int event) {
        this.apiCommon.insertEventLiveCache(event);
    }

    @GetMapping("/qryEventAverageScore")
    public Map<String, Integer> qryEventAverageScore() {
        return this.apiCommon.qryEventAverageScore();
    }

    @GetMapping("/qryTeamList")
    public List<TeamData> qryTeamList(@RequestParam String season) {
        return this.apiCommon.qryTeamList(season);
    }

    @GetMapping("/qryAllLeagueName")
    public List<LeagueInfoData> qryAllLeagueName(@RequestParam String season) {
        return this.apiCommon.qryAllLeagueName(season);
    }

    @GetMapping("/qryNextFixture")
    public List<PlayerFixtureData> qryNextFixture(@RequestParam int event) {
        return this.apiCommon.qryNextFixture(event);
    }

    @GetMapping("/qryAllPopularScoutSource")
    public List<String> qryAllPopularScoutSource() {
        return this.apiCommon.qryAllPopularScoutSource();
    }

    @GetMapping("/qryMiniProgramNotice")
    public String qryMiniProgramNotice() {
        return this.apiCommon.qryMiniProgramNotice();
    }

}

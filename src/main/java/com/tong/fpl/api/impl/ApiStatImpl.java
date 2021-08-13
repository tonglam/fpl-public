package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiStat;
import com.tong.fpl.domain.letletme.league.LeagueEventSelectData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerSummaryData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.team.TeamSummaryData;
import com.tong.fpl.service.IApiQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiStatImpl implements IApiStat {

    private final IApiQueryService apiQueryService;

    @Override
    public Map<String, List<PlayerValueData>> qryPlayerValueByDate(String date) {
        return this.apiQueryService.qryPlayerValueByDate(date);
    }

    @Override
    public List<PlayerValueData> qryPlayerValueByElement(int element) {
        return this.apiQueryService.qryPlayerValueByElement(element);
    }

    @Override
    public Map<String, List<PlayerValueData>> qryPlayerValueByTeamId(int teamId) {
        return this.apiQueryService.qryPlayerValueByTeamId(teamId);
    }

    @Override
    public LeagueEventSelectData qryTeamSelectByLeagueName(String season, int event, String leagueName) {
        return this.apiQueryService.qryTeamSelectByLeagueName(season, event, leagueName);
    }

    @Override
    public LinkedHashMap<String, List<PlayerFixtureData>> qrySeasonFixture() {
        return this.apiQueryService.qrySeasonFixture();
    }

    @Override
    public PlayerInfoData qryPlayerInfo(String season, int code) {
        return this.apiQueryService.qryPlayerInfo(season, code);
    }

    @Override
    public PlayerSummaryData qryPlayerSummary(String season, int code) {
        return this.apiQueryService.qryPlayerSummary(season, code);
    }

    @Override
    public TeamSummaryData qryTeamSummary(String season, String name) {
        return this.apiQueryService.qryTeamSummary(season, name);
    }

}

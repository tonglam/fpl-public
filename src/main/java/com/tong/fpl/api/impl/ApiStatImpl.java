package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiStat;
import com.tong.fpl.domain.letletme.element.ElementEventLiveExplainData;
import com.tong.fpl.domain.letletme.league.LeagueEventSelectData;
import com.tong.fpl.domain.letletme.player.PlayerSummaryData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.team.TeamSummaryData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IDataService;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiStatImpl implements IApiStat {

    private final IApiQueryService apiQueryService;
    private final IDataService eventDataService;

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
    public void refreshPlayerValue() {
        this.eventDataService.refreshPlayerValue();
    }

    @Override
    public LeagueEventSelectData qryLeagueSelectByName(int event, String leagueName) {
        return this.apiQueryService.qryLeagueSelectByName(CommonUtils.getCurrentSeason(), event, leagueName);
    }

    @Override
    public void refreshLeagueSelect(int event, String leagueName) {
        this.eventDataService.refreshLeagueSelect(event, leagueName);
    }

    @Override
    public List<List<String>> qrySeasonFixture() {
        return this.apiQueryService.qrySeasonFixture();
    }

    @Override
    public PlayerSummaryData qryPlayerSummary(String season, int code) {
        return this.apiQueryService.qryPlayerSummary(season, code);
    }

    @Override
    public void refreshPlayerSummary(String season, int code) {
        this.eventDataService.refreshPlayerSummary(season, code);
    }

    @Override
    public TeamSummaryData qryTeamSummary(String season, String name) {
        return this.apiQueryService.qryTeamSummary(season, name);
    }

    @Override
    public void refreshTeamSummary(String season, String name) {
        this.eventDataService.refreshTeamSummary(season, name);
    }

    @Override
    public ElementEventLiveExplainData qryElementEventExplainResult(int event, int element) {
        return this.apiQueryService.qryElementEventExplainResult(event, element);
    }

}

package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiStat;
import com.tong.fpl.domain.letletme.element.ElementAgainstInfoData;
import com.tong.fpl.domain.letletme.element.ElementAgainstRecordData;
import com.tong.fpl.domain.letletme.element.ElementEventLiveExplainData;
import com.tong.fpl.domain.letletme.element.ElementSummaryData;
import com.tong.fpl.domain.letletme.league.LeagueEventSelectData;
import com.tong.fpl.domain.letletme.player.PlayerSummaryData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.team.TeamAgainstInfoData;
import com.tong.fpl.domain.letletme.team.TeamSummaryData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IRefreshService;
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
    private final IRefreshService refreshService;

    @Override
    public Map<String, List<PlayerValueData>> qryPlayerValueByDate(String date) {
        return this.apiQueryService.qryPlayerValueByDate(date);
    }

    @Override
    public List<PlayerValueData> qryPlayerPriceChange(String date) {
        return this.apiQueryService.qryPlayerPriceChange(date);
    }

    @Override
    public List<PlayerValueData> qryPlayerValueByElement(int element) {
        return this.apiQueryService.qryPlayerValueByElement(element);
    }

    @Override
    public void refreshPlayerValue() {
        this.refreshService.refreshPlayerValue();
    }

    @Override
    public LeagueEventSelectData qryLeagueSelectByName(int event, int leagueId, String leagueName) {
        return this.apiQueryService.qryLeagueSelectByName(CommonUtils.getCurrentSeason(), event, leagueId, leagueName);
    }

    @Override
    public void refreshLeagueSelect(int event, String leagueName) {
        this.refreshService.refreshLeagueSelect(event, leagueName);
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
        this.refreshService.refreshPlayerSummary(season, code);
    }

    @Override
    public TeamSummaryData qryTeamSummary(String season, String name) {
        return this.apiQueryService.qryTeamSummary(season, name);
    }

    @Override
    public void refreshTeamSummary(String season, String name) {
        this.refreshService.refreshTeamSummary(season, name);
    }

    @Override
    public ElementEventLiveExplainData qryElementEventExplainResult(int event, int element) {
        return this.apiQueryService.qryElementEventExplainResult(event, element);
    }

    @Override
    public TeamAgainstInfoData qryTeamAgainstRecordInfo(int teamId, int againstId) {
        return this.apiQueryService.qryTeamAgainstRecordInfo(teamId, againstId);
    }

    @Override
    public List<ElementSummaryData> qryTeamAgainstRecordResult(String season, int event, int teamHId, int teamAId) {
        return this.apiQueryService.qryTeamAgainstRecordResult(season, event, teamHId, teamAId);
    }

    @Override
    public List<ElementAgainstInfoData> qryTopElementAgainstInfo(int teamId, int againstId, boolean active) {
        return this.apiQueryService.qryTopElementAgainstInfo(teamId, againstId, active);
    }

    @Override
    public List<ElementAgainstRecordData> qryElementAgainstRecord(int teamId, int againstId, int elementCode) {
        return this.apiQueryService.qryElementAgainstRecord(teamId, againstId, elementCode);
    }

}

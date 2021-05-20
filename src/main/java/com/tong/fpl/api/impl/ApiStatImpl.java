package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiStat;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.service.IApiQueryService;
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
    public List<String> qryAllLeagueName() {
        return this.apiQueryService.qryAllLeagueName();
    }

    @Override
    public LeagueStatData qryTeamSelectByLeagueName(int event, String leagueName) {
        return this.apiQueryService.qryTeamSelectByLeagueName(event, leagueName);
    }

}

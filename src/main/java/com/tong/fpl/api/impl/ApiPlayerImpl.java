package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiPlayer;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFilterData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IRefreshService;
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
public class ApiPlayerImpl implements IApiPlayer {

    private final IApiQueryService apiQueryService;
    private final IRefreshService refreshService;

    @Override
    public PlayerInfoData qryPlayerInfoByElement(String season, int element) {
        return this.apiQueryService.qryPlayerInfoByElement(season, element);
    }

    @Override
    public PlayerInfoData qryPlayerInfoByCode(String season, int code) {
        return this.apiQueryService.qryPlayerInfoByCode(season, code);
    }

    @Override
    public LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType) {
        return this.apiQueryService.qryPlayerInfoByElementType(elementType);
    }

    @Override
    public PlayerDetailData qryPlayerDetailByElement(int element) {
        return this.apiQueryService.qryPlayerDetailByElement(element);
    }

    @Override
    public Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(String shortName) {
        return this.apiQueryService.qryTeamFixtureByShortName(shortName);
    }

    @Override
    public List<PlayerFilterData> qryFilterPlayers(String season) {
        return this.apiQueryService.qryFilterPlayers(season);
    }

    @Override
    public void refreshPlayerStat() {
        this.refreshService.refreshPlayerStat();
    }

}

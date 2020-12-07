package com.tong.fpl.api.impl;

import com.tong.fpl.api.IStatApi;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.ITableQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2020/9/2
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatApiImpl implements IStatApi {

    private final IQuerySerivce querySerivce;
    private final ITableQueryService tableQueryService;

    /**
     * @implNote price
     */
    @Override
    public TableData<PlayerValueData> qryPriceChangeList() {
        return this.tableQueryService.qryPriceChangeList();
    }

    /**
     * @implNote compare
     */
    @Override
    public TableData<PlayerInfoData> qryPlayerList(String season) {
        return this.tableQueryService.qryPlayerList(season);
    }

    /**
     * @implNote selected
     */
    @Override
    public List<String> qryTeamSelectStatList() {
        return this.querySerivce.qryTeamSelectStatList();
    }

    @Override
    public TableData<LeagueStatData> qryTeamSelectStatByName(String leagueName, int event) {
        return this.tableQueryService.qryTeamSelectStatByName(leagueName, event);
    }

    /**
     * @implNote scout
     */
    @Override
    public TableData<PlayerData> qryPageScoutPlayerList(int elementType, int page, int limit) {
        return this.tableQueryService.qryPageScoutPlayerList(elementType, page, limit);
    }

}

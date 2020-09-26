package com.tong.fpl.api.impl;

import com.tong.fpl.api.ILiveApi;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.ITableQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2020/8/3
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveApiImpl implements ILiveApi {

    private final ITableQueryService tableQueryService;
    private final IQuerySerivce querySerivce;

    @Override
    public TableData<LiveCalaData> qryEntryLivePoints(int entry) {
        return this.tableQueryService.qryEntryLivePoints(entry);
    }

    @Override
    public TableData<LiveCalaData> qryTournamentLivePoints(int tournamentId) {
        return this.tableQueryService.qryTournamentLivePoints(tournamentId);
    }

    @Override
    public List<LiveMatchData> qryLiveMatchList() {
        return this.querySerivce.qryLiveMatchList();
    }

    @Override
    public TableData<ElementEventResultData> qryLiveFixturePlayerList(int teamId) {
        return this.tableQueryService.qryLiveFixturePlayerList(teamId);
    }

}

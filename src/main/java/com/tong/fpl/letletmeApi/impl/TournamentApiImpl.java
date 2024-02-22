package com.tong.fpl.letletmeApi.impl;

import cn.hutool.core.bean.BeanUtil;
import com.tong.fpl.domain.letletme.entry.EntryCupData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.letletmeApi.ITournamentApi;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.service.ITableQueryService;
import com.tong.fpl.service.ITournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2020/6/24
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentApiImpl implements ITournamentApi {

    private final IQueryService queryService;
    private final ITableQueryService tableQueryService;
    private final ITournamentService tournamentService;

    /**
     * @implNote create
     */
    @Override
    public String createNewTournament(TournamentCreateData tournamentCreateData) {
        return this.tournamentService.createNewTournament(tournamentCreateData);
    }

    @Override
    public int countTournamentLeagueTeams(String url) {
        return this.queryService.qryCountTournamentLeagueTeams(url);
    }

    @Override
    public TableData<EntryInfoData> qryLeagueEntryList(String url) {
        return this.tableQueryService.qryLeagueEntryList(url);
    }

    @Override
    public boolean checkTournamentName(String name) {
        return this.tournamentService.checkTournamentName(name);
    }

    /**
     * @implNote result
     */
    @Override
    public TableData<TournamentEntryData> qryEntryTournamentList(int entry) {
        return this.tableQueryService.qryEntryTournamentList(entry);
    }

    /**
     * @implNote fixture
     */
    @Override
    public List<TournamentGroupFixtureData> qryGroupFixtureListById(int tournamentId) {
        return this.queryService.qryGroupFixtureListById(tournamentId);
    }

    @Override
    public List<TournamentKnockoutFixtureData> qryKnockoutFixtureListById(int tournamentId) {
        return this.queryService.qryKnockoutFixtureListById(tournamentId);
    }

    /**
     * @implNote pointsResult
     */
    @Override
    public TableData<TournamentGroupEventChampionData> qryPointsGroupChampion(int tournamentId) {
        return this.tableQueryService.qryPointsGroupChampion(tournamentId);
    }

    @Override
    public TableData<TournamentPointsGroupEventResultData> qryPagePointsGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
        return this.tableQueryService.qryPagePointsGroupResult(tournamentId, groupId, entry, page, limit);
    }

    @Override
    public TableData<EntryCupData> qryPageEntryEventCupResult(int entry, int page, int limit) {
        return this.tableQueryService.qryPageEntryEventCupResult(entry, page, limit);
    }

    @Override
    public KnockoutBracketData qryKnockoutBracketResultByTournament(int tournamentId) {
        return this.queryService.qryKnockoutBracketResultByTournament(tournamentId);
    }

    /**
     * @implNote battleResult
     */
    @Override
    public TableData<TournamentBattleGroupEventResultData> qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
        return this.tableQueryService.qryPageBattleGroupResult(tournamentId, groupId, entry, page, limit);
    }

    /**
     * @implNote manage
     */
    @Override
    public String updateTournamentInfo(TournamentCreateData tournamentCreateData) {
        return this.tournamentService.updateTournamentInfo(tournamentCreateData);
    }

    @Override
    public String deleteTournamentByName(String name) {
        return this.tournamentService.deleteTournamentByName(name);
    }

    /**
     * @implNote common
     */
    @Override
    public int getCurrentEvent() {
        return this.queryService.getCurrentEvent();
    }

    @Override
    public int getNextEvent() {
        return this.queryService.getNextEvent();
    }

    @Override
    public TableData<TournamentInfoData> qryTournamentList(TournamentQueryParam param) {
        return this.tableQueryService.qryTournamentList(param);
    }

    @Override
    public TournamentInfoData qryTournamentInfoById(int tournamentId) {
        return this.queryService.qryTournamentDataById(tournamentId);
    }

    @Override
    public EntryInfoData qryEntryInfo(int entry) {
        return BeanUtil.copyProperties(this.queryService.qryEntryInfo(entry), EntryInfoData.class);
    }

    @Override
    public TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId) {
        return this.tableQueryService.qryGroupInfoListByGroupId(tournamentId, groupId);
    }

}

package com.tong.fpl.api.impl;

import cn.hutool.core.bean.BeanUtil;
import com.google.common.collect.Lists;
import com.tong.fpl.api.ITournamentApi;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.service.IQuerySerivce;
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

    private final ITournamentService tournamentService;
    private final IQuerySerivce querySerivce;
    private final ITableQueryService tableQueryService;

    @Override
    public String createNewTournament(TournamentCreateData tournamentCreateData) {
        return this.tournamentService.createNewTournament(tournamentCreateData);
    }

    @Override
    public TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param) {
        return this.tableQueryService.qryTournamenList(param);
    }

    @Override
    public int countTournamentLeagueTeams(String url) {
        return this.tournamentService.countTournamentLeagueTeams(url);
    }

    @Override
    public boolean checkTournamentName(String name) {
        return this.tournamentService.checkTournamentName(name);
    }

    @Override
    public String updateTournament(TournamentCreateData tournamentCreateData) {
        return this.tournamentService.updateTournament(tournamentCreateData);
    }

    @Override
    public String deleteTournamentByName(String name) {
        return this.tournamentService.deleteTournamentByName(name);
    }

    @Override
    public TableData<EntryTournamentData> qryEntryTournamentList(int entry) {
        return this.tableQueryService.qryEntryTournamentList(entry);
    }

    @Override
    public EntryInfoData qryEntryInfoData(int entry) {
        return BeanUtil.copyProperties(this.querySerivce.qryEntryInfo(entry), EntryInfoData.class);
    }

    @Override
    public TournamentInfoData qryTournamentInfoById(int tournamentId) {
        TournamentInfoData tournamentInfoData = new TournamentInfoData();
        TournamentInfoEntity tournamentInfoEntity = this.querySerivce.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return tournamentInfoData;
        }
        BeanUtil.copyProperties(tournamentInfoEntity, tournamentInfoData);
        tournamentInfoData.setShowNum((int) Math.ceil(tournamentInfoData.getGroupNum() * 1.0 / 2));
        return tournamentInfoData;
    }

    @Override
    public List<TournamentKnockoutData> qryKnockoutListByTournamentId(int tournamentId) {
        List<TournamentKnockoutData> list = Lists.newArrayList();
        this.querySerivce.qryKnockoutListByTournamentId(tournamentId).forEach(o ->
                list.add(BeanUtil.copyProperties(o, TournamentKnockoutData.class)));
        return list;
    }

    @Override
    public TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId) {
        return this.tableQueryService.qryGroupInfoListByGroupId(tournamentId, groupId);
    }

    @Override
    public List<TournamentKnockoutResultData> qryKnockoutResultByTournament(int tournamentId) {
        return this.querySerivce.qryKnockoutResultByTournament(tournamentId);
    }

}

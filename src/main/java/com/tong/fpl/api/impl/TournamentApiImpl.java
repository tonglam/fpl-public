package com.tong.fpl.api.impl;

import cn.hutool.core.bean.BeanUtil;
import com.google.common.collect.Lists;
import com.tong.fpl.api.ITournamentApi;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.ITableQueryService;
import com.tong.fpl.service.ITournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
    public String createNewZjTournament(ZjTournamentCreateData zjTournamentCreateData) {
        return this.tournamentService.createNewZjTournament(zjTournamentCreateData);
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
    public String updateTournamentInfo(TournamentCreateData tournamentCreateData) {
        return this.tournamentService.updateTournamentInfo(tournamentCreateData);
    }

    @Override
    public String deleteTournamentByName(String name) {
        return this.tournamentService.deleteTournamentByName(name);
    }

    @Override
    public TableData<TournamentEntryData> qryEntryTournamentList(int entry) {
        return this.tableQueryService.qryEntryTournamentList(entry);
    }

    @Override
    public EntryInfoData qryEntryInfoData(int entry) {
        return BeanUtil.copyProperties(this.querySerivce.qryEntryInfo(entry), EntryInfoData.class);
    }

    @Cacheable(cacheNames = "tournamentData", key = "#tournamentId")
    @Override
    public TournamentInfoData qryTournamentInfoById(int tournamentId) {
        TournamentInfoData tournamentInfoData = new TournamentInfoData();
        TournamentInfoEntity tournamentInfoEntity = this.querySerivce.qryTournamentInfoById(tournamentId);
        if (tournamentInfoEntity == null) {
            return tournamentInfoData;
        }
        BeanUtil.copyProperties(tournamentInfoEntity, tournamentInfoData);
        tournamentInfoData
                .setGroupModeName(GroupMode.valueOf(tournamentInfoData.getGroupMode()).getModeName())
                .setKnockoutModeName(KnockoutMode.valueOf(tournamentInfoData.getKnockoutMode()).getModeName());
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
    public int qryZjTournamentPhaseOneRankByGroupId(int tournamentId, int currentGroupId) {
        return this.querySerivce.qryZjTournamentPhaseOneRankMap(tournamentId).getOrDefault(String.valueOf(currentGroupId), 0);
    }

    @Override
    public List<EntryInfoData> qryGroupEntryInfoList(int tournamentId, int groupId) {
        return this.querySerivce.qryGroupEntryInfoList(tournamentId, groupId);
    }

    @Override
    public Map<String, String> qryZjTournamentGroupNameMap(int tournamentId) {
        return this.querySerivce.qryZjTournamentGroupNameMap(tournamentId);
    }

    @Override
    public TournamentGroupData qryDiscloseGroupData(int tournamentId, int entry, int currentGroupId) {
        return this.querySerivce.qryDiscloseGroupData(tournamentId, entry, currentGroupId);
    }

    @Override
    public TableData<TournamentGroupData> qrySeeableGroupInfoListByGroupId(int tournamentId, int currentGroupId, int groupId) {
        return this.tableQueryService.qrySeeableGroupInfoListByGroupId(tournamentId, currentGroupId, groupId);
    }

    @Override
    public KnockoutBracketData qryKnockoutBracketResultByTournament(int tournamentId) {
        return this.querySerivce.qryKnockoutBracketResultByTournament(tournamentId);
    }

    @Override
    public List<TournamentKnockoutResultData> qryKnockoutResultByTournament(int tournamentId) {
        return this.querySerivce.qryKnockoutResultByTournament(tournamentId);
    }

    @Override
    public TableData<TournamentPointsGroupEventResultData> qryPagePointsGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
        return this.tableQueryService.qryPagePointsGroupResult(tournamentId, groupId, entry, page, limit);
    }

    @Override
    public TableData<TournamentBattleGroupEventResultData> qryPageBattleGroupResult(int tournamentId, int groupId, int entry, int page, int limit) {
        return this.tableQueryService.qryPageBattleGroupResult(tournamentId, groupId, entry, page, limit);
    }

    @Override
    public TableData<TournamentPointsGroupEventResultData> qryZjTournamentGroupResult(int tournamentId, int stage, int groupId, int entry, int page, int limit) {
        return this.tableQueryService.qryPageZjTournamentGroupResult(tournamentId, stage, groupId, entry, page, limit);
    }

    @Override
    public List<TournamentGroupFixtureData> qryGroupFixtureListById(int tournamentId) {
        return this.querySerivce.qryGroupFixtureListById(tournamentId);
    }

    @Override
    public List<TournamentKnockoutFixtureData> qryKnockoutFixtureListById(int tournamentId) {
        return this.querySerivce.qryKnockoutFixtureListById(tournamentId);
    }

    @Override
    public EntryInfoData qryEntryInfo(int entry) {
        return BeanUtil.copyProperties(this.querySerivce.qryEntryInfo(entry), EntryInfoData.class);
    }

    @Override
    public TableData<ZjTournamentResultData> qryZjTournamentResultById(int tournamentId) {
        return this.tableQueryService.qryZjTournamentResultById(tournamentId);
    }

    @Override
    public List<ZjTournamentCaptainData> qryZjTournamentCaptain(int tournamentId) {
        return this.querySerivce.qryZjTournamentCaptain(tournamentId);
    }

    @Override
    public String updateZjTournamentPhaseTwoGroupData(List<TournamentGroupData> groupDataList, int captainEntry) {
        return this.tournamentService.updateZjTournamentPhaseTwoGroupData(groupDataList, captainEntry);
    }

    @Override
    public String updateZjTournamentPkData(int tournamentId, int entry, int pkEntry, int currentGroupId, int captainEntry) {
        return this.tournamentService.updateZjTournamentPkData(tournamentId, entry, pkEntry, currentGroupId, captainEntry);
    }

}

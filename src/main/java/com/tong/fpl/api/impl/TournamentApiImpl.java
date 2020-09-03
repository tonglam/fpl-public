package com.tong.fpl.api.impl;

import cn.hutool.core.bean.BeanUtil;
import com.tong.fpl.api.ITournamentApi;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.table.TableData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.ITableQueryService;
import com.tong.fpl.service.ITournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
		return BeanUtil.copyProperties(this.querySerivce.qryTournamentInfoById(tournamentId), TournamentInfoData.class);
	}

	@Override
	public TableData<TournamentGroupData> qryGroupInfoListByGroupId(int tournamentId, int groupId) {
		return this.tableQueryService.qryGroupInfoListByGroupId(tournamentId, groupId);
	}

}

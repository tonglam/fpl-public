package com.tong.fpl.api.impl;

import com.tong.fpl.api.IStatApi;
import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.tournament.TournamentEventCaptainData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;
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
	 * @implNote captain
	 */
	@Override
	public TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param) {
		return this.tableQueryService.qryTournamenList(param);
	}

	@Override
	public TableData<TournamentEventCaptainData> qryLeagueCaptainDataList(int tournamentId) {
		return this.tableQueryService.qryLeagueCaptainDataList(tournamentId);
	}

	@Override
	public TableData<EntryEventCaptainData> qryLeagueEventCaptainDataList(int tournamentId, int event) {
		return this.tableQueryService.qryLeagueEventCaptainDataList(tournamentId, event);
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
	 * @apiNote common
	 */
	@Override
	public TournamentInfoData qryTournamentInfoById(int tournamentId) {
		return this.querySerivce.qryTournamentDataById(tournamentId);
	}

}

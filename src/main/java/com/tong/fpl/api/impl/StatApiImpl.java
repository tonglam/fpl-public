package com.tong.fpl.api.impl;

import com.tong.fpl.api.IStatApi;
import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.ITableQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
	public TableData<EntryInfoData> qryEntryInfoByTournament(String season, int tournamentId) {
		List<EntryInfoData> list = this.tableQueryService.qryEntryInfoByTournament(season, tournamentId).getData()
				.stream()
				.sorted(Comparator.comparing(EntryInfoData::getOverallRank))
				.collect(Collectors.toList());
		return new TableData<>(list);
	}

	@Override
	public TableData<EntryEventCaptainData> qryEntryCaptainList(String season, int entry) {
		return this.tableQueryService.qryEntryCaptainList(season, entry);
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

}

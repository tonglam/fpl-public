package com.tong.fpl.letletmeApi.impl;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.letletmeApi.IStatApi;
import com.tong.fpl.service.IQueryService;
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

	private final IQueryService queryService;
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
		return this.queryService.qryTeamSelectStatList();
	}

	@Override
	public TableData<LeagueStatData> qryTeamSelectStatByName(int event, String leagueName) {
		return this.tableQueryService.qryTeamSelectStatByName(event, leagueName);
	}

	/**
	 * @implNote common
	 */
	@Override
	public int getCurrentEvent() {
		return this.queryService.getCurrentEvent();
	}

}

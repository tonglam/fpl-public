package com.tong.fpl.letletmeApi.impl;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.letletmeApi.ILiveApi;
import com.tong.fpl.service.IQueryService;
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

	private final IQueryService queryService;
	private final ITableQueryService tableQueryService;

	/**
	 * @implNote entry
	 */
	@Override
	public TableData<LiveCalcData> qryEntryLivePoints(int entry) {
		return this.tableQueryService.qryEntryLivePoints(entry);
	}

	/**
	 * @implNote league
	 */
	@Override
	public TableData<LiveCalcData> qryTournamentLivePoints(int tournamentId) {
		return this.tableQueryService.qryTournamentLivePoints(tournamentId);
	}

	/**
	 * @implNote match
	 */
	@Override
	public List<LiveMatchData> qryLiveMatchList(int statusId) {
		return this.queryService.qryLiveMatchList(statusId);
	}

	@Override
	public TableData<LiveMatchTeamData> qryLiveTeamDataList(int statusId) {
		return this.tableQueryService.qryLiveTeamDataList(statusId);
	}

	/**
	 * @implNote common
	 */
	@Override
	public int getCurrentEvent() {
		return this.queryService.getCurrentEvent();
	}

}

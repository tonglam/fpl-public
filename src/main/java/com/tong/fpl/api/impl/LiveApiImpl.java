package com.tong.fpl.api.impl;

import com.tong.fpl.api.ILiveApi;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
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

	private final IQuerySerivce querySerivce;
	private final ITableQueryService tableQueryService;

	/**
	 * @implNote entry
	 */
	@Override
	public TableData<LiveCalaData> qryEntryLivePoints(int entry) {
		return this.tableQueryService.qryEntryLivePoints(entry);
	}

	/**
	 * @implNote league
	 */
	@Override
	public TableData<LiveCalaData> qryTournamentLivePoints(int tournamentId) {
		return this.tableQueryService.qryTournamentLivePoints(tournamentId);
	}

	/**
	 * @implNote match
	 */
	@Override
	public List<LiveMatchData> qryLiveMatchList(int statusId) {
		return this.querySerivce.qryLiveMatchList(statusId);
	}

	@Override
	public TableData<LiveMatchTeamData> qryLiveTeamDataList(int statusId) {
		return this.tableQueryService.qryLiveTeamDataList(statusId);
	}

}

package com.tong.fpl.api.impl;

import cn.hutool.core.bean.BeanUtil;
import com.tong.fpl.api.IMyFplApi;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.ITableQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/8/15
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyFplApiImpl implements IMyFplApi {

	private final IQuerySerivce querySerivce;
	private final ITableQueryService tableQueryService;

	/**
	 * @implNote entry
	 */
	@Override
	public EntryInfoData qryEntryInfo(int entry) {
		return BeanUtil.copyProperties(this.querySerivce.qryEntryInfo(entry), EntryInfoData.class);
	}

	@Override
	public TableData<EntryEventResultData> qryEntryResultList(int entry) {
		return this.tableQueryService.qryEntryResultList(entry);
	}

	@Override
	public TableData<EntryPickData> qryEntryEventResult(int event, int entry) {
		return this.tableQueryService.qryEntryEventResult(event, entry);
	}

	/**
     * @implNote pick
     */
    @Override
    public TableData<PlayerInfoData> qryPlayerDataList(int page, int limit) {
        return this.tableQueryService.qryPagePlayerDataList(page, limit);
    }

	/**
	 * @implNote league
	 */
	@Override
	public TableData<TournamentInfoData> qryTournamenList(TournamentQueryParam param) {
		return this.tableQueryService.qryTournamentList(param);
	}

	@Override
	public TableData<LeagueEventReportData> qryEntryEventReportList(int leagueId, String leagueType, int entry) {
		return this.tableQueryService.qryEntryEventReportList(leagueId, leagueType, entry);
	}

	@Override
	public String qryLeagueNameByIdAndType(int leagueId, String leagueType) {
		return this.querySerivce.qryLeagueNameByIdAndType(leagueId, leagueType);
	}

	@Override
	public TableData<LeagueEventReportStatData> qryLeagueCaptainReportStat(int leagueId, String leagueType) {
		return this.tableQueryService.qryLeagueCaptainReportStat(leagueId, leagueType);
	}

	@Override
	public TableData<LeagueEventReportData> qryLeagueCaptainEventReportList(int leagueId, String leagueType, int event) {
		return this.tableQueryService.qryLeagueCaptainEventReportList(leagueId, leagueType, event);
	}

	@Override
	public TableData<LeagueEventReportStatData> qryLeagueTransferReportStat(int leagueId, String leagueType) {
		return this.tableQueryService.qryLeagueTransferReportStat(leagueId, leagueType);
	}

	@Override
	public TableData<LeagueEventReportData> qryLeagueTransferEventReportList(int leagueId, String leagueType, int event) {
		return this.tableQueryService.qryLeagueTransferEventReportList(leagueId, leagueType, event);
	}

}

package com.tong.fpl.service;

import com.tong.fpl.domain.letletme.league.LeagueStatData;

/**
 * Create by tong on 2020/9/2
 */
public interface IReportService {

	void insertEntryCaptainStat(int tournamentId);

	void insertLeagueResultStat(int event, String leagueType, int leagueId, int limit);

	void inertTeamSelectStat(int event, String leagueType, int leagueId, int limit);

	LeagueStatData getLeagueStatData(String leagueName, int event);

}

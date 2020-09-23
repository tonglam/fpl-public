package com.tong.fpl.service;

/**
 * Create by tong on 2020/9/2
 */
public interface IReportService {

	void insertLeagueResultStat(int event, String leagueType, int leagueId, int limit);

	void inertTeamSelectStat(int event, String leagueType, int leagueId, int limit);

}

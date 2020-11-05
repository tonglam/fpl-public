package com.tong.fpl.service;

/**
 * Create by tong on 2020/9/2
 */
public interface IReportService {

	void insertLeagueEventSelectStat(int event, int leagueId, String leagueType, int limit);

	void updateLeagueEventResultStat(int event, int leagueId, String leagueType);

}

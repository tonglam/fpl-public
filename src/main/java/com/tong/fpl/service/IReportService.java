package com.tong.fpl.service;

/**
 * Create by tong on 2020/9/2
 */
public interface IReportService {

	void insertLeagueEventSelect(int event, int leagueId, String leagueType, int limit);

	void updateLeagueEventResult(int event, int leagueId, String leagueType);

}

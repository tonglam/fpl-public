package com.tong.fpl.service;

import java.util.Map;

/**
 * Create by tong on 2020/9/2
 */
public interface IReportService {

	void insertEntryCaptainStat(int tournamentId);

	void insertLeagueResultStat(int event, String leagueType, int leagueId, int limit);

	void inertTeamSelectStat(int event, String leagueType, int leagueId, int limit);

	Map<String, String> getTopSelectedMap(String leagueName, int event, boolean budge);

}

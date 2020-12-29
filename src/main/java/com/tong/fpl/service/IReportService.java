package com.tong.fpl.service;

import java.util.Map;

/**
 * Create by tong on 2020/9/2
 */
public interface IReportService {

	boolean entryEvenLeagueEventExists(int event, int leagueId, String leagueType, int entry);

	boolean evenLeagueEventExists(int event, int leagueId, String leagueType);

	void insertEntryLeagueEventSelect(int event, int leagueId, String leagueType, int entry);

	void insertLeagueEventSelect(int event, int leagueId, String leagueType, int limit);

	void updateEntryLeagueEventResult(int event, int leagueId, String leagueType, int entry);

	void updateLeagueEventResult(int event, int leagueId, String leagueType);

	Map<String, Object> calcEventStat(int event, int leagueId, String leagueType, int topNum);

}

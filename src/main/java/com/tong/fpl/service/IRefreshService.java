package com.tong.fpl.service;

/**
 * Create by tong on 2020/6/29
 */
public interface IRefreshService {

    void refreshEventLive(int event);

    void refreshEventLiveCache(int event);

    void refreshPlayerValue();

    void refreshPlayerStat();

    void refreshEventOverall(int event);

    void refreshEntryInfo(int entry);

    void refreshEntryEventTransfers(int event, int entry);

    void refreshEntryEventResult(int event, int entry);

    void refreshCurrentEventScoutResult(int entry);

    void refreshTournamentEventResult(int event, int tournamentId);

    void refreshEntrySummary(int event, int entry);

    void refreshLeagueSummary(int event, String leagueName, int entry);

    void refreshLeagueSelect(int event, String leagueName);

    void refreshPlayerSummary(String season, int code);

    void refreshTeamSummary(String season, String name);

    void refreshEventOverallSummary(int event);

    void refreshEventSourceScoutResult(int event);

}

package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.summary.entry.*;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonCaptainData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonInfoData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonScoreData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonSummaryData;

/**
 * Create by tong on 2021/5/25
 */
public interface IApiSummary {

    /**
     * @apiNote entry
     */
    EntrySeasonInfoData qryEntrySeasonInfo(int entry);

    EntrySeasonSummaryData qryEntrySeasonSummary(int entry);

    EntrySeasonCaptainData qryEntrySeasonCaptain(int entry);

    EntrySeasonTransfersData qryEntrySeasonTransfers(int entry);

    EntrySeasonScoreData qryEntrySeasonScore(int entry);

    void refreshEntryEventSummary(int event, int entry);

    /**
     * @apiNote league
     */
    LeagueSeasonInfoData qryLeagueSeasonInfo(String leagueName);

    LeagueSeasonSummaryData qryLeagueSeasonSummary(String leagueName, int entry);

    LeagueSeasonCaptainData qryLeagueSeasonCaptain(String leagueName, int entry);

    LeagueSeasonScoreData qryLeagueSeasonScore(String leagueName, int entry);

    void refreshLeagueSummary(int event, String leagueName, int entry);

}

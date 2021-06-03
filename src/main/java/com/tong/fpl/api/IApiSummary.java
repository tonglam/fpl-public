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

    /**
     * @apiNote league
     */
    LeagueSeasonInfoData qryLeagueSeasonInfo(int leagueId, String leagueType);

    LeagueSeasonSummaryData qryLeagueSeasonSummary(int leagueId, String leagueType, int entry);

    LeagueSeasonCaptainData qryLeagueSeasonCaptain(int leagueId, String leagueType, int entry);

    LeagueSeasonScoreData qryLeagueSeasonScore(int leagueId, String leagueType, int entry);

}

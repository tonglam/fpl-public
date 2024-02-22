package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.element.ElementEventData;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.global.MapData;
import com.tong.fpl.domain.letletme.scout.PopularScoutData;
import com.tong.fpl.domain.letletme.summary.entry.*;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonCaptainData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonInfoData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonScoreData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonSummaryData;

import java.util.List;
import java.util.Map;

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

    void refreshEntrySummary(int event, int entry);

    /**
     * @apiNote league
     */
    LeagueSeasonInfoData qryLeagueSeasonInfo(String leagueName);

    LeagueSeasonSummaryData qryLeagueSeasonSummary(String leagueName, int entry);

    LeagueSeasonCaptainData qryLeagueSeasonCaptain(String leagueName, int entry);

    LeagueSeasonScoreData qryLeagueSeasonScore(String leagueName, int entry);

    void refreshLeagueSummary(int event, String leagueName, int entry);

    /**
     * @apiNote overall
     */

    EventOverallResultData qryEventOverallResult(int event);

    List<ElementEventData> qryEventDreamTeam(int event);

    List<ElementEventData> qryEventEliteElements(int event);

    Map<String, List<ElementEventData>> qryEventOverallTransfers(int event);

    void refreshEventOverallSummary(int event);

    /**
     * @apiNote scout
     */
    void insertEventSourceScout(int event, String source, List<MapData<Integer>> scoutDataList);

    PopularScoutData qryEventSourceScoutResult(int event, String source);

    List<PopularScoutData> qryOverallEventScoutResult(int event);

    void refreshEventSourceScoutResult(int event);

}

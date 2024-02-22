package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiSummary;
import com.tong.fpl.domain.letletme.element.ElementEventData;
import com.tong.fpl.domain.letletme.event.EventOverallResultData;
import com.tong.fpl.domain.letletme.scout.InsertEventSourceScoutParam;
import com.tong.fpl.domain.letletme.scout.PopularScoutData;
import com.tong.fpl.domain.letletme.summary.entry.*;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonCaptainData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonInfoData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonScoreData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonSummaryData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/25
 */
@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SummaryApiController {

    private final IApiSummary apiSummary;

    /**
     * @implNote entry
     */
    @GetMapping("/qryEntrySeasonInfo")
    public EntrySeasonInfoData qryEntrySeasonInfo(@RequestParam int entry) {
        return this.apiSummary.qryEntrySeasonInfo(entry);
    }

    @GetMapping("/qryEntrySeasonSummary")
    public EntrySeasonSummaryData qryEntrySeasonSummary(@RequestParam int entry) {
        return this.apiSummary.qryEntrySeasonSummary(entry);
    }

    @GetMapping("/qryEntrySeasonCaptain")
    public EntrySeasonCaptainData qryEntrySeasonCaptain(@RequestParam int entry) {
        return this.apiSummary.qryEntrySeasonCaptain(entry);
    }

    @GetMapping("/qryEntrySeasonTransfers")
    public EntrySeasonTransfersData qryEntrySeasonTransfers(@RequestParam int entry) {
        return this.apiSummary.qryEntrySeasonTransfers(entry);
    }

    @GetMapping("/qryEntrySeasonScore")
    public EntrySeasonScoreData qryEntrySeasonScore(@RequestParam int entry) {
        return this.apiSummary.qryEntrySeasonScore(entry);
    }

    @GetMapping("/refreshEntrySummary")
    public void refreshEntrySummary(@RequestParam int event, @RequestParam int entry) {
        this.apiSummary.refreshEntrySummary(event, entry);
    }

    /**
     * @implNote league
     */
    @GetMapping("/qryLeagueSeasonInfo")
    public LeagueSeasonInfoData qryLeagueSeasonInfo(@RequestParam String leagueName) {
        return this.apiSummary.qryLeagueSeasonInfo(this.getClassicLeagueName(leagueName));
    }

    @GetMapping("/qryLeagueSeasonSummary")
    public LeagueSeasonSummaryData qryLeagueSeasonSummary(@RequestParam String leagueName, @RequestParam int entry) {
        return this.apiSummary.qryLeagueSeasonSummary(this.getClassicLeagueName(leagueName), entry);
    }

    @GetMapping("/qryLeagueSeasonCaptain")
    public LeagueSeasonCaptainData qryLeagueSeasonCaptain(@RequestParam String leagueName, @RequestParam int entry) {
        return this.apiSummary.qryLeagueSeasonCaptain(this.getClassicLeagueName(leagueName), entry);
    }

    @GetMapping("/qryLeagueSeasonScore")
    public LeagueSeasonScoreData qryLeagueSeasonScore(@RequestParam String leagueName, @RequestParam int entry) {
        return this.apiSummary.qryLeagueSeasonScore(this.getClassicLeagueName(leagueName), entry);
    }

    @GetMapping("/refreshLeagueSummary")
    public void refreshLeagueSummary(@RequestParam int event, @RequestParam String leagueName, @RequestParam int entry) {
        this.apiSummary.refreshLeagueSummary(event, this.getClassicLeagueName(leagueName), entry);
    }

    /**
     * @implNote overall
     */
    @GetMapping("/qryEventOverallResult")
    public EventOverallResultData qryEventOverallResult(@RequestParam int event) {
        return this.apiSummary.qryEventOverallResult(event);
    }

    @GetMapping("/qryEventDreamTeam")
    public List<ElementEventData> qryEventDreamTeam(@RequestParam int event) {
        return this.apiSummary.qryEventDreamTeam(event);
    }

    @GetMapping("/qryEventEliteElements")
    public List<ElementEventData> qryEventEliteElements(@RequestParam int event) {
        return this.apiSummary.qryEventEliteElements(event);
    }

    @GetMapping("/qryEventOverallTransfers")
    public Map<String, List<ElementEventData>> qryEventOverallTransfers(@RequestParam int event) {
        return this.apiSummary.qryEventOverallTransfers(event);
    }

    @GetMapping("/refreshEventOverallSummary")
    public void refreshEventOverallSummary(@RequestParam int event) {
        this.apiSummary.refreshEventOverallSummary(event);
    }

    @PostMapping("/insertEventSourceScout")
    public void insertEventSourceScout(@RequestBody InsertEventSourceScoutParam insertEventSourceScoutParam) {
        if (insertEventSourceScoutParam.getEvent() <= 0 || StringUtils.isEmpty(insertEventSourceScoutParam.getSource()) || CollectionUtils.isEmpty(insertEventSourceScoutParam.getScoutDataList())) {
            return;
        }
        this.apiSummary.insertEventSourceScout(insertEventSourceScoutParam.getEvent(), insertEventSourceScoutParam.getSource(), insertEventSourceScoutParam.getScoutDataList());
    }

    @GetMapping("/qryEventSourceScoutResult")
    public PopularScoutData qryEventSourceScoutResult(@RequestParam int event, @RequestParam String source) {
        return this.apiSummary.qryEventSourceScoutResult(event, source);
    }

    @GetMapping("/qryOverallEventScoutResult")
    public List<PopularScoutData> qryOverallEventScoutResult(@RequestParam int event) {
        return this.apiSummary.qryOverallEventScoutResult(event);
    }

    @GetMapping("/refreshEventSourceScoutResult")
    public void refreshEventSourceScoutResult(@RequestParam int event) {
        this.apiSummary.refreshEventSourceScoutResult(event);
    }

    private String getClassicLeagueName(String leagueName) {
        if (leagueName.contains("_Classic")) {
            return StringUtils.substringBefore(leagueName, "_Classic");
        }
        return leagueName;
    }

}

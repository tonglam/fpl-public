package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiSummary;
import com.tong.fpl.domain.letletme.summary.entry.*;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonCaptainData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonInfoData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonScoreData;
import com.tong.fpl.domain.letletme.summary.league.LeagueSeasonSummaryData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * @implNote league
     */
    @GetMapping("/qryLeagueSeasonInfo")
    public LeagueSeasonInfoData qryLeagueSeasonInfo(@RequestParam int leagueId, @RequestParam String leagueType) {
        return this.apiSummary.qryLeagueSeasonInfo(leagueId, leagueType);
    }

    @GetMapping("/qryLeagueSeasonSummary")
    public LeagueSeasonSummaryData qryLeagueSeasonSummary(@RequestParam int leagueId, @RequestParam String leagueType, @RequestParam int entry) {
        return this.apiSummary.qryLeagueSeasonSummary(leagueId, leagueType, entry);
    }

    @GetMapping("/qryLeagueSeasonCaptain")
    public LeagueSeasonCaptainData qryLeagueSeasonCaptain(@RequestParam int leagueId, @RequestParam String leagueType, @RequestParam int entry) {
        return this.apiSummary.qryLeagueSeasonCaptain(leagueId, leagueType, entry);
    }

    @GetMapping("/qryLeagueSeasonScore")
    public LeagueSeasonScoreData qryLeagueSeasonScore(@RequestParam int leagueId, @RequestParam String leagueType, @RequestParam int entry) {
        return this.apiSummary.qryLeagueSeasonScore(leagueId, leagueType, entry);
    }

}

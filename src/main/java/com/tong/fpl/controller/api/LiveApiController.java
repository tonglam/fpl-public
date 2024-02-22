package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiLive;
import com.tong.fpl.domain.letletme.live.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Create by tong on 2021/5/9
 */
@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveApiController {

    private final IApiLive apiLive;

    @RequestMapping("/calcLivePointsByElement")
    @ResponseBody
    public LiveCalcElementData calcLivePointsByElement(@RequestParam int event, @RequestParam int element) {
        return this.apiLive.calcLivePointsByElement(event, element);
    }

    @RequestMapping("/calcLivePointsByEntry")
    @ResponseBody
    public LiveCalcData calcLivePointsByEntry(@RequestParam int event, @RequestParam int entry) {
        return this.apiLive.calcLivePointsByEntry(event, entry);
    }

    @RequestMapping("/calcLivePointsByEntries")
    @ResponseBody
    public List<LiveCalcData> calcLivePointsByEntries(@RequestParam int event, @RequestParam String entries) {
        return this.apiLive.calcLivePointsByEntries(event, entries);
    }

    @RequestMapping("/calcLivePointsByElementList")
    @ResponseBody
    public LiveCalcData calcLivePointsByElementList(@RequestBody LiveCalcParamData liveCalcParamData) {
        return this.apiLive.calcLivePointsByElementList(liveCalcParamData);
    }

    @RequestMapping("/calcLivePointsByTournament")
    @ResponseBody
    public LiveTournamentCalcData calcLivePointsByTournament(@RequestParam int event, @RequestParam int tournamentId) {
        return this.apiLive.calcLivePointsByTournament(event, tournamentId);
    }

    @RequestMapping("/calcSearchLivePointsByTournament")
    @ResponseBody
    public SearchLiveTournamentCalcData calcSearchLivePointsByTournament(@RequestBody LiveCalcSearchParamData liveCalcSearchParamData) {
        return this.apiLive.calcSearchLivePointsByTournament(liveCalcSearchParamData);
    }

    @RequestMapping("/calcLivePointsByKnockout")
    @ResponseBody
    public List<LiveKnockoutResultData> calcLivePointsByKnockout(@RequestParam int event, @RequestParam int tournamentId) {
        return this.apiLive.calcLivePointsByKnockout(event, tournamentId);
    }

    @RequestMapping("/calcLiveGwPointsByChampionLeagueGroup")
    @ResponseBody
    public List<LiveCalcData> calcLiveGwPointsByChampionLeagueGroup(@RequestParam int event, @RequestParam int tournamentId, @RequestParam String stage) {
        return this.apiLive.calcLiveGwPointsByChampionLeague(event, tournamentId, stage);
    }

    @RequestMapping("/calcLiveTotalPointsByChampionLeagueGroup")
    @ResponseBody
    public List<LiveCalcData> calcLiveTotalPointsByChampionLeagueGroup(@RequestParam int event, @RequestParam int tournamentId, @RequestParam String stage) {
        return this.apiLive.calcLiveTotalPointsByChampionLeague(event, tournamentId, stage);
    }

    @GetMapping("/qryLiveMatchByStatus")
    public List<LiveMatchData> qryLiveMatchByStatus(@RequestParam String playStatus) {
        return this.apiLive.qryLiveMatchByStatus(playStatus);
    }

}

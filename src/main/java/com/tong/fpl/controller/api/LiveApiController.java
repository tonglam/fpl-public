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

    @RequestMapping("/calcLivePointsByElementList")
    @ResponseBody
    public LiveCalcData calcLivePointsByElementList(@RequestBody LiveCalcParamData liveCalcParamData) {
        return this.apiLive.calcLivePointsByElementList(liveCalcParamData);
    }

    @RequestMapping("/calcLivePointsByTournament")
    @ResponseBody
    public List<LiveCalcData> calcLivePointsByTournament(@RequestParam int event, @RequestParam int tournamentId) {
        return this.apiLive.calcLivePointsByTournament(event, tournamentId);
    }

    @RequestMapping("/calcSearchLivePointsByTournament")
    @ResponseBody
    public SearchLiveCalcData calcSearchLivePointsByTournament(@RequestParam int event, @RequestParam int tournamentId, @RequestParam int element) {
        return this.apiLive.calcSearchLivePointsByTournament(event, tournamentId, element);
    }

    @GetMapping("/qryLiveMatchByStatus")
    public List<LiveMatchData> qryLiveMatchByStatus(@RequestParam String playStatus) {
        return this.apiLive.qryLiveMatchByStatus(playStatus);
    }

}

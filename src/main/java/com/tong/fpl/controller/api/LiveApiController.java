package com.tong.fpl.controller.api;

import com.google.common.collect.Lists;
import com.tong.fpl.api.IApiLive;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.live.SearchLiveCalcData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

    @RequestMapping("/calcLivePointsByEntry")
    @ResponseBody
    public LiveCalcData calcLivePointsByEntry(@RequestParam int event, @RequestParam int entry) {
        if (event <= 0 || entry <= 0) {
            return new LiveCalcData();
        }
        return this.apiLive.calcLivePointsByEntry(event, entry);
    }

    @RequestMapping("/calcLivePointsByTournament")
    @ResponseBody
    public List<LiveCalcData> calcLivePointsByTournament(@RequestParam int event, @RequestParam int tournamentId) {
        if (event <= 0 || tournamentId <= 0) {
            return Lists.newArrayList();
        }
        return this.apiLive.calcLivePointsByTournament(event, tournamentId);
    }

    @RequestMapping("/calcSearchLivePointsByTournament")
    @ResponseBody
    public SearchLiveCalcData calcSearchLivePointsByTournament(@RequestParam int event, @RequestParam int tournamentId, @RequestParam int element) {
        if (event <= 0 || tournamentId <= 0 || element <= 0) {
            return new SearchLiveCalcData();
        }
        return this.apiLive.calcSearchLivePointsByTournament(event, tournamentId, element);
    }

    @GetMapping("/qryLiveMatchByStatus")
    public List<LiveMatchData> qryLiveMatchByStatus(@RequestParam String playStatus) {
        if (StringUtils.isEmpty(playStatus)) {
            return Lists.newArrayList();
        }
        return this.apiLive.qryLiveMatchByStatus(playStatus);
    }

}

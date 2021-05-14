package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiLive;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
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

    @RequestMapping("/calcLivePointsByEntry")
    @ResponseBody
    public LiveCalcData calcLivePointsByEntry(@RequestParam int event, @RequestParam int entry) {
        if (entry <= 0) {
            return new LiveCalcData();
        }
        return this.apiLive.calcLivePointsByEntry(event, entry);
    }

    @RequestMapping("/calcLivePointsByTournament")
    @ResponseBody
    public List<LiveCalcData> calcLivePointsByTournament(@RequestParam int event, @RequestParam int tournamentId) {
        return this.apiLive.calcLivePointsByTournament(event, tournamentId);
    }

    @GetMapping("/qryLiveMatchByStatus")
    public List<LiveMatchData> qryLiveMatchByStatus(@RequestParam String playStatus) {
        return this.apiLive.qryLiveMatchByStatus(playStatus);
    }

}

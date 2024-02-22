package com.tong.fpl.controller;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.LiveCalcData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
import com.tong.fpl.letletmeApi.ILiveApi;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Create by tong on 2020/6/23
 */
@Controller
@RequestMapping(value = "/live")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveController {

    private final ILiveApi liveApi;

    @GetMapping(value = "/entry")
    public String entryController() {
        return "live/entry";
    }

    @GetMapping(value = "/entry/liveEntry")
    public String liveEntryController(@RequestParam int entry, Model model) {
        model.addAttribute("qryEntry", entry);
        return "forward:/live/entry";
    }

    @GetMapping(value = "/league")
    public String leagueController(Model model) {
        model.addAttribute("currentGw", this.liveApi.getCurrentEvent());
        return "live/league";
    }

    @GetMapping(value = "/match")
    public String matchController(Model model, HttpSession session) {
        model.addAttribute("matchList", this.liveApi.qryLiveMatchList(0));
        int mode = 0;
        if (session.getAttribute("liveMatchMode") != null) {
            mode = Integer.parseInt(session.getAttribute("liveMatchMode").toString());
        }
        model.addAttribute("checkMode", mode);
        return "live/match";
    }

    @GetMapping(value = "/match/reload")
    public String matchController(@RequestParam int statusId, Model model) {
        model.addAttribute("matchList", this.liveApi.qryLiveMatchList(statusId));
        if (statusId == 0) {
            return "live/match::playingContent";
        }
        return "live/match::finishedContent";
    }

    /**
     * @apiNote entry
     */
    @RequestMapping("/qryEntryLivePoints")
    @ResponseBody
    public TableData<LiveCalcData> qryEntryLivePoints(@RequestParam int entry) {
        if (entry <= 0) {
            return new TableData<>();
        }
        return this.liveApi.qryEntryLivePoints(entry);
    }

    /**
     * @apiNote league
     */
    @RequestMapping("/qryTournamentLivePoints")
    @ResponseBody
    public TableData<LiveCalcData> qryTournamentLivePoints(@RequestParam int tournamentId) {
        return this.liveApi.qryTournamentLivePoints(tournamentId);
    }

    /**
     * @apiNote match
     */
    @RequestMapping("/qryLiveTeamDataList")
    @ResponseBody
    public TableData<LiveMatchTeamData> qryLiveTeamDataList(@RequestParam int statusId) {
        return this.liveApi.qryLiveTeamDataList(statusId);
    }

}

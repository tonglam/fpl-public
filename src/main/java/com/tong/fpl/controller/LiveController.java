package com.tong.fpl.controller;

import com.tong.fpl.api.ILiveApi;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Create by tong on 2020/6/23
 */
@Controller
@RequestMapping(value = "/live")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveController {

    private final ILiveApi liveApi;

    @RequestMapping(value = "/entry")
    public String entryController() {
        return "live/entry";
    }

    @RequestMapping(value = "/match")
    public String matchController() {
        return "live/match";
    }

    @RequestMapping(value = "/league")
    public String leagueController() {
        return "live/league";
    }

    @RequestMapping(value = "/saveLiveEntry")
    @ResponseBody
    public void saveLiveEntry(@RequestParam int liveEntry, HttpSession session) {
        session.setAttribute("liveEntry", liveEntry);
    }

    @GetMapping("/qryEntryLivePoints")
    @ResponseBody
    public TableData<LiveCalaData> qryEntryLivePoints(HttpSession session) {
        int entry = this.getLiveEntry(session);
        if (entry == 0) {
            return new TableData<>(new LiveCalaData());
        }
        return this.liveApi.qryEntryLivePoints(entry);
    }

    private int getLiveEntry(HttpSession session) {
        if (session.getAttribute("liveEntry") != null) {
            return (int) session.getAttribute("liveEntry");
        } else if (session.getAttribute("entry") != null) {
            return (int) session.getAttribute("entry");
        }
        return 0;
    }

    @GetMapping("/qryTournamentLivePoints")
    @ResponseBody
    public TableData<LiveCalaData> qryTournamentLivePoints(@RequestParam int tournamentId) {
        return this.liveApi.qryTournamentLivePoints(tournamentId);
    }

}

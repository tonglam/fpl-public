package com.tong.fpl.controller;

import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;
import com.tong.fpl.letletmeApi.IMyFplApi;
import com.tong.fpl.utils.CommonUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Create by tong on 2020/6/23
 */
@Controller
@RequestMapping(value = "/my_fpl")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyFplController {

    private final IMyFplApi myFplApi;

    @GetMapping(value = "/pick")
    public String pickController() {
        return "myFpl/pick";
    }

    @GetMapping(value = "/entry")
    public String entryController() {
        return "myFpl/entry";
    }

    @GetMapping(value = "/league")
    public String leagueController(Model model) {
        model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
        model.addAttribute("seasonMap", CommonUtils.createSeasonMapForOption());
        return "myFpl/league";
    }

    @GetMapping(value = "/leagueCaptainReport")
    public String leagueCaptainReportController(@RequestParam int leagueId, @RequestParam String leagueType, Model model) {
        model.addAttribute("leagueName", this.myFplApi.qryLeagueNameByIdAndType(leagueId, leagueType));
        model.addAttribute("gwMap", CommonUtils.createCurrentGwMapForOption(this.myFplApi.getCurrentEvent()));
        return "myFpl/leagueCaptainReport";
    }

    @GetMapping(value = "/leagueTransfersReport")
    public String leagueTransferReportController(@RequestParam int leagueId, @RequestParam String leagueType, Model model) {
        model.addAttribute("leagueName", this.myFplApi.qryLeagueNameByIdAndType(leagueId, leagueType));
        model.addAttribute("gwMap", CommonUtils.createCurrentGwMapForOption(this.myFplApi.getCurrentEvent()));
        return "myFpl/leagueTransfersReport";
    }

    @GetMapping(value = "/leagueScoringReport")
    public String leagueScoringReport(@RequestParam int leagueId, @RequestParam String leagueType, Model model) {
        model.addAttribute("leagueName", this.myFplApi.qryLeagueNameByIdAndType(leagueId, leagueType));
        model.addAttribute("gwMap", CommonUtils.createCurrentGwMapForOption(this.myFplApi.getCurrentEvent()));
        return "myFpl/leagueScoringReport";
    }

    /**
     * @apiNote entry
     */
    @RequestMapping("/qryEntryResultList")
    @ResponseBody
    public TableData<EntryEventResultData> qryEntryResultList() {
        return new TableData<>();
    }

    @RequestMapping("/qryEntryEventResult")
    @ResponseBody
    public TableData<EntryPickData> qryEntryEventResult() {
        return new TableData<>();
    }

    /**
     * @apiNote pick
     */
    @RequestMapping("/qryPlayerDataList")
    @ResponseBody
    public TableData<PlayerInfoData> qryPlayerDataList(@RequestParam int page, @RequestParam int limit) {
        return this.myFplApi.qryPlayerDataList(page, limit);
    }

    /**
     * @apiNote league
     */
    @ResponseBody
    @RequestMapping(value = "/qryTournamentList")
    public TableData<TournamentInfoData> qryTournamentList(@RequestBody TournamentQueryParam param, HttpSession session) {
        int entry;
        if (session.getAttribute("entry") != null) {
            entry = Integer.parseInt(session.getAttribute("entry").toString());
            param.setEntry(entry);
        }
        return this.myFplApi.qryTournamentList(param);
    }

    /**
     * @apiNote leagueCaptain
     */
    @RequestMapping("/qryLeagueCaptainReportStat")
    @ResponseBody
    public TableData<LeagueEventReportStatData> qryLeagueCaptainReportStat(@RequestParam int leagueId, @RequestParam String leagueType) {
        return this.myFplApi.qryLeagueCaptainReportStat(leagueId, leagueType);
    }

    @RequestMapping("/qryLeagueCaptainEventReportList")
    @ResponseBody
    public TableData<LeagueEventReportData> qryLeagueCaptainEventReportList(@RequestParam int event, @RequestParam int leagueId, @RequestParam String leagueType) {
        return this.myFplApi.qryLeagueCaptainEventReportList(event, leagueId, leagueType);
    }

    @RequestMapping("/qryEntryCaptainEventReportList")
    @ResponseBody
    public TableData<LeagueEventReportData> qryEntryCaptainEventReportList(@RequestParam int leagueId, @RequestParam String leagueType, @RequestParam int entry) {
        return this.myFplApi.qryEntryCaptainEventReportList(leagueId, leagueType, entry);
    }

    /**
     * @apiNote leagueTransfers
     */
    @RequestMapping("/qryLeagueTransfersReportStat")
    @ResponseBody
    public TableData<LeagueEventReportStatData> qryLeagueTransfersReportStat(@RequestParam int leagueId, @RequestParam String leagueType) {
        return this.myFplApi.qryLeagueTransfersReportStat(leagueId, leagueType);
    }

    @RequestMapping("/qryLeagueTransfersEventReportList")
    @ResponseBody
    public TableData<LeagueEventReportData> qryLeagueTransfersEventReportList(@RequestParam int event, @RequestParam int leagueId, @RequestParam String leagueType) {
        return this.myFplApi.qryLeagueTransfersEventReportList(event, leagueId, leagueType);
    }

    @RequestMapping("/qryEntryTransfersEventReportList")
    @ResponseBody
    public TableData<LeagueEventReportData> qryEntryTransfersEventReportList(@RequestParam int leagueId, @RequestParam String leagueType, @RequestParam int entry) {
        return this.myFplApi.qryEntryTransfersEventReportList(leagueId, leagueType, entry);
    }

    /**
     * @apiNote leagueScoring
     */
    @RequestMapping("/qryLeagueScoringReportStat")
    @ResponseBody
    public TableData<LeagueEventReportStatData> qryLeagueScoringReportStat(@RequestParam int leagueId, @RequestParam String leagueType) {
        return this.myFplApi.qryLeagueScoringReportStat(leagueId, leagueType);
    }

    @RequestMapping("/qryLeagueScoringEventReportList")
    @ResponseBody
    public TableData<LeagueEventReportData> qryLeagueScoringEventReportList(@RequestParam int event, @RequestParam int leagueId, @RequestParam String leagueType) {
        return this.myFplApi.qryLeagueScoringEventReportList(event, leagueId, leagueType);
    }

    @RequestMapping("/qryEntryScoringEventReportList")
    @ResponseBody
    public TableData<LeagueEventReportData> qryEntryScoringEventReportList(@RequestParam int leagueId, @RequestParam String leagueType, @RequestParam int entry) {
        return this.myFplApi.qryEntryScoringEventReportList(leagueId, leagueType, entry);
    }

}

package com.tong.fpl.controller;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.api.IStatApi;
import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Create by tong on 2020/8/15
 */
@Controller
@RequestMapping(value = "/stat")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatController {

    private final IStatApi statApi;
    private final IHttpApi httpApi;

    @RequestMapping(value = "/price")
    public String priceController() {
        return "stat/price";
    }

    @RequestMapping(value = "/compare")
    public String compareController() {
        return "stat/compare";
    }

    @RequestMapping(value = "/captain")
    public String captainController(Model model) {
        model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
        model.addAttribute("tournamentName", "赛事：让让我吧");
        return "stat/captain";
    }

    @RequestMapping(value = "/selected")
    public String selectedController(Model model) {
        List<String> leagueList = this.statApi.qryTeamSelectStatList();
        if (!CollectionUtils.isEmpty(leagueList)) {
            model.addAttribute("leagueList", leagueList);
        }
        model.addAttribute("currentGw", this.httpApi.getCurrentEvent());
        model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
        return "stat/selected";
    }

    @GetMapping("/qryEntryInfoByTournament")
    @ResponseBody
    public TableData<EntryInfoData> qryEntryInfoByTournament(@RequestParam String season, @RequestParam int tournamentId) {
        return this.statApi.qryEntryInfoByTournament(season, tournamentId);
    }

    @GetMapping("/qryEntryCaptainList")
    @ResponseBody
    public TableData<EntryEventCaptainData> qryEntryCaptainList(@RequestParam String season, @RequestParam int entry) {
        return this.statApi.qryEntryCaptainList(season, entry);
    }

    @RequestMapping("/qryPlayerList")
    @ResponseBody
    public TableData<PlayerInfoData> qryPlayerList(@RequestParam String season) {
        return this.statApi.qryPlayerList(season);
    }

    @RequestMapping("/qryTeamSelectStatByName")
    @ResponseBody
    public TableData<LeagueStatData> qryTeamSelectStatByName(@RequestParam String leagueName, @RequestParam int event) {
        return this.statApi.qryTeamSelectStatByName(leagueName, event);
    }

}

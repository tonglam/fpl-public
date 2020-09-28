package com.tong.fpl.controller;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.api.ITournamentApi;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * Create by tong on 2020/6/23
 */
@Validated
@Controller
@RequestMapping(value = "/tournament")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentController {

    private final ITournamentApi tournamentApi;
    private final IHttpApi httpApi;

    @GetMapping(value = "/create")
    public String createController(Model model) {
        model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
        model.addAttribute("zjGroupNum", 4);
        model.addAttribute("zjTeamPerGroup", 4);
        return "tournament/create";
    }

    @GetMapping(value = "/result")
    public String resultController(Model model, HttpSession session) {
        if (session.getAttribute("entry") != null) {
            int entry = (int) session.getAttribute("entry");
            model.addAttribute("entryInfo", this.tournamentApi.qryEntryInfoData(entry));
            model.addAttribute("tournamentList", this.tournamentApi.qryEntryTournamentList(entry));
        }
        return "tournament/result";
    }

    @GetMapping(value = "/checkresult")
    public String checkresultController(@RequestParam int id, Model model, HttpSession session) {
        TournamentInfoData tournamentInfoData = this.tournamentApi.qryTournamentInfoById(id);
        if (tournamentInfoData != null) {
            model.addAttribute("tournamentInfo", tournamentInfoData);
            model.addAttribute("showNum", Math.ceil(tournamentInfoData.getGroupNum() * 1.0 / 2));
        }
        if (session.getAttribute("entry") != null) {
            int entry = (int) session.getAttribute("entry");
            model.addAttribute("entryInfo", this.tournamentApi.qryEntryInfoData(entry));
        }
        model.addAttribute("currentGw", this.httpApi.getCurrentEvent());
        return "tournament/checkresult";
    }

    @GetMapping(value = "/checkfixture")
    public String checkfixtureController(@RequestParam int id, Model model) {
        TournamentInfoData tournamentInfoData = this.tournamentApi.qryTournamentInfoById(id);
        if (tournamentInfoData != null) {
            model.addAttribute("tournamentInfo", tournamentInfoData);
        }
        List<TournamentGroupFixtureData> groupFixtureList = this.tournamentApi.qryGroupFixtureListById(id);
        model.addAttribute("groupFixtureList", groupFixtureList);
        List<TournamentKnockoutFixtureData> knockoutFixtureList = this.tournamentApi.qryKnockoutFixtureListById(id);
        model.addAttribute("knockoutFixtureList", knockoutFixtureList);
        model.addAttribute("currentGw", this.httpApi.getCurrentEvent());
        return "tournament/checkfixture";
    }

    @GetMapping(value = "/manage")
    public String manageController() {
        return "tournament/manage";
    }

    @GetMapping(value = "/rule")
    public String ruleController() {
        return "tournament/rule";
    }

    @ResponseBody
    @RequestMapping(value = "/createNewTournament")
    public String createNewTournament(@RequestBody TournamentCreateData tournamentCreateData) {
        tournamentCreateData
                .setLeagueType(tournamentCreateData.getUrl().contains("/standings/c") ? LeagueType.Classic.name() : LeagueType.H2h.name())
                .setLeagueId(Integer.parseInt(StringUtils.substringBetween(tournamentCreateData.getUrl(), "https://fantasy.premierleague.com/leagues/", "/standings")))
                .setGroupMode(GroupMode.getGroupModeByValue(tournamentCreateData.getGroupMode()).name())
                .setKnockoutMode(KnockoutMode.getKnockoutModeByValue(tournamentCreateData.getKnockoutMode()).name());
        return this.tournamentApi.createNewTournament(tournamentCreateData);
    }

    @ResponseBody
    @RequestMapping(value = "/createNewZjTournament")
    public String createNewZjTournament(@RequestBody ZjTournamentCreateData zjTournamentCreateData) {
        return this.tournamentApi.createNewZjTournament(zjTournamentCreateData);
    }

    @ResponseBody
    @RequestMapping(value = "/countTournamentLeagueTeams")
    public int countTournamentLeagueTeams(@RequestParam @Pattern(regexp = "^https://fantasy.premierleague.com/leagues/.*/standings/[c|h]$") String url) {
        return this.tournamentApi.countTournamentLeagueTeams(url);
    }

    @ResponseBody
    @RequestMapping(value = "/checkTournamentName")
    public boolean checkTournamentName(@RequestParam String name) {
        return this.tournamentApi.checkTournamentName(name);
    }

    @ResponseBody
    @RequestMapping(value = "/qryEntryInfo")
    public EntryInfoData qryEntryInfo(@RequestParam int entry) {
        return this.tournamentApi.qryEntryInfo(entry);
    }

    @ResponseBody
    @RequestMapping(value = "/qryTournamenList")
    public TableData<TournamentInfoData> qryTournamenList(@RequestBody TournamentQueryParam param, HttpSession session) {
        int entry;
        if (session.getAttribute("entry") != null) {
            entry = (int) session.getAttribute("entry");
            param.setEntry(entry);
        }
        return this.tournamentApi.qryTournamenList(param);
    }

    @ResponseBody
    @RequestMapping(value = "/updateTournament")
    public String updateTournament(@RequestBody TournamentCreateData tournamentCreateData) {
        return this.tournamentApi.updateTournament(tournamentCreateData);
    }

    @ResponseBody
    @RequestMapping(value = "/deleteTournamentByName")
    public String deleteTournamentByName(@RequestParam String name) {
        return this.tournamentApi.deleteTournamentByName(name);
    }

    @ResponseBody
    @RequestMapping(value = "/qryEntryTournamenList")
    public TableData<TournamentEntryData> qryEntryTournamenList(HttpSession session) {
        int entry = 0;
        if (session.getAttribute("entry") != null) {
            entry = (int) session.getAttribute("entry");
        }
        return this.tournamentApi.qryEntryTournamentList(entry);
    }

    @ResponseBody
    @RequestMapping(value = "/qryGroupInfoListByGroupId")
    public TableData<TournamentGroupData> qryGroupInfoListByGroupId(@RequestParam int tournamentId, @RequestParam int groupId) {
        return this.tournamentApi.qryGroupInfoListByGroupId(tournamentId, groupId);
    }

    @ResponseBody
    @RequestMapping(value = "/qryKnockoutResultByTournament")
    public List<TournamentKnockoutResultData> qryKnockoutResultByTournament(@RequestParam int tournamentId) {
        return this.tournamentApi.qryKnockoutResultByTournament(tournamentId);
    }

    @ResponseBody
    @RequestMapping(value = "/qryPointsGroupResult")
    public TableData<TournamentPointsGroupEventResultData> qryPointsGroupEventResult(@RequestParam int tournamentId, @RequestParam int groupId, @RequestParam int entry, int page, int limit) {
        return this.tournamentApi.qryPagePointsGroupResult(tournamentId, groupId, entry, page, limit);
    }

    @ResponseBody
    @RequestMapping(value = "/qryBattleGroupResult")
    public TableData<TournamentBattleGroupEventResultData> qryBattleGroupResult(@RequestParam int tournamentId, @RequestParam int groupId, @RequestParam int entry, int page, int limit) {
        return this.tournamentApi.qryPageBattleGroupResult(tournamentId, groupId, entry, page, limit);
    }

}

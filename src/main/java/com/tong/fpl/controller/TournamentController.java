package com.tong.fpl.controller;

import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.letletme.entry.EntryCupData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.letletmeApi.ITournamentApi;
import com.tong.fpl.utils.CommonUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/create")
    public String createController(Model model, HttpSession session) {
        model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
        String createTabId = "normal";
        if (session.getAttribute("createTabId") != null) {
            createTabId = (String) session.getAttribute("createTabId");
        }
        model.addAttribute("createTabId", createTabId);
        return "tournament/create";
    }

    @GetMapping(value = "/result")
    public String resultController(Model model, HttpSession session) {
        if (session.getAttribute("entry") != null) {
            int entry = Integer.parseInt(session.getAttribute("entry").toString());
            model.addAttribute("entryInfo", this.tournamentApi.qryEntryInfo(entry));
            model.addAttribute("tournamentList", this.tournamentApi.qryEntryTournamentList(entry));
        }
        return "tournament/result";
    }

    @GetMapping(value = "/fixture")
    public String fixtureController(@RequestParam int id, Model model) {
        TournamentInfoData tournamentInfoData = this.tournamentApi.qryTournamentInfoById(id);
        if (tournamentInfoData != null) {
            model.addAttribute("tournamentInfo", tournamentInfoData);
        }
        List<TournamentGroupFixtureData> groupFixtureList = this.tournamentApi.qryGroupFixtureListById(id);
        model.addAttribute("groupFixtureList", groupFixtureList);
        List<TournamentKnockoutFixtureData> knockoutFixtureList = this.tournamentApi.qryKnockoutFixtureListById(id);
        model.addAttribute("knockoutFixtureList", knockoutFixtureList);
        model.addAttribute("currentGw", this.tournamentApi.getCurrentEvent());
        return "tournament/fixture";
    }

    @GetMapping(value = "/pointsResult")
    public String pointsResultController(@RequestParam int id, Model model, HttpSession session) {
        TournamentInfoData tournamentInfoData = this.tournamentApi.qryTournamentInfoById(id);
        if (tournamentInfoData != null) {
            model.addAttribute("tournamentInfo", tournamentInfoData);
            model.addAttribute("showNum", (int) Math.ceil(tournamentInfoData.getGroupNum() * 1.0 / 2));
        }
        if (session.getAttribute("entry") != null) {
            int entry = Integer.parseInt(session.getAttribute("entry").toString());
            model.addAttribute("entryInfo", this.tournamentApi.qryEntryInfo(entry));
        }
        model.addAttribute("currentGw", this.tournamentApi.getCurrentEvent());
        return "tournament/pointsResult";
    }

    @GetMapping(value = "/battleResult")
    public String battleResultController(@RequestParam int id, Model model, HttpSession session) {
        TournamentInfoData tournamentInfoData = this.tournamentApi.qryTournamentInfoById(id);
        if (tournamentInfoData != null) {
            model.addAttribute("tournamentInfo", tournamentInfoData);
            model.addAttribute("showNum", (int) Math.ceil(tournamentInfoData.getGroupNum() * 1.0 / 2));
        }
        if (session.getAttribute("entry") != null) {
            int entry = Integer.parseInt(session.getAttribute("entry").toString());
            model.addAttribute("entryInfo", this.tournamentApi.qryEntryInfo(entry));
        }
        model.addAttribute("currentGw", this.tournamentApi.getCurrentEvent());
        return "tournament/battleResult";
    }

    @GetMapping(value = "/manage")
    public String manageController() {
        return "tournament/manage";
    }

    @GetMapping(value = "/rule")
    public String ruleController() {
        return "tournament/rule";
    }

    /**
     * @apiNote create
     */
    @ResponseBody
    @RequestMapping(value = "/createNewTournament")
    public String createNewTournament(@RequestBody TournamentCreateData tournamentCreateData) {
        tournamentCreateData
                .setGroupMode(GroupMode.getGroupModeByValue(tournamentCreateData.getGroupMode()).name())
                .setKnockoutMode(KnockoutMode.getKnockoutModeByValue(tournamentCreateData.getKnockoutMode()).name());
        if (StringUtils.isNotEmpty(tournamentCreateData.getUrl())) {
            if (StringUtils.equals(LeagueType.Swiss.name(), tournamentCreateData.getLeagueType())) {
                tournamentCreateData.setLeagueType(LeagueType.Swiss.name());
            } else if (StringUtils.equals(LeagueType.Royale.name(), tournamentCreateData.getLeagueType())) {
                tournamentCreateData.setLeagueType(LeagueType.Royale.name());
            } else {
                tournamentCreateData.setLeagueType(tournamentCreateData.getUrl().contains("/standings/c") ? LeagueType.Classic.name() : LeagueType.H2h.name());
            }
            tournamentCreateData.setLeagueId(CommonUtils.getLeagueId(tournamentCreateData.getUrl()));
        } else {
            tournamentCreateData
                    .setLeagueType(LeagueType.Custom.name())
                    .setLeagueId(0);
        }
        return this.tournamentApi.createNewTournament(tournamentCreateData);
    }

    @ResponseBody
    @RequestMapping(value = "/countTournamentLeagueTeams")
    public int countTournamentLeagueTeams(@RequestParam @Pattern(regexp = "^https://fantasy.premierleague.com/leagues/.*/standings/[c|h]$") String url) {
        return this.tournamentApi.countTournamentLeagueTeams(url);
    }

    @ResponseBody
    @RequestMapping(value = "/qryLeagueEntryList")
    public TableData<EntryInfoData> qryLeagueEntryList(@RequestParam @Pattern(regexp = "^https://fantasy.premierleague.com/leagues/.*/standings/[c|h]$") String url) {
        return this.tournamentApi.qryLeagueEntryList(url);
    }

    @ResponseBody
    @RequestMapping(value = "/checkTournamentName")
    public boolean checkTournamentName(@RequestParam String name) {
        return this.tournamentApi.checkTournamentName(name);
    }

    /**
     * @apiNote result
     */
    @ResponseBody
    @RequestMapping(value = "/qryEntryTournamentList")
    public TableData<TournamentEntryData> qryEntryTournamentList(HttpSession session) {
        int entry = 0;
        if (session.getAttribute("entry") != null) {
            entry = Integer.parseInt(session.getAttribute("entry").toString());
        }
        return this.tournamentApi.qryEntryTournamentList(entry);
    }

    /**
     * @apiNote checkResult
     */
    @ResponseBody
    @RequestMapping(value = "/qryPointsGroupChampion")
    public TableData<TournamentGroupEventChampionData> qryPointsGroupChampion(@RequestParam int tournamentId) {
        return this.tournamentApi.qryPointsGroupChampion(tournamentId);
    }

    @ResponseBody
    @RequestMapping(value = "/qryPointsGroupResult")
    public TableData<TournamentPointsGroupEventResultData> qryPointsGroupEventResult(@RequestParam int tournamentId, @RequestParam int groupId, @RequestParam int entry, int page, int limit) {
        return this.tournamentApi.qryPagePointsGroupResult(tournamentId, groupId, entry, page, limit);
    }

    @ResponseBody
    @RequestMapping(value = "/qryEntryEventCupResult")
    public TableData<EntryCupData> qryEntryEventCupResult(@RequestParam int entry, int page, int limit) {
        return this.tournamentApi.qryPageEntryEventCupResult(entry, page, limit);
    }

    @ResponseBody
    @RequestMapping(value = "/qryBattleGroupResult")
    public TableData<TournamentBattleGroupEventResultData> qryBattleGroupResult(@RequestParam int tournamentId, @RequestParam int groupId, @RequestParam int entry, int page, int limit) {
        return this.tournamentApi.qryPageBattleGroupResult(tournamentId, groupId, entry, page, limit);
    }

    @ResponseBody
    @RequestMapping(value = "/qryKnockoutBracketResultByTournament")
    public KnockoutBracketData qryKnockoutBracketResultByTournament(@RequestParam int tournamentId) {
        return this.tournamentApi.qryKnockoutBracketResultByTournament(tournamentId);
    }

    /**
     * @apiNote manage
     */
    @ResponseBody
    @RequestMapping(value = "/updateTournamentInfo")
    public String updateTournamentInfo(@RequestBody TournamentCreateData tournamentCreateData) {
        return this.tournamentApi.updateTournamentInfo(tournamentCreateData);
    }

    @ResponseBody
    @RequestMapping(value = "/deleteTournamentByName")
    public String deleteTournamentByName(@RequestParam String name) {
        return this.tournamentApi.deleteTournamentByName(name);
    }

    /**
     * @apiNote common
     */
    @ResponseBody
    @RequestMapping(value = "/qryEntryInfo")
    public EntryInfoData qryEntryInfo(@RequestParam int entry) {
        return this.tournamentApi.qryEntryInfo(entry);
    }

    @ResponseBody
    @RequestMapping(value = "/qryTournamentList")
    public TableData<TournamentInfoData> qryTournamentList(@RequestBody TournamentQueryParam param, HttpSession session) {
        int entry;
        if (session.getAttribute("entry") != null) {
            entry = Integer.parseInt(session.getAttribute("entry").toString());
            param.setEntry(entry);
        }
        return this.tournamentApi.qryTournamentList(param);
    }

    @ResponseBody
    @RequestMapping(value = "/qryGroupInfoListByGroupId")
    public TableData<TournamentGroupData> qryGroupInfoListByGroupId(@RequestParam int tournamentId, @RequestParam int groupId) {
        return this.tournamentApi.qryGroupInfoListByGroupId(tournamentId, groupId);
    }

}

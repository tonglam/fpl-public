package com.tong.fpl.controller;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.api.ITournamentApi;
import com.tong.fpl.constant.Constant;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.global.KnockoutBracketData;
import com.tong.fpl.domain.letletme.global.StepsData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.tournament.*;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public String zjRealodController(Model model, HttpSession session) {
        model.addAttribute("gwMap", CommonUtils.createGwMapStartFromCurrentForOption(this.httpApi.getNextEvent()));
        // input entry
        int inputGroupNum = 4;
        if (session.getAttribute("inputGroupNum") != null) {
            inputGroupNum = Integer.parseInt(session.getAttribute("inputGroupNum").toString());
        }
        model.addAttribute("inputGroupNum", inputGroupNum);
        model.addAttribute("inputShowNum", (int) Math.ceil(inputGroupNum * 1.0 / 2));
        int inputTeamPerGroup = 4;
        if (session.getAttribute("inputTeamPerGroup") != null) {
            inputTeamPerGroup = Integer.parseInt(session.getAttribute("inputTeamPerGroup").toString());
        }
        model.addAttribute("inputTeamPerGroup", inputTeamPerGroup);
        // zj input entry
        int zjGroupNum = 4;
        if (session.getAttribute("zjGroupNum") != null) {
            zjGroupNum = Integer.parseInt(session.getAttribute("zjGroupNum").toString());
        }
        model.addAttribute("zjGroupNum", zjGroupNum);
        model.addAttribute("showNum", (int) Math.ceil(zjGroupNum * 1.0 / 2));
        int zjTeamPerGroup = 4;
        if (session.getAttribute("zjTeamPerGroup") != null) {
            zjTeamPerGroup = Integer.parseInt(session.getAttribute("zjTeamPerGroup").toString());
        }
        model.addAttribute("zjTeamPerGroup", zjTeamPerGroup);
        String createTabId = "normal";
        if (session.getAttribute("createTabId") != null) {
            createTabId = (String) session.getAttribute("createTabId");
        }
        model.addAttribute("createTabId", createTabId);
        return "tournament/create";
    }

    @GetMapping(value = "/create/inputReload")
    public String inputRealodController(@RequestParam int inputGroupNum, @RequestParam int inputTeamPerGroup, HttpSession session) {
        session.setAttribute("inputGroupNum", inputGroupNum);
        session.setAttribute("inputTeamPerGroup", inputTeamPerGroup);
        return "redirect:/tournament/create";
    }

    @GetMapping(value = "/create/zjReload")
    public String zjRealodController(@RequestParam int zjGroupNum, @RequestParam int zjTeamPerGroup, HttpSession session) {
        session.setAttribute("zjGroupNum", zjGroupNum);
        session.setAttribute("zjTeamPerGroup", zjTeamPerGroup);
        return "redirect:/tournament/create";
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
        model.addAttribute("currentGw", this.httpApi.getCurrentEvent());
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
        model.addAttribute("currentGw", this.httpApi.getCurrentEvent());
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
        model.addAttribute("currentGw", this.httpApi.getCurrentEvent());
        return "tournament/battleResult";
    }

    @GetMapping(value = "/zjResult")
    public String zjResultController(@RequestParam int id, Model model) {
        TournamentInfoData tournamentInfoData = this.tournamentApi.qryTournamentInfoById(id);
        if (tournamentInfoData != null) {
            model.addAttribute("tournamentInfo", tournamentInfoData);
            model.addAttribute("phaseOneShowNum", Math.ceil(tournamentInfoData.getGroupNum() * 1.0 / 2));
            model.addAttribute("phaseTwoShowNum", Math.ceil(tournamentInfoData.getTotalTeam() * 1.0 / 2));
        }
        List<ZjTournamentCaptainData> captainDataList = this.tournamentApi.qryZjTournamentCaptain(id);
        if (!CollectionUtils.isEmpty(captainDataList)) {
            if (LocalDateTime.now().isAfter(LocalDateTime.parse(captainDataList.get(0).getPhaseTwoDeadline(), DateTimeFormatter.ofPattern(Constant.DATETIME)))) {
                model.addAttribute("phaseTwoShow", true);
            } else {
                model.addAttribute("phaseTwoShow", false);
            }
            if (LocalDateTime.now().isAfter(LocalDateTime.parse(captainDataList.get(0).getPkDeadline(), DateTimeFormatter.ofPattern(Constant.DATETIME)))) {
                model.addAttribute("pkShow", true);
            } else {
                model.addAttribute("pkShow", false);
            }
        }
        List<TournamentKnockoutResultData> knockoutResultList = this.tournamentApi.qryZjTournamentPkResultByTournament(id);
        model.addAttribute("knockoutResultList", knockoutResultList);
        return "tournament/zjResult";
    }

    @GetMapping(value = "/manage")
    public String manageController() {
        return "tournament/manage";
    }

    @GetMapping(value = "/manageZjTournament")
    public String manageZjTournamentController(@RequestParam int id, Model model, HttpSession session) {
        model.addAttribute("captainDataList", this.tournamentApi.qryZjTournamentCaptain(id));
        model.addAttribute("groupNameMap", this.tournamentApi.qryZjTournamentGroupNameMap(id));
        TournamentInfoData tournamentInfoData = this.tournamentApi.qryTournamentInfoById(id);
        if (tournamentInfoData != null) {
            model.addAttribute("tournamentInfo", tournamentInfoData);
            model.addAttribute("phaseTwoShowNum", Math.ceil(tournamentInfoData.getTeamPerGroup() * 1.0 / 2));
        }
        List<TournamentKnockoutEventFixtureData> pkPickList = this.tournamentApi.qryZjPkPickListById(id);
        model.addAttribute("pkPickList", pkPickList);
        String manageZjTabId = "phaseTwo";
        if (session.getAttribute("manageZjTabId") != null) {
            manageZjTabId = session.getAttribute("manageZjTabId").toString();
        }
        model.addAttribute("manageZjTabId", manageZjTabId);
        return "tournament/manageZjTournament";
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
        if (StringUtils.isNotBlank(tournamentCreateData.getUrl())) {
            tournamentCreateData.setLeagueType(tournamentCreateData.getUrl().contains("/standings/c") ? LeagueType.Classic.name() : LeagueType.H2h.name());
            tournamentCreateData.setLeagueId(CommonUtils.getLeagueIdByType(tournamentCreateData.getUrl(), tournamentCreateData.getLeagueType()));
        } else {
            tournamentCreateData
                    .setLeagueType(LeagueType.Custom.name())
                    .setLeagueId(0);
        }
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
     * @apiNote checkZjResult
     */
    @ResponseBody
    @RequestMapping(value = "/qryZjTournamentGroupResult")
    public TableData<TournamentPointsGroupEventResultData> qryZjTournamentGroupResult(@RequestParam int tournamentId, @RequestParam int stage, @RequestParam int groupId, @RequestParam int entry, int page, int limit) {
        return this.tournamentApi.qryZjTournamentGroupResult(tournamentId, stage, groupId, entry, page, limit);
    }

    @ResponseBody
    @RequestMapping(value = "/qryZjTournamentResultById")
    public TableData<ZjTournamentResultData> qryZjTournamentResultById(@RequestParam int tournamentId) {
        return this.tournamentApi.qryZjTournamentResultById(tournamentId);
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
     * @apiNote manageZjTournament
     */
    @ResponseBody
    @RequestMapping(value = "/qryZjTournamentPhaseOneRankByGroupId")
    public int qryZjTournamentPhaseOneRankByGroupId(@RequestParam int tournamentId, @RequestParam int currentGroupId) {
        return this.tournamentApi.qryZjTournamentPhaseOneRankByGroupId(tournamentId, currentGroupId);
    }

    @ResponseBody
    @RequestMapping(value = "/qryGroupEntryInfoList")
    public List<EntryInfoData> qryGroupEntryInfoList(@RequestParam int tournamentId, @RequestParam int groupId) {
        return this.tournamentApi.qryGroupEntryInfoList(tournamentId, groupId);
    }

    @ResponseBody
    @RequestMapping(value = "/qryDiscloseGroupData")
    public TournamentGroupData qryDiscloseGroupData(@RequestParam int tournamentId, @RequestParam int entry, @RequestParam int currentGroupId) {
        return this.tournamentApi.qryDiscloseGroupData(tournamentId, entry, currentGroupId);
    }

    @ResponseBody
    @RequestMapping(value = "/qrySeeableGroupInfoListByGroupId")
    public TableData<TournamentGroupData> qrySeeableGroupInfoListByGroupId(@RequestParam int tournamentId, @RequestParam int currentGroupId, @RequestParam int groupId) {
        return this.tournamentApi.qrySeeableGroupInfoListByGroupId(tournamentId, currentGroupId, groupId);
    }

    @ResponseBody
    @RequestMapping(value = "/updateZjTournamentPhaseTwoGroupData")
    public String updateZjTournamentPhaseTwoGroupData(@RequestBody List<TournamentGroupData> groupDataList, HttpSession session) {
        if (CollectionUtils.isEmpty(groupDataList)) {
            return "分配列表不能为空!";
        }
        int captainEntry = Integer.parseInt(session.getAttribute("entry").toString());
        return this.tournamentApi.updateZjTournamentPhaseTwoGroupData(groupDataList, captainEntry);
    }

    @ResponseBody
    @RequestMapping(value = "/qryZjTournamentPkPickSteps")
    public StepsData qryZjTournamentPkPickSteps(@RequestParam int tournamentId) {
        return this.tournamentApi.qryZjTournamentPkPickSteps(tournamentId);
    }

    @ResponseBody
    @RequestMapping(value = "/qryZjTournamentPkPickableList")
    public TableData<TournamentGroupData> qryZjTournamentPkPickableList(@RequestParam int tournamentId, @RequestParam int currentGroupId) {
        return this.tournamentApi.qryZjTournamentPkPickableList(tournamentId, currentGroupId);
    }

    @ResponseBody
    @RequestMapping(value = "/updateZjTournamentPkData")
    public String updateZjTournamentPkData(@RequestParam int tournamentId, @RequestParam int entry, @RequestParam int pkEntry, HttpSession session) {
        int captainEntry = Integer.parseInt(session.getAttribute("entry").toString());
        return this.tournamentApi.updateZjTournamentPkData(tournamentId, entry, pkEntry, captainEntry);
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

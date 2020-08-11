package com.tong.fpl.controller;

import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.letletme.TournamentCreateData;
import com.tong.fpl.service.ITournamentManagementService;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;

/**
 * Create by tong on 2020/6/23
 */
@Validated
@Controller
@RequestMapping(value = "/tournament")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentManagementController {

    private final ITournamentManagementService tournamentManagementService;

    @RequestMapping(value = {"", "/"})
    public String tournamentManagementController(Model model) {
        model.addAttribute("title", "自定义赛事-letletme");
        return "tournament";
    }

    @RequestMapping(value = "/create")
    public String tournamentCreateController(Model model) {
        model.addAttribute("title", "创建赛事-自定义赛事-letletme");
        model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
        return "create";
    }

    @RequestMapping(value = "/rule")
    public String tournamentRuleController(Model model) {
        model.addAttribute("title", "规则-自定义赛事-letletme");
        return "rule";
    }

    @ResponseBody
    @PostMapping(value = "/createNewTournament")
    public String createNewTournament(@RequestBody TournamentCreateData tournamentCreateData) {
        tournamentCreateData
                .setLeagueType(tournamentCreateData.getUrl().contains("/standings/c") ? LeagueType.Classic.name() : LeagueType.H2h.name())
                .setLeagueId(Integer.parseInt(StringUtils.substringBetween(tournamentCreateData.getUrl(), "https://fantasy.premierleague.com/leagues/", "/standings")))
                .setGroupMode(GroupMode.getGroupModeFromValue(tournamentCreateData.getGroupMode()).name())
                .setKnockoutMode(KnockoutMode.getKnockoutModeFromValue(tournamentCreateData.getKnockoutMode()).name());
//        return this.tournamentManagementService.createNewTournament(tournamentCreateData);
        return "创建成功!";
    }

    @ResponseBody
    @GetMapping(value = "/countLeagueTeams")
    public int countLeagueTeams(@Pattern(regexp = "^https://fantasy.premierleague.com/leagues/.*/standings/[c|h]$") String url) {
//		return this.tournamentManagementService.countLeagueTeams(url);
        return 107;
    }

    @ResponseBody
    @GetMapping(value = "/checkTournamentName")
    public boolean checkTournamentName(String name) {
//        return this.tournamentManagementService.checkTournamentName(name);
        return true;
    }

}

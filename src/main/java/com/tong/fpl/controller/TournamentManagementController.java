package com.tong.fpl.controller;

import com.tong.fpl.api.ITournamentManagementApi;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.data.letletme.tournament.TournamentCreateData;
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

    private final ITournamentManagementApi tournamentManagementApi;

    @RequestMapping(value = "/create")
    public String tournamentCreateController(Model model) {
        model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
        return "create";
    }

    @RequestMapping(value = "/rule")
    public String tournamentRuleController() {
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
        return this.tournamentManagementApi.createNewTournament(tournamentCreateData);
    }

    @ResponseBody
    @GetMapping(value = "/countTournamentLeagueTeams")
    public int countTournamentLeagueTeams(@Pattern(regexp = "^https://fantasy.premierleague.com/leagues/.*/standings/[c|h]$") String url) {
        return this.tournamentManagementApi.countTournamentLeagueTeams(url);
    }

    @ResponseBody
    @GetMapping(value = "/checkTournamentName")
    public boolean checkTournamentName(String name) {
        return this.tournamentManagementApi.checkTournamentName(name);
    }

}

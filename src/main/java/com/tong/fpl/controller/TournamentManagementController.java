package com.tong.fpl.controller;

import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.domain.data.letletme.TournamentCreateData;
import com.tong.fpl.service.impl.TournamentManagementServiceImpl;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Create by tong on 2020/6/23
 */
@Validated
@Controller
@RequestMapping(value = "/tournament")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentManagementController {

    private final TournamentManagementServiceImpl createTournamentService;

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
    @PostMapping(value = {"/createNewTournament"})
    public String createNewTournament(@RequestBody TournamentCreateData tournamentCreateData) {
        tournamentCreateData
                .setGroupMode(GroupMode.getGroupModeFromValue(tournamentCreateData.getGroupMode()).name())
                .setKnockoutMode(KnockoutMode.getKnockoutModeFromValue(tournamentCreateData.getKnockoutMode()).name());
        return this.createTournamentService.createNewTournament(tournamentCreateData);
    }

}

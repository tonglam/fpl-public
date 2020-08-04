package com.tong.fpl.controller;

import com.tong.fpl.domain.data.letletme.TournamentCreateData;
import com.tong.fpl.service.impl.TournamentManagementServiceImpl;
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
		model.addAttribute("entry", 3697);
		return "index";
	}

	@ResponseBody
	@PostMapping(value = {"/createNewTournament"})
	public String createNewTournament(@RequestBody TournamentCreateData tournamentCreateData) {
		return this.createTournamentService.createNewTournament(tournamentCreateData);
	}

}

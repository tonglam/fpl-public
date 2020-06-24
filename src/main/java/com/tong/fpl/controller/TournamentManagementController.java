package com.tong.fpl.controller;

import com.tong.fpl.data.fpl.TournamentCreateData;
import com.tong.fpl.service.impl.TournamentManagementImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
@RequestMapping(value = "/cup")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentManagementController {

	private final TournamentManagementImpl createTournamentService;

	@RequestMapping(value = {"", "/"})
	public String tournamentManagementController() {
		return "cup";
	}

	@ResponseBody
	@PostMapping(value = {"/createNewTournament"})
	public String createNewTournament(@RequestBody TournamentCreateData tournamentCreateData) {
		return this.createTournamentService.createNewTournament(tournamentCreateData);
	}

}

package com.tong.fpl.controller;

import com.tong.fpl.domain.data.letletme.TournamentCreateData;
import com.tong.fpl.service.impl.TournamentManagementServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Create by tong on 2020/6/23
 */
@Validated
@RestController
@RequestMapping(value = "/tournament")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentManagementController {

	private final TournamentManagementServiceImpl createTournamentService;

	@RequestMapping(value = {"", "/"})
	public String tournamentManagementController() {
		return "tournament";
	}

	@ResponseBody
	@PostMapping(value = {"/createNewTournament"})
	public String createNewTournament(@RequestBody TournamentCreateData tournamentCreateData) {
		return this.createTournamentService.createNewTournament(tournamentCreateData);
	}

}

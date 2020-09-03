package com.tong.fpl.controller;

import com.tong.fpl.api.ITournamentApi;
import com.tong.fpl.constant.enums.GroupMode;
import com.tong.fpl.constant.enums.KnockoutMode;
import com.tong.fpl.constant.enums.LeagueType;
import com.tong.fpl.domain.letletme.table.TableData;
import com.tong.fpl.domain.letletme.tournament.EntryTournamentData;
import com.tong.fpl.domain.letletme.tournament.TournamentCreateData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.Pattern;

/**
 * Create by tong on 2020/6/23
 */
@Validated
@Controller
@RequestMapping(value = "/tournament")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TournamentController {

	private final ITournamentApi tournamentApi;

	@RequestMapping(value = "/create")
	public String createController(Model model) {
		model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
		return "create";
	}

	@RequestMapping(value = "/manage")
	public String manageController() {
		return "manage";
	}

	@RequestMapping(value = "/result")
	public String resultController(Model model, HttpSession session) {
		if (session.getAttribute("entry") != null) {
			int entry = (int) session.getAttribute("entry");
			if (entry > 0) {
				model.addAttribute("entryInfo", this.tournamentApi.qryEntryInfoData(entry));
				model.addAttribute("tournamentList", this.tournamentApi.qryEntryTournamentList(entry));
			}
		}
		return "result";
	}

	@RequestMapping(value = "/tournamentresult")
	public String tournamentresultController(@RequestParam int id, Model model) {
		model.addAttribute("tournamentInfo", this.tournamentApi.qryTournamentInfoById(id));
//		model.addAttribute("tournamentGroupList", this.tournamentApi.qryGroupListByTournamentId(id));
//		model.addAttribute("tournamentKnockoutList", this.tournamentApi.qryKnockoutListByTournamentId(id));
		return "tournamentresult";
	}

	@RequestMapping(value = "/rule")
	public String ruleController() {
		return "rule";
	}

	@ResponseBody
	@RequestMapping(value = "/createNewTournament")
	public String createNewTournament(@RequestBody TournamentCreateData tournamentCreateData) {
		tournamentCreateData
				.setLeagueType(tournamentCreateData.getUrl().contains("/standings/c") ? LeagueType.Classic.name() : LeagueType.H2h.name())
				.setLeagueId(Integer.parseInt(StringUtils.substringBetween(tournamentCreateData.getUrl(), "https://fantasy.premierleague.com/leagues/", "/standings")))
				.setGroupMode(GroupMode.getGroupModeFromValue(tournamentCreateData.getGroupMode()).name())
				.setKnockoutMode(KnockoutMode.getKnockoutModeFromValue(tournamentCreateData.getKnockoutMode()).name());
		return this.tournamentApi.createNewTournament(tournamentCreateData);
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
	@RequestMapping(value = "/qryTournamenList")
	public TableData<TournamentInfoData> qryTournamenList(@RequestBody TournamentQueryParam param) {
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
	public TableData<EntryTournamentData> qryEntryTournamenList(HttpSession session) {
		int entry = 0;
		if (session.getAttribute("entry") != null) {
			entry = (int) session.getAttribute("entry");
		}
		return this.tournamentApi.qryEntryTournamentList(entry);
	}

}

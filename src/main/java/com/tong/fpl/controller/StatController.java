package com.tong.fpl.controller;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.api.IStatApi;
import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.tournament.TournamentEventCaptainData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Create by tong on 2020/8/15
 */
@Controller
@RequestMapping(value = "/stat")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatController {

	private final IStatApi statApi;
	private final IHttpApi httpApi;

	@GetMapping(value = "/price")
	public String priceController() {
		return "stat/price";
	}

	@GetMapping(value = "/compare")
	public String compareController() {
		return "stat/compare";
	}

	@GetMapping(value = "/captain")
	public String captainController(Model model) {
		model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
		model.addAttribute("seasonMap", CommonUtils.createSeasonMapForOption());
		return "stat/captain";
	}

	@GetMapping(value = "/leagueCaptain")
	public String leagueCaptainController(@RequestParam int id, Model model) {
		TournamentInfoData tournamentInfoData = this.statApi.qryTournamentInfoById(id);
		if (tournamentInfoData != null) {
			model.addAttribute("tournamentInfo", tournamentInfoData);
		}
		model.addAttribute("gwMap", CommonUtils.createCurrentGwMapForOption(this.httpApi.getCurrentEvent()));
		return "stat/leagueCaptain";
	}

	@GetMapping(value = "/selected")
	public String selectedController(Model model) {
		List<String> leagueList = this.statApi.qryTeamSelectStatList();
		if (!CollectionUtils.isEmpty(leagueList)) {
			model.addAttribute("leagueList", leagueList);
		}
		model.addAttribute("currentGw", this.httpApi.getCurrentEvent());
		model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
		return "stat/selected";
	}

	/**
	 * @apiNote price
	 */
	@RequestMapping("/qryPriceChangeList")
	@ResponseBody
	public TableData<PlayerValueData> qryPriceChangeList() {
		return this.statApi.qryPriceChangeList();
	}

	/**
	 * @apiNote captain
	 */
	@ResponseBody
	@RequestMapping(value = "/qryTournamenList")
	public TableData<TournamentInfoData> qryTournamenList(@RequestBody TournamentQueryParam param, HttpSession session) {
		int entry;
		if (session.getAttribute("entry") != null) {
			entry = Integer.parseInt(session.getAttribute("entry").toString());
			param.setEntry(entry);
		}
		return this.statApi.qryTournamenList(param);
	}

	@RequestMapping("/qryLeagueCaptainDataList")
	@ResponseBody
	public TableData<TournamentEventCaptainData> qryLeagueCaptainDataList(@RequestParam int tournamentId) {
		return this.statApi.qryLeagueCaptainDataList(tournamentId);
	}

	@RequestMapping("/qryLeagueEventCaptainDataList")
	@ResponseBody
	public TableData<EntryEventCaptainData> qryLeagueEventCaptainDataList(@RequestParam int tournamentId, @RequestParam int event) {
		return this.statApi.qryLeagueEventCaptainDataList(tournamentId, event);
	}

	/**
	 * @apiNote compare
	 */
	@RequestMapping("/qryPlayerList")
	@ResponseBody
	public TableData<PlayerInfoData> qryPlayerList(@RequestParam String season) {
		return this.statApi.qryPlayerList(season);
	}

	/**
	 * @apiNote selected
	 */
	@RequestMapping("/qryTeamSelectStatByName")
	@ResponseBody
	public TableData<LeagueStatData> qryTeamSelectStatByName(@RequestParam String leagueName, @RequestParam int event) {
		return this.statApi.qryTeamSelectStatByName(leagueName, event);
	}

}

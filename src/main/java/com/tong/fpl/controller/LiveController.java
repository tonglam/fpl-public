package com.tong.fpl.controller;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.api.ILiveApi;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Create by tong on 2020/6/23
 */
@Controller
@RequestMapping(value = "/live")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveController {

	private final IHttpApi httpApi;
	private final ILiveApi liveApi;

	@GetMapping(value = "/entry")
	public String entryController() {
		return "live/entry";
	}

	@GetMapping(value = "/liveEntry")
	public String liveEntryController(int liveEntry, HttpSession session) {
		session.setAttribute("liveEntry", liveEntry);
		return "forward:/live/entry";
	}

	@GetMapping(value = "/league")
	public String leagueController(Model model) {
		model.addAttribute("currentGw", this.httpApi.getCurrentEvent());
		return "live/league";
	}

	@GetMapping(value = "/match")
	public String matchController(Model model) {
		model.addAttribute("matchList", this.liveApi.qryLiveMatchList());
		return "live/match";
	}

	/**
	 * @apiNote entry
	 */
	@RequestMapping("/qryEntryLivePoints")
	@ResponseBody
	public TableData<LiveCalaData> qryEntryLivePoints(HttpSession session) {
		int entry = this.getLiveEntry(session);
		if (entry == 0) {
			return new TableData<>();
		}
		return this.liveApi.qryEntryLivePoints(entry);
	}

	private int getLiveEntry(HttpSession session) {
		if (session.getAttribute("liveEntry") != null) {
			return Integer.parseInt(session.getAttribute("liveEntry").toString());
		} else if (session.getAttribute("entry") != null) {
			return Integer.parseInt(session.getAttribute("entry").toString());
		}
		return 0;
	}

	/**
	 * @apiNote league
	 */
	@RequestMapping("/qryTournamentLivePoints")
	@ResponseBody
	public TableData<LiveCalaData> qryTournamentLivePoints(@RequestParam int tournamentId) {
		return this.liveApi.qryTournamentLivePoints(tournamentId);
	}

	/**
	 * @apiNote match
	 */
	@RequestMapping("/qryLiveFixturePlayerList")
	@ResponseBody
	public TableData<ElementEventResultData> qryLiveFixturePlayerList(@RequestParam int teamId) {
		return this.liveApi.qryLiveFixturePlayerList(teamId);
	}

}

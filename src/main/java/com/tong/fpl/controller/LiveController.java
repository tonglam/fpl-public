package com.tong.fpl.controller;

import com.tong.fpl.api.ILiveApi;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.LiveCalaData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

	private final ILiveApi liveApi;

	@RequestMapping(value = "/entry")
	public String entryController() {
		return "live/entry";
	}

	@RequestMapping(value = "/leaguerank")
	public String leaguerankController() {
		return "live/league";
	}

	@RequestMapping(value = "/saveLiveEntry")
	@ResponseBody
	public void saveLiveEntry(@RequestParam int liveEntry, HttpSession session) {
		session.setAttribute("liveEntry", liveEntry);
	}

	@GetMapping("/qryEntryLivePoints")
	@ResponseBody
	public TableData<LiveCalaData> qryEntryLivePoints(HttpSession session) {
		int entry = 0;
		if (session.getAttribute("liveEntry") != null) {
			entry = (int) session.getAttribute("liveEntry");
		}
		return this.liveApi.qryEntryLivePoints(entry);
	}

	@GetMapping("/qryTournamentLivePoints")
	@ResponseBody
	public TableData<LiveCalaData> qryTournamentLivePoints(@RequestParam int tournamentId) {
		return this.liveApi.qryTournamentLivePoints(tournamentId);
	}

}

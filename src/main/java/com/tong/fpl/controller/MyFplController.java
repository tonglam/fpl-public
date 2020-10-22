package com.tong.fpl.controller;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.api.IMyFplApi;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentGroupData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.utils.CommonUtils;
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
@RequestMapping(value = "/my_fpl")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyFplController {

	private final IMyFplApi myFplApi;
	private final IHttpApi httpApi;

	@GetMapping(value = "/pick")
	public String pickController() {
		return "myFpl/pick";
	}

	@GetMapping(value = "/entry")
	public String entryController(Model model, HttpSession session) {
		int entry = this.getQryEntry(session);
		model.addAttribute("entryInfo", this.myFplApi.qryEntryInfo(entry));
		return "myFpl/entry";
	}

	@GetMapping(value = "/league")
	public String leagueController(Model model, HttpSession session) {
		int entry = 0;
		if (session.getAttribute("entry") != null) {
			entry = Integer.parseInt(session.getAttribute("entry").toString());
		}
		TableData<TournamentInfoData> tournamentInfoData = this.myFplApi.qryEntryPointsGroupTournamentList(entry);
		if (tournamentInfoData != null) {
			model.addAttribute("tournamentList", tournamentInfoData.getData());
		}
		model.addAttribute("currentGw", this.httpApi.getCurrentEvent());
		model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
		return "myFpl/league";
	}

	/**
	 * @apiNote entry
	 */
	@RequestMapping("/qryEntryResultList")
	@ResponseBody
	public TableData<EntryEventResultData> qryEntryResultList(HttpSession session) {
		int entry = this.getQryEntry(session);
		return this.myFplApi.qryEntryResultList(entry);
	}

	@RequestMapping("/qryEntryEventResult")
	@ResponseBody
	public TableData<EntryPickData> qryEntryEventResult(@RequestParam int event, HttpSession session) {
		int entry = this.getQryEntry(session);
		return this.myFplApi.qryEntryEventResult(event, entry);
	}

	/**
	 * @apiNote pick
	 */
	@RequestMapping("/qryPlayerDataList")
	@ResponseBody
	public TableData<PlayerInfoData> qryPlayerDataList(@RequestParam long page, @RequestParam long limit) {
		return this.myFplApi.qryPlayerDataList(page, limit);
	}

	/**
	 * @apiNote league
	 */
	@RequestMapping("/qryTournamentResultList")
	@ResponseBody
	public TableData<TournamentGroupData> qryTournamentResultList(@RequestParam int tournamentId, @RequestParam int event) {
		return this.myFplApi.qryTournamentResultList(tournamentId, event);
	}

	private int getQryEntry(HttpSession session) {
		int entry = 0;
		if (session.getAttribute("myFplEntry") != null) {
			entry = Integer.parseInt(session.getAttribute("myFplEntry").toString());
		} else if (session.getAttribute("entry") != null) {
			entry = Integer.parseInt(session.getAttribute("entry").toString());
		}
		return entry;
	}

}

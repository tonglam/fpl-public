package com.tong.fpl.controller;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.api.IMyFplApi;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportData;
import com.tong.fpl.domain.letletme.league.LeagueEventReportStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentQueryParam;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
	public String entryController() {
		return "myFpl/entry";
	}

	@GetMapping(value = "/league")
	public String leagueController(Model model) {
		model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
		model.addAttribute("seasonMap", CommonUtils.createSeasonMapForOption());
		return "myFpl/league";
	}

	@GetMapping(value = "/leagueCaptainReport")
	public String leagueReportController(@RequestParam int leagueId, @RequestParam String leagueType, Model model) {
		model.addAttribute("leagueName", this.myFplApi.qryLeagueNameByIdAndType(leagueId, leagueType));
		model.addAttribute("gwMap", CommonUtils.createCurrentGwMapForOption(this.httpApi.getCurrentEvent()));
		return "myFpl/leagueCaptainReport";
	}

	/**
	 * @apiNote entry
	 */
	@RequestMapping("/qryEntryResultList")
	@ResponseBody
	public TableData<EntryEventResultData> qryEntryResultList() {
		return new TableData<>();
//		return this.myFplApi.qryEntryResultList(entry);
	}

	@RequestMapping("/qryEntryEventResult")
	@ResponseBody
	public TableData<EntryPickData> qryEntryEventResult(@RequestParam int event) {
		return new TableData<>();
//		return this.myFplApi.qryEntryEventResult(event, entry);
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
	@ResponseBody
	@RequestMapping(value = "/qryTournamenList")
	public TableData<TournamentInfoData> qryTournamenList(@RequestBody TournamentQueryParam param, HttpSession session) {
		int entry;
		if (session.getAttribute("entry") != null) {
			entry = Integer.parseInt(session.getAttribute("entry").toString());
			param.setEntry(entry);
		}
		return this.myFplApi.qryTournamenList(param);
	}

	@RequestMapping("/qryLeagueReportStat")
	@ResponseBody
	public TableData<LeagueEventReportStatData> qryLeagueReportStat(@RequestParam int leagueId, @RequestParam String leagueType) {
		return this.myFplApi.qryLeagueReportStat(leagueId, leagueType);
	}

	@RequestMapping("/qryLeagueEventReportList")
	@ResponseBody
	public TableData<LeagueEventReportData> qryLeagueEventReportList(@RequestParam int leagueId, @RequestParam String leagueType, @RequestParam int event) {
		return this.myFplApi.qryLeagueEventReportList(leagueId, leagueType, event);
	}

	@RequestMapping("/qryEntryEventReportList")
	@ResponseBody
	public TableData<LeagueEventReportData> qryEntryEventReportList(@RequestParam int leagueId, @RequestParam String leagueType, @RequestParam int entry) {
		return this.myFplApi.qryEntryEventReportList(leagueId, leagueType, entry);
	}

}

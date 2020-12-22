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
	public String leagueCaptainReportController(@RequestParam int leagueId, @RequestParam String leagueType, Model model) {
		model.addAttribute("leagueName", this.myFplApi.qryLeagueNameByIdAndType(leagueId, leagueType));
		model.addAttribute("gwMap", CommonUtils.createCurrentGwMapForOption(this.httpApi.getCurrentEvent()));
		return "myFpl/leagueCaptainReport";
	}

	@GetMapping(value = "/leagueTransferReport")
	public String leagueTransferReportController(@RequestParam int leagueId, @RequestParam String leagueType, Model model) {
		model.addAttribute("leagueName", this.myFplApi.qryLeagueNameByIdAndType(leagueId, leagueType));
		model.addAttribute("gwMap", CommonUtils.createCurrentGwMapForOption(this.httpApi.getCurrentEvent()));
		return "myFpl/leagueTransferReport";
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
	public TableData<PlayerInfoData> qryPlayerDataList(@RequestParam int page, @RequestParam int limit) {
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

	/**
	 * @apiNote leagueCaptain
	 */
	@RequestMapping("/qryLeagueCaptainReportStat")
	@ResponseBody
	public TableData<LeagueEventReportStatData> qryLeagueCaptainReportStat(@RequestParam int leagueId, @RequestParam String leagueType) {
		return this.myFplApi.qryLeagueCaptainReportStat(leagueId, leagueType);
	}

	@RequestMapping("/qryLeagueCaptainEventReportList")
	@ResponseBody
	public TableData<LeagueEventReportData> qryLeagueCaptainEventReportList(@RequestParam int event, @RequestParam int leagueId, @RequestParam String leagueType) {
		return this.myFplApi.qryLeagueCaptainEventReportList(event, leagueId, leagueType);
	}

	@RequestMapping("/qryEntryCaptainEventReportList")
	@ResponseBody
	public TableData<LeagueEventReportData> qryEntryCaptainEventReportList(@RequestParam int leagueId, @RequestParam String leagueType, @RequestParam int entry) {
		return this.myFplApi.qryEntryCaptainEventReportList(leagueId, leagueType, entry);
	}

	/**
	 * @apiNote leagueTransfer
	 */
	@RequestMapping("/qryLeagueTransferReportStat")
	@ResponseBody
	public TableData<LeagueEventReportStatData> qryLeagueTransferReportStat(@RequestParam int leagueId, @RequestParam String leagueType) {
		return this.myFplApi.qryLeagueTransferReportStat(leagueId, leagueType);
	}

	@RequestMapping("/qryLeagueTransferEventReportList")
	@ResponseBody
	public TableData<LeagueEventReportData> qryLeagueTransferEventReportList(@RequestParam int event, @RequestParam int leagueId, @RequestParam String leagueType) {
		return this.myFplApi.qryLeagueTransferEventReportList(event, leagueId, leagueType);
	}

	@RequestMapping("/qryEntryTransferEventReportList")
	@ResponseBody
	public TableData<LeagueEventReportData> qryEntryTransferEventReportList(@RequestParam int leagueId, @RequestParam String leagueType, @RequestParam int entry) {
		return this.myFplApi.qryEntryTransferEventReportList(leagueId, leagueType, entry);
	}

}

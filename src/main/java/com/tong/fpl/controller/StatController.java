package com.tong.fpl.controller;

import com.tong.fpl.api.IStatApi;
import com.tong.fpl.domain.letletme.entry.EntryEventCaptainData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.table.TableData;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Create by tong on 2020/8/15
 */
@Controller
@RequestMapping(value = "/stat")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatController {

	private final IStatApi statApi;

	@RequestMapping(value = "/captain")
	public String captainStatController(Model model) {
		model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
		return "statCaptain";
	}

	@GetMapping("/qryEntryInfoByTournament")
	@ResponseBody
	public TableData<EntryInfoData> qryEntryInfoByTournament(@RequestParam String season, @RequestParam int tournamentId, @RequestParam long page, @RequestParam long limit) {
		return this.statApi.qryEntryInfoByTournament(season, tournamentId, page, limit);
	}

	@GetMapping("/qryTournamentCaptainList")
	@ResponseBody
	public TableData<EntryEventCaptainData> qryTournamentCaptainList(@RequestParam String season, @RequestParam int entry, @RequestParam int event) {
		return this.statApi.qryTournamentCaptainList(season, entry, event);
	}


}

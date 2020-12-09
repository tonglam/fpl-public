package com.tong.fpl.controller;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.api.IStatApi;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.domain.letletme.scout.ScoutPlayerData;
import com.tong.fpl.utils.CommonUtils;
import com.tong.fpl.utils.RedisUtils;
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

	@GetMapping(value = "/scout")
	public String scoutController(Model model, HttpSession session) {
		int next = this.httpApi.getNextEvent();
		int entry = 0;
		if (session.getAttribute("entry") != null) {
			entry = Integer.parseInt(session.getAttribute("entry").toString());
		}
		model.addAttribute("scoutEntryEventData", this.statApi.qryScoutEntryEventData(next, entry));
		model.addAttribute("nextGw", next);
		model.addAttribute("fund", RedisUtils.getValueByKey("scoutFund"));
		model.addAttribute("deadline", RedisUtils.getValueByKey("scoutDeadline"));
		model.addAttribute("scoutEntryList", RedisUtils.getHashByKey("scoutEntry").keySet());
		return "stat/scout";
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

	/**
	 * @apiNote scout
	 */
	@RequestMapping("/qryScoutPlayerList")
	@ResponseBody
	public TableData<ScoutPlayerData> qryScoutPlayerList(@RequestParam int elementType) {
		return this.statApi.qryScoutPlayerList(elementType);
	}

	@RequestMapping("/upsertEventScout")
	@ResponseBody
	public void upsertEventScout(@RequestBody ScoutData scoutData, HttpSession session) throws Exception {
		int entry = Integer.parseInt(session.getAttribute("entry").toString());
		scoutData
				.setEvent(this.httpApi.getNextEvent())
				.setEntry(entry)
				.setScoutName((String) RedisUtils.getHashByKey("scoutEntry").get(String.valueOf(entry)));
		this.statApi.upsertEventScout(scoutData);
	}

}

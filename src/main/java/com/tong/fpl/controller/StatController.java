package com.tong.fpl.controller;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.letletmeApi.IStatApi;
import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Create by tong on 2020/8/15
 */
@Controller
@RequestMapping(value = "/stat")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StatController {

	private final IStatApi statApi;

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
		int current = this.statApi.getCurrentEvent();
		model.addAttribute("currentGw", current);
		model.addAttribute("gwMap", CommonUtils.createCurrentGwMapForOption(current));
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
	public TableData<LeagueStatData> qryTeamSelectStatByName(@RequestParam int event, @RequestParam String leagueName) {
		return this.statApi.qryTeamSelectStatByName(event, leagueName);
	}

}

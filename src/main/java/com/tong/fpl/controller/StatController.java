package com.tong.fpl.controller;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.api.IStatApi;
import com.tong.fpl.domain.letletme.global.DropdownData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerShowData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
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
import java.util.Map;

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
		int current = this.httpApi.getCurrentEvent();
		model.addAttribute("currentGw", current);
		model.addAttribute("gwMap", CommonUtils.createCurrentGwMapForOption(current));
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
		model.addAttribute("deadline", this.statApi.getScoutDeadline(next));
		model.addAttribute("pickPlayerData", this.statApi.qryOffiaccountPickList());
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
	public TableData<LeagueStatData> qryTeamSelectStatByName(@RequestParam int event, @RequestParam String leagueName) {
		return this.statApi.qryTeamSelectStatByName(event, leagueName);
	}

	/**
	 * @apiNote scout
	 */
	@RequestMapping("/qryScoutPlayerList")
	@ResponseBody
	public TableData<PlayerShowData> qryScoutPlayerList(@RequestParam int elementType) {
		if (elementType == 0) {
			return new TableData<>();
		}
		return this.statApi.qryScoutPlayerList(elementType);
	}

	@RequestMapping("/upsertEventScout")
	@ResponseBody
	public String upsertEventScout(@RequestBody ScoutData scoutData, HttpSession session) throws Exception {
		int entry = Integer.parseInt(session.getAttribute("entry").toString());
		Map<Object, Object> map = RedisUtils.getHashByKey("scoutEntry");
		if (!map.containsKey(String.valueOf(entry))) {
			return "请先加入让让群球探！";
		}
		scoutData
				.setEvent(this.httpApi.getNextEvent())
				.setEntry(entry)
				.setScoutName((String) RedisUtils.getHashByKey("scoutEntry").get(String.valueOf(entry)));
		this.statApi.upsertEventScout(scoutData);
		return "提交成功";
	}

	@RequestMapping("/qryEventScoutPickList")
	@ResponseBody
	public TableData<ScoutData> qryEventScoutPickList(@RequestParam int event) {
		return this.statApi.qryEventScoutPickList(event);
	}

	@RequestMapping("/qryEventScoutList")
	@ResponseBody
	public TableData<ScoutData> qryEventScoutList(@RequestParam int event) {
		return this.statApi.qryEventScoutList(event);
	}

	@RequestMapping("/getScoutEventList")
	@ResponseBody
	public List<DropdownData> getScoutEvent() {
		return this.statApi.getScoutEvent();
	}

	@RequestMapping("/qryOffiaccountPlayerShowList")
	@ResponseBody
	public TableData<PlayerShowData> qryOffiaccountPlayerShowList(@RequestParam int event) {
		return this.statApi.qryOffiaccountPlayerShowList(event);
	}

	@RequestMapping("/qryPlayerDetailData")
	@ResponseBody
	public TableData<PlayerDetailData> qryPlayerDetailData(@RequestParam int element) {
		return this.statApi.qryPlayerDetailData(element);
	}

}

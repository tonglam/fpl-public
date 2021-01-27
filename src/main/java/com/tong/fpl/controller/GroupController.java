package com.tong.fpl.controller;

import com.google.common.collect.Lists;
import com.tong.fpl.api.IGroupApi;
import com.tong.fpl.domain.letletme.entry.EntryEventLineupData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.DropdownData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerPickData;
import com.tong.fpl.domain.letletme.player.PlayerShowData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/1/8
 */
@Controller
@RequestMapping(value = "/group")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GroupController {

	private final IGroupApi groupApi;

	@GetMapping(value = "/scout")
	public String scoutController(Model model, HttpSession session) {
		int next = this.groupApi.getNextEvent();
		model.addAttribute("nextGw", next);
		int entry = 0;
		if (session.getAttribute("entry") != null) {
			entry = Integer.parseInt(session.getAttribute("entry").toString());
		}
		model.addAttribute("scoutEntryEventData", this.groupApi.qryScoutEntryEventData(next, entry));
		model.addAttribute("deadline", this.groupApi.getScoutDeadline(next));
		model.addAttribute("fund", 28);
		return "group/scout";
	}

	@GetMapping(value = "/transfers")
	public String transfersController(Model model) {
		model.addAttribute("nextGw", this.groupApi.getNextEvent());
		model.addAttribute("pickPlayerData", this.groupApi.qryOffiaccountPickListForTransfers());
		model.addAttribute("lineupList", Lists.newArrayList());
		return "group/transfers";
	}

	@GetMapping(value = "/pick")
	public String pickController(Model model) {
		model.addAttribute("nextGw", this.groupApi.getNextEvent());
		model.addAttribute("pickPlayerData", this.groupApi.qryOffiaccountPickList());
		return "group/pick";
	}

	@RequestMapping(value = "/reloadLineup")
	public String reloadLineupController(@RequestBody PlayerPickData pickPlayerData, Model model) {
		model.addAttribute("pickPlayerData", pickPlayerData);
		return "group/transfers::lineup";
	}

	@GetMapping(value = "/reloadTransfers")
	public String reloadTransfersController(Model model) {
		model.addAttribute("lineupList", this.groupApi.qryOffiaccountLineupForTransfers());
		return "group/transfers::transfers";
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
		return this.groupApi.qryScoutPlayerList(elementType);
	}

	@RequestMapping("/upsertEventScout")
	@ResponseBody
	public String upsertEventScout(@RequestBody ScoutData scoutData, HttpSession session) {
		int entry = Integer.parseInt(session.getAttribute("entry").toString());
		Map<Object, Object> map = RedisUtils.getHashByKey("scoutEntry");
		if (!map.containsKey(String.valueOf(entry))) {
			return "请先加入让让群球探！";
		}
		scoutData
				.setEvent(this.groupApi.getNextEvent())
				.setEntry(entry)
				.setScoutName((String) RedisUtils.getHashByKey("scoutEntry").get(String.valueOf(entry)));
		this.groupApi.upsertEventScout(scoutData);
		return "提交成功";
	}

	@RequestMapping("/qryEventScoutPickList")
	@ResponseBody
	public TableData<ScoutData> qryEventScoutPickList(@RequestParam int event) {
		return this.groupApi.qryEventScoutPickList(event);
	}

	@RequestMapping("/qryEventScoutList")
	@ResponseBody
	public TableData<ScoutData> qryEventScoutList(@RequestParam int event) {
		return this.groupApi.qryEventScoutList(event);
	}

	@RequestMapping("/getScoutEventList")
	@ResponseBody
	public List<DropdownData> getScoutEvent() {
		return this.groupApi.getScoutEvent();
	}

	/**
	 * @apiNote pick
	 */
	@RequestMapping("/qryEntryEventPlayerShowList")
	@ResponseBody
	public TableData<PlayerShowData> qryEntryEventPlayerShowList(@RequestParam int event) {
		return this.groupApi.qryEntryEventPlayerShowList(event);
	}

	@RequestMapping("/qrySortedEntryEventPlayerShowList")
	@ResponseBody
	public TableData<PlayerShowData> qrySortedEntryEventPlayerShowList(@RequestBody List<PlayerShowData> playerShowDataList) {
		return this.groupApi.qrySortedEntryEventPlayerShowList(playerShowDataList);
	}

	/**
	 * @apiNote transfers
	 */
	@RequestMapping("/qryEntryEventPlayerShowListForTransfers")
	@ResponseBody
	public TableData<PlayerShowData> qryEntryEventPlayerShowListForTransfers(@RequestParam int event) {
		return this.groupApi.qryEntryEventPlayerShowListForTransfers(event);
	}

	@RequestMapping("/qryPlayerShowListByElementForTransfers")
	@ResponseBody
	public TableData<PlayerShowData> qryPlayerShowListByElementForTransfers(@RequestBody List<EntryPickData> pickList) {
		return this.groupApi.qryPlayerShowListByElementForTransfers(pickList);
	}

	@RequestMapping("/upsertEventTransfers")
	@ResponseBody
	public String upsertEventTransfers(@RequestBody EntryEventLineupData entryEventLineupData, HttpSession session) {
		int entry = Integer.parseInt(session.getAttribute("entry").toString());
		Map<Object, Object> map = RedisUtils.getHashByKey("scoutEntry");
		if (!map.containsKey(String.valueOf(entry))) {
			return "请先加入让让群球探！";
		}
		entryEventLineupData
				.setEntry(entry)
				.setEvent(this.groupApi.getCurrentEvent());
		this.groupApi.upsertEventTransfers(entryEventLineupData);
		return "提交成功";
	}

	/**
	 * @apiNote common
	 */
	@RequestMapping("/qryPlayerDetailData")
	@ResponseBody
	public TableData<PlayerDetailData> qryPlayerDetailData(@RequestParam int element) {
		return this.groupApi.qryPlayerDetailData(element);
	}

}

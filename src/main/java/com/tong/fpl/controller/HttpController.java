package com.tong.fpl.controller;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.api.ILiveCalcApi;
import com.tong.fpl.domain.data.letletme.EntryEventData;
import com.tong.fpl.domain.data.letletme.LiveCalaData;
import com.tong.fpl.domain.data.letletme.PlayerValueData;
import com.tong.fpl.domain.entity.EventLiveEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HttpController {

	private final IHttpApi httpApi;
	private final ILiveCalcApi liveCalcApi;

	@GetMapping("/insertplayerValue")
	@ResponseBody
	public String insertPlayerValue() {
		try {
			this.httpApi.insertPlayerValue();
			return "success!";
		} catch (Exception e) {
			return "insert player value error: " + e.getMessage();
		}
	}

	@GetMapping("/qryDayChangePlayerValue")
	@ResponseBody
	public List<PlayerValueData> qryDayChangePlayerValue(@RequestParam String changeDate) {
		return this.httpApi.qryDayChangePlayerValue(changeDate);
	}

	@GetMapping("/qryEntryResult")
	@ResponseBody
	public EntryEventData qryEntryResult(@RequestParam int entry) {
		return this.httpApi.qryEntryResult(entry);
	}

	@GetMapping("/qryEntryEventResult")
	@ResponseBody
	public EntryEventData qryEntryEventResult(@RequestParam int event, @RequestParam int entry) {
		return this.httpApi.qryEntryEventResult(event, entry);
	}

	@GetMapping("/qryLivePointsByEntry")
	@ResponseBody
	public LiveCalaData qryLivePointsByEntry(@RequestParam int event, @RequestParam int entry) {
		return this.liveCalcApi.calcLivePointsByEntry(event, entry);
	}

	@GetMapping("/qryLivePointsByElementList")
	@ResponseBody
	public LiveCalaData qryLivePointsByElementList(int event, Map<Integer, Integer> elementMap, int captain, int viceCaptain) {
		return liveCalcApi.calcLivePointsByElementList(event, elementMap, captain, viceCaptain);
	}

	@GetMapping("/qryEventLive")
	@ResponseBody
	public List<EventLiveEntity> qryEventLive(@RequestParam int element) {
		return this.httpApi.qryEventLive(element);
	}

}

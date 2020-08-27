package com.tong.fpl.controller;

import com.tong.fpl.aop.annotation.TraceHttpCall;
import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.api.ILiveCalcApi;
import com.tong.fpl.domain.data.letletme.api.EntryEventData;
import com.tong.fpl.domain.data.letletme.api.LiveCalaData;
import com.tong.fpl.domain.data.letletme.player.PlayerData;
import com.tong.fpl.domain.data.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.data.letletme.player.PlayerQueryParam;
import com.tong.fpl.domain.entity.EventLiveEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/6/23
 */
@Validated
@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HttpController {

	private final IHttpApi httpApi;
	private final ILiveCalcApi liveCalcApi;

	@TraceHttpCall
	@GetMapping("/insertplayerValue")
	@ResponseBody
	public void insertPlayerValue() {
		this.httpApi.insertPlayerValue();
	}

	@TraceHttpCall
	@GetMapping("/qryEntryResult")
	@ResponseBody
	public EntryEventData qryEntryResult(@RequestParam String season, @RequestParam int entry) {
		return this.httpApi.qryEntryResult(season, entry);
	}

	@TraceHttpCall
	@GetMapping("/qryEntryEventResult")
	@ResponseBody
	public EntryEventData qryEntryEventResult(@RequestParam String season, @RequestParam int event, @RequestParam int entry) {
		return this.httpApi.qryEntryEventResult(season, event, entry);
	}

	@TraceHttpCall
	@GetMapping("/qryLivePointsByEntry")
	@ResponseBody
	public LiveCalaData qryLivePointsByEntry(@RequestParam int event, @RequestParam int entry) {
		return this.liveCalcApi.calcLivePointsByEntry(event, entry);
	}

	@TraceHttpCall
	@GetMapping("/qryLivePointsByElementList")
	@ResponseBody
	@Cacheable(value = "qryLivePointsByElementList")
	public LiveCalaData qryLivePointsByElementList(@RequestParam int event, @RequestParam Map<Integer, Integer> elementMap, @RequestParam int captain, @RequestParam int viceCaptain) {
		return liveCalcApi.calcLivePointsByElementList(event, elementMap, captain, viceCaptain);
	}

	@TraceHttpCall
	@GetMapping("/qryEventLiveAll")
	@ResponseBody
	@Cacheable(value = "qryEventLiveAll")
	public List<EventLiveEntity> qryEventLiveAll(@RequestParam String season, @RequestParam int element) {
		return this.httpApi.qryEventLiveAll(season, element);
	}

	@TraceHttpCall
	@GetMapping("/qryEventLive")
	@ResponseBody
	@Cacheable(value = "qryEventLive")
	public List<EventLiveEntity> qryEventLive(@RequestParam String season, @RequestParam int event, @RequestParam int element) {
		return this.httpApi.qryEventLive(season, event, element);
	}

	@TraceHttpCall
	@GetMapping("/qryPlayerData")
	@ResponseBody
	public PlayerData qryPlayerData(@RequestBody PlayerQueryParam queryParam) throws Exception {
		return this.httpApi.qryPlayerData(queryParam);
	}

	@TraceHttpCall
	@GetMapping("/qryAllPlayers")
	@ResponseBody
	public List<PlayerInfoData> qryAllPlayers(@RequestParam String season) {
		return this.httpApi.qryAllPlayers(season);
	}

}

package com.tong.fpl.controller;

import com.tong.fpl.aop.annotation.TraceHttpCall;
import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.letletme.entry.EntryEventData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerQueryParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Create by tong on 2020/6/23
 */
@Validated
@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HttpController {

	private final IHttpApi httpApi;

	@TraceHttpCall
	@RequestMapping("/qryEntryResult")
	@ResponseBody
	public EntryEventData qryEntryResult(@RequestParam String season, @RequestParam int entry) {
		return this.httpApi.qryEntryResult(season, entry);
	}

	@TraceHttpCall
	@RequestMapping("/qryEntryEventResult")
	@ResponseBody
	public EntryEventData qryEntryEventResult(@RequestParam String season, @RequestParam int event, @RequestParam int entry) {
		return this.httpApi.qryEntryEventResult(season, event, entry);
	}

	@TraceHttpCall
	@RequestMapping("/qryEventLiveAll")
	@ResponseBody
	public List<EventLiveEntity> qryEventLiveAll(@RequestParam String season, @RequestParam int element) {
		return this.httpApi.qryEventLiveAll(season, element);
	}

	@TraceHttpCall
	@RequestMapping("/qryEventLive")
	@ResponseBody
	public EventLiveEntity qryEventLive(@RequestParam String season, @RequestParam int event, @RequestParam int element) {
		return this.httpApi.qryEventLive(season, event, element);
	}

	@TraceHttpCall
	@RequestMapping("/qryPlayerData")
	@ResponseBody
	public PlayerData qryPlayerData(@RequestBody PlayerQueryParam queryParam) throws Exception {
		return this.httpApi.qryPlayerData(queryParam);
	}

	@TraceHttpCall
	@RequestMapping("/qryAllPlayers")
	@ResponseBody
	public List<PlayerInfoData> qryAllPlayers(@RequestParam String season) {
		return this.httpApi.qryAllPlayers(season);
	}

}

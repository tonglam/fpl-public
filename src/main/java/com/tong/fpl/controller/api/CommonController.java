package com.tong.fpl.controller.api;

import com.tong.fpl.api.ICommonApi;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Create by tong on 2021/2/26
 */
@RestController
@RequestMapping("/common")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommonController {

	private final ICommonApi commonApi;

	@GetMapping("/getCurrentEvent")
	public int getCurrentEvent() {
		return this.commonApi.getCurrentEvent();
	}

	@GetMapping("/getNextEvent")
	public int getNextEvent() {
		return this.commonApi.getNextEvent();
	}

	@GetMapping("/getUtcDeadlineByEvent")
	public String getUtcDeadlineByEvent(@RequestParam int event) {
		return this.commonApi.getUtcDeadlineByEvent(event);
	}

	@GetMapping("/qryEntryInfoData")
	public EntryInfoData qryEntryInfoData(@RequestParam int entry) {
		return this.commonApi.qryEntryInfoData(entry);
	}

	@GetMapping("/qryPlayerInfoByElementType")
	public Map<String, PlayerInfoData> qryPlayerInfoByElementType(@RequestParam int elementType) {
		return this.commonApi.qryPlayerInfoByElementType(elementType);
	}

}

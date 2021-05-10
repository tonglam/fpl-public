package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiLive;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Create by tong on 2021/5/9
 */
@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveApiController {

	private final IApiLive apiLive;

	@GetMapping("/qryLiveMatchDataByStatus")
	public List<LiveMatchData> qryLiveMatchDataByStatus(@RequestParam String playStatus) {
		return this.apiLive.qryLiveMatchDataByStatus(playStatus);
	}

}

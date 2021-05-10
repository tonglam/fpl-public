package com.tong.fpl.controller.api;

import com.tong.fpl.api.ILiveApi;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.domain.letletme.live.LiveMatchTeamData;
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

	private final ILiveApi liveApi;

	@GetMapping("/qryLiveFixtureByStatus")
	public List<LiveMatchData> qryLiveFixtureByStatus(@RequestParam String playStatus) {
		return this.liveApi.qryLiveFixtureByStatus(playStatus);
	}

	@GetMapping("/qryLiveMatchDataByStatus")
	public List<LiveMatchTeamData> qryLiveMatchDataByStatus(@RequestParam String playStatus) {
		return this.liveApi.qryLiveMatchDataByStatus(playStatus);
	}

}

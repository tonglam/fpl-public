package com.tong.fpl.controller;

import cn.hutool.core.codec.Base64;
import com.tong.fpl.aop.annotation.TraceHttpCall;
import com.tong.fpl.api.ICacheApi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Create by tong on 2020/8/24
 */
@RestController
@RequestMapping(value = "/cache")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CacheController {

	private final String acessToken = Base64.encode("letletguanlaoshi");
	private final ICacheApi cacheApi;

	@TraceHttpCall
	@GetMapping("/insertTeam")
	@ResponseBody
	public void insertTeam(@RequestParam String token) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertTeam();
	}

	@TraceHttpCall
	@GetMapping("/insertHisTeam")
	@ResponseBody
	public void insertHisTeam(@RequestParam String token, @RequestParam String season) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertHisTeam(season);
	}

	@TraceHttpCall
	@GetMapping("/insertEvent")
	@ResponseBody
	public void insertEvent(@RequestParam String token) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertEvent();
	}

	@TraceHttpCall
	@GetMapping("/insertHisEvent")
	@ResponseBody
	public void insertHisEvent(@RequestParam String token, @RequestParam String season) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertHisEvent(season);
	}

	@TraceHttpCall
	@GetMapping("/insertEventFixture")
	@ResponseBody
	public void insertEventFixture(@RequestParam String token) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertEventFixture();
	}

	@TraceHttpCall
	@GetMapping("/insertHisEventFixture")
	@ResponseBody
	public void insertHisEventFixture(@RequestParam String token, @RequestParam String season) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertHisEventFixture(season);
	}

	@TraceHttpCall
	@GetMapping("/insertSingleEventFixture")
	@ResponseBody
	public void insertSingleEventFixture(@RequestParam String token, @RequestParam int event) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertSingleEventFixture(event);
	}

	@TraceHttpCall
	@GetMapping("/insertPlayer")
	@ResponseBody
	public void insertPlayer(@RequestParam String token) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertPlayer();
	}

	@TraceHttpCall
	@GetMapping("/insertHisPlayer")
	@ResponseBody
	public void insertHisPlayer(@RequestParam String token, @RequestParam String season) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertHisPlayer(season);
	}

	@TraceHttpCall
	@GetMapping("/insertPlayerStat")
	@ResponseBody
	public void insertPlayerStat(@RequestParam String token) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertPlayerStat();
	}

	@TraceHttpCall
	@GetMapping("/insertHisPlayerStat")
	@ResponseBody
	public void insertHisPlayerStat(@RequestParam String token, @RequestParam String season) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertHisPlayerStat(season);
	}

	@TraceHttpCall
	@GetMapping("/insertplayerValue")
	@ResponseBody
	public void insertPlayerValue(@RequestParam String token) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertPlayerValue();
	}

	@TraceHttpCall
	@GetMapping("/insertEventLive")
	@ResponseBody
	public void insertEventLive(@RequestParam String token, @RequestParam int event) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.insertEventLive(event);
	}

	@TraceHttpCall
	@GetMapping("/deleteKeys")
	@ResponseBody
	public void deleteKeys(@RequestParam String token, @RequestParam String pattern) {
		if (this.checkToken(token)) {
			return;
		}
		this.cacheApi.deleteKeys(pattern);
	}

	private boolean checkToken(String token) {
		return !StringUtils.equals(token, acessToken);
	}


}

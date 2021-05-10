package com.tong.fpl.controller.api;

import com.tong.fpl.api.IScoutApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Create by tong on 2021/5/9
 */
@RestController
@RequestMapping("/api/scout")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScoutApiController {

	private final IScoutApi scoutApi;

	@GetMapping("/qryScoutEntry")
	public Map<String, String> qryScoutEntry() {
		return this.scoutApi.qryScoutEntry();
	}

}

package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiScout;
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
public class ApiScoutController {

	private final IApiScout apiScout;

	@GetMapping("/getScoutMap")
	public Map<Object, Object> getScoutMap() {
		return this.apiScout.getScoutMap();
	}

}

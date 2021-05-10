package com.tong.fpl.controller.api;

import com.tong.fpl.api.ICommonApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Create by tong on 2021/2/26
 */
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CommonApiController {

	private final ICommonApi commonApi;

	@GetMapping("/qryCurrentEventAndNextUtcDeadline")
	public Map<String, String> qryCurrentEventAndNextUtcDeadline() {
		return this.commonApi.qryCurrentEventAndNextUtcDeadline();
	}

}

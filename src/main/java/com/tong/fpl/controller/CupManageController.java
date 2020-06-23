package com.tong.fpl.controller;

import com.tong.fpl.data.CupCreateData;
import com.tong.fpl.service.CreateNewCupsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Create by tong on 2020/6/23
 */
@Validated
@Controller
@RequestMapping(value = "/cup")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CupManageController {

	private final CreateNewCupsService createNewCupsService;

	@RequestMapping(value = {"", "/"})
	public String cupManageController() {
		return "cup";
	}

	@ResponseBody
	@PostMapping(value = {"/createNewCup"})
	public String createNewCup(@RequestBody CupCreateData cupCreateData) {
		return this.createNewCupsService.createNewCup(cupCreateData.getUrl(),
				cupCreateData.getCupName(), cupCreateData.getCreator(),
				cupCreateData.getStartGw(), cupCreateData.getEndGw(),
				cupCreateData.getTeamsPerGroup(), cupCreateData.getQualifiers(), cupCreateData.isFillAverage());
	}

}

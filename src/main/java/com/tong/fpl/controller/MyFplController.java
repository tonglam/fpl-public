package com.tong.fpl.controller;

import com.tong.fpl.api.IMyFplApi;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Create by tong on 2020/6/23
 */
@Controller
@RequestMapping(value = "/my_fpl")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyFplController {

	private final IMyFplApi myFplApi;

	@RequestMapping(value = "/pick")
	public String pickController() {
		return "pick";
	}

	@GetMapping("/qryPlayerDataList")
	@ResponseBody
	public TableData<PlayerInfoData> qryPlayerDataList(@RequestParam long page, @RequestParam long limit) {
		return this.myFplApi.qryPlayerDataList(page, limit);
	}

}

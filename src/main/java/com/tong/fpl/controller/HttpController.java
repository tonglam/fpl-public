package com.tong.fpl.controller;

import com.tong.fpl.domain.entity.EntryEventResultEntity;
import com.tong.fpl.service.IQuerySerivce;
import com.tong.fpl.service.IStaticSerive;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Validated
@Controller
@RequestMapping(value = "/api")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HttpController {

	private final IQuerySerivce querySerivce;
	private final IStaticSerive staticSerive;

	@GetMapping("/insertplayerValue")
	@ResponseBody
	public String insertPlayerValue() {
		try {
			this.staticSerive.insertPlayerValue();
			return "success!";
		} catch (Exception e) {
			return "insert player value error: " + e.getMessage();
		}
	}

	@GetMapping("/qryEntryEventResult")
	@ResponseBody
	public EntryEventResultEntity qryEntryEventResult(@RequestBody int event, @RequestBody int entry) {
		return this.querySerivce.qryEntryEvent(event, entry);
	}

}

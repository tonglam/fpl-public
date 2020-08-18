package com.tong.fpl.controller;

import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Create by tong on 2020/8/15
 */
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebPageController {

	@RequestMapping(value = {"", "/"})
	public String indexController(Model model) {
		int currentEvent = CommonUtils.getNowEvent();
		model.addAttribute("nextGw", currentEvent);
		model.addAttribute("deadline", CommonUtils.getDeadlineTime(currentEvent));
		return "index";
	}

	@RequestMapping(value = "/404")
	public String errorController() {
		return "error";
	}

	@RequestMapping(value = "/test")
	public String testController() {
		return "test";
	}

	@GetMapping(value = "/saveEntry")
	@ResponseBody
	public void saveEntry(@RequestParam int entry, HttpSession session) {
		session.setAttribute("entry", entry);
	}

}

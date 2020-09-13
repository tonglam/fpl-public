package com.tong.fpl.controller;

import com.tong.fpl.api.IHttpApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    private final IHttpApi httpApi;

    @RequestMapping(value = {"", "/"})
    public String indexController(Model model) {
        int nextEvent = this.httpApi.getCurrentEvent();
        model.addAttribute("nextGw", nextEvent);
        model.addAttribute("deadline", this.httpApi.qryDeadlineByEvent(nextEvent));
        return "index";
    }

    @RequestMapping(value = "/404")
    public String errorController() {
        return "error";
    }

    @RequestMapping(value = "/test")
    public String testController(Model model) {
        return "test";
    }

    @RequestMapping(value = "/saveEntry")
    @ResponseBody
    public void saveEntry(@RequestParam int entry, HttpSession session) {
        session.setAttribute("entry", entry);
    }

}

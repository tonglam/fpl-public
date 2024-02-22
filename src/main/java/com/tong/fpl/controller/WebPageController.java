package com.tong.fpl.controller;

import com.tong.fpl.letletmeApi.IHttpApi;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Create by tong on 2020/8/15
 */
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebPageController {

    private final IHttpApi httpApi;

    @GetMapping(value = {"", "/"})
    public String indexController(Model model) {
        int nextEvent = this.httpApi.getNextEvent();
        model.addAttribute("nextGw", nextEvent);
        model.addAttribute("deadline", this.httpApi.getUtcDeadlineByEvent(nextEvent));
        return "web/index";
    }

    @GetMapping(value = "/404")
    public String errorController() {
        return "web/error";
    }

    @RequestMapping(value = "/saveSession")
    @ResponseBody
    public void saveEntry(@RequestParam String key, @RequestParam Object value, HttpSession session) {
        session.setAttribute(key, value);
    }

}

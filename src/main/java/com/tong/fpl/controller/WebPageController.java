package com.tong.fpl.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Create by tong on 2020/8/15
 */
@Controller
public class WebPageController {

    @RequestMapping(value = {"", "/"})
    public String indexController(Model model) {
        model.addAttribute("title", "letletme");
        return "index";
    }

    @RequestMapping(value = "/404")
    public String errorController(Model model) {
        model.addAttribute("title", "letletme");
        return "error";
    }

    @RequestMapping(value = "/test")
    public String testController(Model model) {
        model.addAttribute("title", "test-letletme");
        return "test";
    }

}

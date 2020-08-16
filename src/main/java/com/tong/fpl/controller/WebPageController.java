package com.tong.fpl.controller;

import com.tong.fpl.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Create by tong on 2020/8/15
 */
@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebPageController {

    @RequestMapping(value = {"", "/"})
    public String indexController(Model model) {
        model.addAttribute("title", "letletme");
        model.addAttribute("entry", "请输入id");
        model.addAttribute("nextGw", CommonUtils.getNowEvent());
        model.addAttribute("deadline", "2020-09-12 20:30:00");
        return "index";
    }

    @RequestMapping(value = "/404")
    public String errorController(Model model) {
        model.addAttribute("title", "出错啦-letletme");
        model.addAttribute("entry", "9999999");
        return "error";
    }

    @RequestMapping(value = "/test")
    public String testController(Model model) {
        model.addAttribute("title", "test-letletme");
        model.addAttribute("entry", "9999999");
        return "test";
    }

}

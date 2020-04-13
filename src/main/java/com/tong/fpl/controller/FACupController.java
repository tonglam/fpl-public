package com.tong.fpl.controller;

import com.tong.fpl.domain.db.FACup;
import com.tong.fpl.service.FAservice;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Create by tong on 2020/3/9
 */
@Controller
@RequestMapping(value = "/FA")
public class FACupController {

    private final FAservice fAservice;

    public FACupController(FAservice fAservice) {
        this.fAservice = fAservice;
    }

    @GetMapping(value = "/against")
    public String againstController2() {
        return "FA";
    }

    @ResponseBody
    @GetMapping(value = "/getResult")
    public List<FACup> getResult() {
        return fAservice.getAgainst();
    }

}

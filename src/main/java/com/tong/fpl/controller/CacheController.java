package com.tong.fpl.controller;

import cn.hutool.core.codec.Base64;
import com.tong.fpl.aop.annotation.TraceHttpCall;
import com.tong.fpl.letletmeApi.ICacheApi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by tong on 2020/8/24
 */
@RestController
@RequestMapping(value = "/cache")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CacheController {

    private final String accessToken = Base64.encode("letletguanlaoshi");
    private final ICacheApi cacheApi;

    @TraceHttpCall
    @RequestMapping("/insertTeam")
    @ResponseBody
    public void insertTeam(@RequestParam String token) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertTeam();
    }

    @TraceHttpCall
    @RequestMapping("/insertEvent")
    @ResponseBody
    public void insertEvent(@RequestParam String token) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertEvent();
    }

    @TraceHttpCall
    @RequestMapping("/insertEventFixture")
    @ResponseBody
    public void insertEventFixture(@RequestParam String token) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertEventFixture();
    }

    @TraceHttpCall
    @RequestMapping("/insertSingleEventFixture")
    @ResponseBody
    public void insertSingleEventFixture(@RequestParam String token, @RequestParam int event) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertSingleEventFixture(event);
    }

    @TraceHttpCall
    @RequestMapping("/insertPlayer")
    @ResponseBody
    public void insertPlayer(@RequestParam String token) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertPlayer();
    }

    @TraceHttpCall
    @RequestMapping("/insertPlayerStat")
    @ResponseBody
    public void insertPlayerStat(@RequestParam String token) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertPlayerStat();
    }

    @TraceHttpCall
    @RequestMapping("/insertPlayerValue")
    @ResponseBody
    public void insertPlayerValue(@RequestParam String token) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertPlayerValue();
    }

    @TraceHttpCall
    @RequestMapping("/insertEventLive")
    @ResponseBody
    public void insertEventLive(@RequestParam String token, @RequestParam int event) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertEventLive(event);
    }

    @TraceHttpCall
    @RequestMapping("/deleteKeys")
    @ResponseBody
    public void deleteKeys(@RequestParam String token, @RequestParam String pattern) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.deleteKeys(pattern);
    }

    private boolean checkToken(String token) {
        return !StringUtils.equals(token, this.accessToken);
    }

}

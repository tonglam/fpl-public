package com.tong.fpl.controller;

import cn.hutool.core.codec.Base64;
import com.tong.fpl.aop.annotation.TraceHttpCall;
import com.tong.fpl.api.ICacheApi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Create by tong on 2020/8/24
 */
@RestController
@RequestMapping(value = "/cache")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CacheController {

    private final String acessToken = Base64.encode("letletguanlaoshi");
    private final ICacheApi cacheApi;

    @TraceHttpCall
    @GetMapping("/insertTeam")
    @ResponseBody
    public void insertTeam(String token) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertTeam();
    }

    @TraceHttpCall
    @GetMapping("/insertHisTeam")
    @ResponseBody
    public void insertHisTeam(String token, String season) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertHisTeam(season);
    }

    @TraceHttpCall
    @GetMapping("/insertEvent")
    @ResponseBody
    public void insertEvent(String token) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertEvent();
    }

    @TraceHttpCall
    @GetMapping("/insertHisEvent")
    @ResponseBody
    public void insertHisEvent(String token, String season) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertHisEvent(season);
    }

    @TraceHttpCall
    @GetMapping("/insertEventFixture")
    @ResponseBody
    public void insertEventFixture(String token) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertEventFixture();
    }

    @TraceHttpCall
    @GetMapping("/insertHisEventFixture")
    @ResponseBody
    public void insertHisEventFixture(String token, String season) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertHisEventFixture(season);
    }

    @TraceHttpCall
    @GetMapping("/insertPlayer")
    @ResponseBody
    public void insertPlayer(String token) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertPlayer();
    }

    @TraceHttpCall
    @GetMapping("/insertHisPlayer")
    @ResponseBody
    public void insertHisPlayer(String token, String season) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertHisPlayer(season);
    }

    @TraceHttpCall
    @GetMapping("/insertPlayerStat")
    @ResponseBody
    public void insertPlayerStat(String token) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertPlayerStat();
    }

    @TraceHttpCall
    @GetMapping("/insertHisPlayerStat")
    @ResponseBody
    public void insertHisPlayerStat(String token, String season) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertHisPlayerStat(season);
    }

    @TraceHttpCall
    @GetMapping("/insertplayerValue")
    @ResponseBody
    public void insertPlayerValue(String token) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertPlayerValue();
    }

    @TraceHttpCall
    @GetMapping("/insertEventLive")
    @ResponseBody
    public void insertEventLive(String token, @RequestParam int event) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.insertEventLive(event);
    }

    @TraceHttpCall
    @GetMapping("/deleteKeys")
    @ResponseBody
    public void deleteKeys(String token, @RequestParam String pattern) {
        if (this.checkToken(token)) {
            return;
        }
        this.cacheApi.deleteKeys(pattern);
    }

    private boolean checkToken(String token) {
        return !StringUtils.equals(token, acessToken);
    }


}

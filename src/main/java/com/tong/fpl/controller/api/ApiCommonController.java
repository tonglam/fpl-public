package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiCommon;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by tong on 2021/2/26
 */
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiCommonController {

    private final IApiCommon commonApi;

    @GetMapping("/getCurrentEvent")
    public int getCurrentEvent() {
        return this.commonApi.getCurrentEvent();
    }

    @GetMapping("/getNextEvent")
    public int getNextEvent() {
        return this.commonApi.getNextEvent();
    }

    @GetMapping("/getUtcDeadlineByEvent")
    public String getUtcDeadlineByEvent(@RequestParam int event) {
        return this.commonApi.getUtcDeadlineByEvent(event);
    }

}

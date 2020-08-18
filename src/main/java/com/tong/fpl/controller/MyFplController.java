package com.tong.fpl.controller;

import com.tong.fpl.api.IMyFplApi;
import com.tong.fpl.domain.data.letletme.PlayerData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Create by tong on 2020/6/23
 */
@Controller
@RequestMapping(value = "/my_fpl")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyFplController {

    private final IMyFplApi myFplApi;

    @RequestMapping(value = "/pick")
    public String pickController(Model model) {
        return "pick";
    }

    @GetMapping("/qryPlayerDataList")
    @ResponseBody
    public List<PlayerData> qryPlayerDataList(long current, long size) {
        return this.myFplApi.qryPlayerDataList(current, size);
    }


}

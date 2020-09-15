package com.tong.fpl.controller;

import com.tong.fpl.api.IMyFplApi;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
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
 * Create by tong on 2020/6/23
 */
@Controller
@RequestMapping(value = "/my_fpl")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyFplController {

    private final IMyFplApi myFplApi;

    @RequestMapping(value = "/pick")
    public String pickController() {
        return "myFpl/pick";
    }

    @RequestMapping(value = "/entry")
    public String entryController(Model model, HttpSession session) {
        int entry = 0;
        if (session.getAttribute("entry") != null) {
            entry = (int) session.getAttribute("entry");
        }
        model.addAttribute("entryInfo", this.myFplApi.qryEntryInfo(entry));
        return "myFpl/entry";
    }

    @RequestMapping(value = "/league")
    public String leagueController() {
        return "myFpl/league";
    }

    @GetMapping("/qryEntryResultList")
    @ResponseBody
    public TableData<EntryEventResultData> qryEntryResultList(HttpSession session) {
        int entry = 0;
        if (session.getAttribute("entry") != null) {
            entry = (int) session.getAttribute("entry");
        }
        return this.myFplApi.qryEntryResultList(entry);
    }

    @GetMapping("/qryEntryEventResult")
    @ResponseBody
    public TableData<EntryPickData> qryEntryEventResult(@RequestParam int event, HttpSession session) {
        int entry = 0;
        if (session.getAttribute("entry") != null) {
            entry = (int) session.getAttribute("entry");
        }
        return this.myFplApi.qryEntryEventResult(event, entry);
    }

    @GetMapping("/qryPlayerDataList")
    @ResponseBody
    public TableData<PlayerInfoData> qryPlayerDataList(@RequestParam long page, @RequestParam long limit) {
        return this.myFplApi.qryPlayerDataList(page, limit);
    }

}

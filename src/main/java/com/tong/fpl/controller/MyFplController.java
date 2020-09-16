package com.tong.fpl.controller;

import com.tong.fpl.api.IHttpApi;
import com.tong.fpl.api.IMyFplApi;
import com.tong.fpl.domain.letletme.element.ElementEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.utils.CommonUtils;
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
    private final IHttpApi httpApi;

    @RequestMapping(value = "/pick")
    public String pickController() {
        return "myFpl/pick";
    }

    @RequestMapping(value = "/entry")
    public String entryController(Model model, HttpSession session) {
        int entry = this.getQryEntry(session);
        model.addAttribute("entryInfo", this.myFplApi.qryEntryInfo(entry));
        return "myFpl/entry";
    }

    @RequestMapping(value = "/league")
    public String leagueController(Model model) {
        model.addAttribute("currentGw", this.httpApi.getCurrentEvent());
        model.addAttribute("gwMap", CommonUtils.createGwMapForOption());
        return "myFpl/league";
    }

    @GetMapping("/qryEntryResultList")
    @ResponseBody
    public TableData<EntryEventResultData> qryEntryResultList(HttpSession session) {
        int entry = this.getQryEntry(session);
        if (entry == 0) {
            return new TableData<>();
        }
        return this.myFplApi.qryEntryResultList(entry);
    }

    @GetMapping("/qryEntryEventResult")
    @ResponseBody
    public TableData<EntryPickData> qryEntryEventResult(@RequestParam int event, HttpSession session) {
        int entry = this.getQryEntry(session);
        return this.myFplApi.qryEntryEventResult(event, entry);
    }

    @GetMapping("/qryElementEventResult")
    @ResponseBody
    public TableData<ElementEventResultData> qryElementEventResult(@RequestParam int event, @RequestParam int element) {
        return this.myFplApi.qryElementEventResult(event, element);
    }

    @RequestMapping(value = "/saveMyFplEntry")
    @ResponseBody
    public void saveMyFplEntry(@RequestParam int myFplEntry, HttpSession session) {
        session.setAttribute("myFplEntry", myFplEntry);
    }

    @GetMapping("/qryPlayerDataList")
    @ResponseBody
    public TableData<PlayerInfoData> qryPlayerDataList(@RequestParam long page, @RequestParam long limit) {
        return this.myFplApi.qryPlayerDataList(page, limit);
    }

    private int getQryEntry(HttpSession session) {
        int entry = 0;
        if (session.getAttribute("myFplEntry") != null) {
            entry = (int) session.getAttribute("myFplEntry");
        } else if (session.getAttribute("entry") != null) {
            entry = (int) session.getAttribute("entry");
        }
        return entry;
    }

}

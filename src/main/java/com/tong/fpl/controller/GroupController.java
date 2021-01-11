package com.tong.fpl.controller;

import com.tong.fpl.api.IGroupApi;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.DropdownData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerPickData;
import com.tong.fpl.domain.letletme.player.PlayerShowData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/1/8
 */
@Controller
@RequestMapping(value = "/group")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GroupController {

    private final IGroupApi groupApi;

    @GetMapping(value = "/scout")
    public String scoutController(Model model, HttpSession session) {
        int next = this.groupApi.getNextEvent();
        model.addAttribute("nextGw", next);
        int entry = 0;
        if (session.getAttribute("entry") != null) {
            entry = Integer.parseInt(session.getAttribute("entry").toString());
        }
        model.addAttribute("scoutEntryEventData", this.groupApi.qryScoutEntryEventData(next, entry));
        model.addAttribute("deadline", this.groupApi.getScoutDeadline(next));
        model.addAttribute("fund", 28);
        return "group/scout";
    }

    @GetMapping(value = "/transfers")
    public String transfersController(Model model) {
        model.addAttribute("nextGw", this.groupApi.getNextEvent());
        model.addAttribute("pickPlayerData", this.groupApi.qryOffiaccountPickList());
        return "group/transfers";
    }

    @RequestMapping(value = "/reloadLineup")
    public String reloadLineupController(@RequestBody PlayerPickData pickPlayerData, Model model) {
        model.addAttribute("pickPlayerData", pickPlayerData);
        return "group/transfers::lineup";
    }

    /**
     * @apiNote scout
     */
    @RequestMapping("/qryScoutPlayerList")
    @ResponseBody
    public TableData<PlayerShowData> qryScoutPlayerList(@RequestParam int elementType) {
        if (elementType == 0) {
            return new TableData<>();
        }
        return this.groupApi.qryScoutPlayerList(elementType);
    }

    @RequestMapping("/upsertEventScout")
    @ResponseBody
    public String upsertEventScout(@RequestBody ScoutData scoutData, HttpSession session) throws Exception {
        int entry = Integer.parseInt(session.getAttribute("entry").toString());
        Map<Object, Object> map = RedisUtils.getHashByKey("scoutEntry");
        if (!map.containsKey(String.valueOf(entry))) {
            return "请先加入让让群球探！";
        }
        scoutData
                .setEvent(this.groupApi.getNextEvent())
                .setEntry(entry)
                .setScoutName((String) RedisUtils.getHashByKey("scoutEntry").get(String.valueOf(entry)));
        this.groupApi.upsertEventScout(scoutData);
        return "提交成功";
    }

    @RequestMapping("/qryEventScoutPickList")
    @ResponseBody
    public TableData<ScoutData> qryEventScoutPickList(@RequestParam int event) {
        return this.groupApi.qryEventScoutPickList(event);
    }

    @RequestMapping("/qryEventScoutList")
    @ResponseBody
    public TableData<ScoutData> qryEventScoutList(@RequestParam int event) {
        return this.groupApi.qryEventScoutList(event);
    }

    @RequestMapping("/getScoutEventList")
    @ResponseBody
    public List<DropdownData> getScoutEvent() {
        return this.groupApi.getScoutEvent();
    }

    /**
     * @apiNote transfers
     */
    @RequestMapping("/qryOffiaccountPlayerShowList")
    @ResponseBody
    public TableData<PlayerShowData> qryOffiaccountPlayerShowList(@RequestParam int event) {
        return this.groupApi.qryOffiaccountPlayerShowList(event);
    }

    @RequestMapping("/qryPlayerShowListByElement")
    @ResponseBody
    public TableData<PlayerShowData> qryPlayerShowListByElement(@RequestBody List<EntryPickData> pickList) {
        return this.groupApi.qryPlayerShowListByElement(pickList);
    }

    /**
     * @apiNote common
     */
    @RequestMapping("/qryPlayerDetailData")
    @ResponseBody
    public TableData<PlayerDetailData> qryPlayerDetailData(@RequestParam int element) {
        return this.groupApi.qryPlayerDetailData(element);
    }

}

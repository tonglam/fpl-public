package com.tong.fpl.controller;

import com.google.common.collect.Lists;
import com.tong.fpl.constant.enums.FollowAccount;
import com.tong.fpl.domain.letletme.entry.EntryEventSimulatePickData;
import com.tong.fpl.domain.letletme.entry.EntryEventSimulateTransfersData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.global.DropdownData;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerPickData;
import com.tong.fpl.domain.letletme.player.PlayerShowData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.letletmeApi.IGroupApi;
import com.tong.fpl.utils.RedisUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/pick")
    public String pickController(Model model, HttpSession session) {
        int operator = 0;
        if (session.getAttribute("entry") != null) {
            operator = Integer.parseInt(session.getAttribute("entry").toString());
        }
        model.addAttribute("nextGw", this.groupApi.getNextEvent());
        model.addAttribute("pickPlayerData", this.groupApi.qryOffiaccountPickData(operator));
        return "group/pick";
    }

    @GetMapping(value = "/transfers")
    public String transfersController(Model model) {
        model.addAttribute("nextGw", this.groupApi.getNextEvent());
        model.addAttribute("pickPlayerData", this.groupApi.qryOffiaccountPickListForTransfers());
        model.addAttribute("lineupList", Lists.newArrayList());
        return "group/transfers";
    }

    @RequestMapping(value = "/reloadPick")
    public String reloadPickController(@RequestBody PlayerPickData pickPlayerData, Model model) {
        model.addAttribute("pickPlayerData", pickPlayerData);
        return "group/pick::pick";
    }

    @GetMapping(value = "/reloadPickList")
    public String reloadPickListController(Model model) {
        model.addAttribute("pickList", this.groupApi.qryOffiaccountPickList());
        return "group/pick::pickList";
    }

    @RequestMapping(value = "/reloadTransfers")
    public String reloadTransfersController(@RequestBody PlayerPickData pickPlayerData, Model model) {
        model.addAttribute("pickPlayerData", pickPlayerData);
        return "group/transfers::transfers";
    }

    @GetMapping(value = "/reloadTransfersList")
    public String reloadTransfersListController(Model model) {
        model.addAttribute("transfersList", this.groupApi.qryOffiaccountLineupForTransfers());
        return "group/transfers::transfersList";
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
    public String upsertEventScout(@RequestBody ScoutData scoutData, HttpSession session) {
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
     * @apiNote pick
     */
    @RequestMapping("/qryEntryEventPlayerShowList")
    @ResponseBody
    public TableData<PlayerShowData> qryEntryEventPlayerShowList(@RequestParam int event, HttpSession session) {
        int operator = 0;
        if (session.getAttribute("entry") != null) {
            operator = Integer.parseInt(session.getAttribute("entry").toString());
        }
        return this.groupApi.qryOffiaccountEventPlayerShowList(event, operator);
    }

    @RequestMapping("/qrySortedEntryEventPlayerShowList")
    @ResponseBody
    public TableData<PlayerShowData> qrySortedEntryEventPlayerShowList(@RequestBody List<PlayerShowData> playerShowDataList) {
        return this.groupApi.qrySortedEntryEventPlayerShowList(playerShowDataList);
    }

    @RequestMapping("/upsertEventPick")
    @ResponseBody
    public String upsertEventPick(@RequestBody EntryEventSimulatePickData entryEventSimulatePickData, HttpSession session) {
        int entry = Integer.parseInt(session.getAttribute("entry").toString());
        Map<Object, Object> map = RedisUtils.getHashByKey("scoutEntry");
        if (!map.containsKey(String.valueOf(entry))) {
            return "请先加入让让群球探！";
        }
        entryEventSimulatePickData
                .setEntry(FollowAccount.Offiaccount.getEntry())
                .setEvent(this.groupApi.getNextEvent())
                .setOperator(entry);
        this.groupApi.upsertEventPick(entryEventSimulatePickData);
        return "保存成功";
    }

    /**
     * @apiNote transfers
     */
    @RequestMapping("/qryEntryEventPlayerShowListForTransfers")
    @ResponseBody
    public TableData<PlayerShowData> qryEntryEventPlayerShowListForTransfers(@RequestParam int event) {
        return this.groupApi.qryEntryEventPlayerShowListForTransfers(event);
    }

    @RequestMapping("/qryPlayerShowListByElementForTransfers")
    @ResponseBody
    public TableData<PlayerShowData> qryPlayerShowListByElementForTransfers(@RequestBody List<EntryPickData> pickList) {
        return this.groupApi.qryPlayerShowListByElementForTransfers(pickList);
    }

    @RequestMapping("/upsertEventTransfers")
    @ResponseBody
    public String upsertEventTransfers(@RequestBody EntryEventSimulateTransfersData entryEventSimulateTransfersData, HttpSession session) {
        int entry = Integer.parseInt(session.getAttribute("entry").toString());
        Map<Object, Object> map = RedisUtils.getHashByKey("scoutEntry");
        if (!map.containsKey(String.valueOf(entry))) {
            return "请先加入让让群球探！";
        }
        entryEventSimulateTransfersData
                .setEntry(FollowAccount.Offiaccount.getEntry())
                .setEvent(this.groupApi.getNextEvent())
                .setOperator(entry);
        this.groupApi.upsertEventTransfers(entryEventSimulateTransfersData);
        return "提交成功";
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

package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiEntry;
import com.tong.fpl.domain.letletme.entry.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Create by tong on 2021/5/10
 */
@RestController
@RequestMapping("/api/entry")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EntryApiController {

    private final IApiEntry apiEntry;

    @GetMapping("/qryEntryInfo")
    public EntryInfoData qryEntryInfo(@RequestParam int entry) {
        return this.apiEntry.qryEntryInfo(entry);
    }

    @RequestMapping("/fuzzyQueryEntry")
    @ResponseBody
    public List<EntryInfoData> fuzzyQueryEntry(@RequestBody EntryQueryParam param) {
        return this.apiEntry.fuzzyQueryEntry(param);
    }

    @GetMapping("/refreshEntryInfo")
    public void refreshEntryInfo(@RequestParam int entry) {
        this.apiEntry.refreshEntryInfo(entry);
    }

    @GetMapping("/qryEntryLeagueInfo")
    public EntryLeagueData qryEntryLeagueInfo(@RequestParam int entry) {
        return this.apiEntry.qryEntryLeagueInfo(entry);
    }

    @GetMapping("/qryEntryHistoryInfo")
    public EntryHistoryData qryEntryHistoryInfo(@RequestParam int entry) {
        return this.apiEntry.qryEntryHistoryInfo(entry);
    }

    @GetMapping("/qryEntryEventResult")
    public EntryEventResultData qryEntryEventResult(@RequestParam int event, @RequestParam int entry) {
        return this.apiEntry.qryEntryEventResult(event, entry);
    }

    @GetMapping("/refreshEntryEventResult")
    public void refreshEntryEventResult(@RequestParam int event, @RequestParam int entry) {
        this.apiEntry.refreshEntryEventResult(event, entry);
    }

    @GetMapping("/qryEntryEventTransfers")
    public List<EntryEventTransfersData> qryEntryEventTransfers(@RequestParam int event, @RequestParam int entry) {
        return this.apiEntry.qryEntryEventTransfers(event, entry);
    }

    @GetMapping("/qryEntryAllTransfers")
    public List<EntryEventTransfersData> qryEntryAllTransfers(@RequestParam int entry) {
        return this.apiEntry.qryEntryAllTransfers(entry);
    }

    @GetMapping("/refreshEntryEventTransfers")
    public void refreshEntryEventTransfers(@RequestParam int event, @RequestParam int entry) {
        this.apiEntry.refreshEntryEventTransfers(event, entry);
    }

}

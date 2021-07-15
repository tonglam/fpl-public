package com.tong.fpl.controller.api;

import com.google.common.collect.Lists;
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

    @GetMapping("/qryEntryLeagueInfo")
    public EntryLeagueInfoData qryEntryLeagueInfo(@RequestParam int entry) {
        if (entry <= 0) {
            return new EntryLeagueInfoData();
        }
        return this.apiEntry.qryEntryLeagueInfo(entry);
    }

    @GetMapping("/qryEntryHistoryInfo")
    public EntryHistoryInfoData qryEntryHistoryInfo(@RequestParam int entry) {
        if (entry <= 0) {
            return new EntryHistoryInfoData();
        }
        return this.apiEntry.qryEntryHistoryInfo(entry);
    }

    @GetMapping("/qryEntryEventResult")
    public EntryEventResultData qryEntryEventResult(@RequestParam int event, @RequestParam int entry) {
        if (event <= 0 || entry <= 0) {
            return new EntryEventResultData();
        }
        return this.apiEntry.qryEntryEventResult(event, entry);
    }

    @GetMapping("/qryEntryEventTransfers")
    public List<EntryEventTransfersData> qryEntryEventTransfers(@RequestParam int event, @RequestParam int entry) {
        if (event <= 0 || entry <= 0) {
            return Lists.newArrayList();
        }
        return this.apiEntry.qryEntryEventTransfers(event, entry);
    }

    @GetMapping("/qryEntryEventSummary")
    public List<EntryEventResultData> qryEntryEventSummary(@RequestParam int entry) {
        if (entry <= 0) {
            return Lists.newArrayList();
        }
        return this.apiEntry.qryEntryEventSummary(entry);
    }

}

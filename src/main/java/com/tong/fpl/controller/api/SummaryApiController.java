package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiSummary;
import com.tong.fpl.domain.letletme.summary.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by tong on 2021/5/25
 */
@RestController
@RequestMapping("/api/summary")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SummaryApiController {

    private final IApiSummary apiSummary;

    @GetMapping("/qryEntrySeasonInfo")
    public EntrySeasonInfoData qryEntrySeasonInfo(@RequestParam int entry) {
        return this.apiSummary.qryEntrySeasonInfo(entry);
    }

    @GetMapping("/qrySeasonEntrySummary")
    public EntrySeasonSummaryData qrySeasonEntrySummary(@RequestParam int entry) {
        return this.apiSummary.qrySeasonEntrySummary(entry);
    }

    @GetMapping("/qryEntrySeasonCaptain")
    public EntrySeasonCaptainData qryEntrySeasonCaptain(@RequestParam int entry) {
        return this.apiSummary.qryEntrySeasonCaptain(entry);
    }

    @GetMapping("/qryEntrySeasonTransfers")
    public EntrySeasonTransfersData qryEntrySeasonTransfers(@RequestParam int entry) {
        return this.apiSummary.qryEntrySeasonTransfers(entry);
    }

    @GetMapping("/qryEntrySeasonScore")
    public EntrySeasonScoreData qryEntrySeasonScore(@RequestParam int entry) {
        return this.apiSummary.qryEntrySeasonScore(entry);
    }

}

package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiEntry;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by tong on 2021/5/10
 */
@RestController
@RequestMapping("/api/entry")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EntryApiController {

    private final IApiEntry apiEntry;

    @GetMapping("/qryEntryInfoData")
    public EntryInfoData qryEntryInfoData(@RequestParam int entry) {
        return this.apiEntry.qryEntryInfoData(entry);
    }

}

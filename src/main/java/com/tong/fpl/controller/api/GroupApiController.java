package com.tong.fpl.controller.api;

import com.google.common.collect.Lists;
import com.tong.fpl.api.IApiGroup;
import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/9
 */
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GroupApiController {

    private final IApiGroup apiGroup;

    @GetMapping("/qryScoutEntry")
    public Map<String, String> qryScoutEntry() {
        return this.apiGroup.qryScoutEntry();
    }

    @GetMapping("/qryEventEntryScoutResult")
    public EventScoutData qryEventEntryScoutResult(@RequestParam int event, @RequestParam int entry) {
        if (event <= 0 || entry <= 0) {
            return new EventScoutData();
        }
        return this.apiGroup.qryEventEntryScoutResult(event, entry);
    }

    @GetMapping("/qryEventScoutResult")
    public List<EventScoutData> qryEventScoutResult(@RequestParam int event) {
        if (event <= 0) {
            return Lists.newArrayList();
        }
        return this.apiGroup.qryEventScoutResult(event);
    }

    @PostMapping("/upsertEventScout")
    @ResponseBody
    public void upsertEventScout(@RequestBody ScoutData scoutData) {
        if (scoutData.getEvent() <= 0 || scoutData.getEntry() <= 0) {
            return;
        }
        this.apiGroup.upsertEventScout(scoutData);
    }

    @GetMapping("/updateEventScoutResult")
    @ResponseBody
    public void updateEventScoutResult(@RequestParam int event) {
        if (event <= 0) {
            return;
        }
        this.apiGroup.updateEventScoutResult(event);
    }

}

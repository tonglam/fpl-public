package com.tong.fpl.controller.api;

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

    @RequestMapping("/qryScoutEntry")
    public Map<String, String> qryScoutEntry() {
        return this.apiGroup.qryScoutEntry();
    }

    @RequestMapping("/qryEventEntryScoutResult")
    public EventScoutData qryEventEntryScoutResult(@RequestParam int event, @RequestParam int entry) {
        return this.apiGroup.qryEventEntryScoutResult(event, entry);
    }

    @RequestMapping("/qryEventScoutResult")
    public List<EventScoutData> qryEventScoutResult(@RequestParam int event) {
        return this.apiGroup.qryEventScoutResult(event);
    }

    @RequestMapping("/upsertEventScout")
    @ResponseBody
    public void upsertEventScout(@RequestBody ScoutData scoutData) {
        this.apiGroup.upsertEventScout(scoutData);
    }

    @RequestMapping("/updateEventScoutResult")
    @ResponseBody
    public void updateEventScoutResult(@RequestParam int event) {
        this.apiGroup.updateEventScoutResult(event);
    }
}

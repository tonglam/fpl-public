package com.tong.fpl.controller.api;

import com.tong.fpl.api.IApiGroup;
import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
        return this.apiGroup.qryEventEntryScoutResult(event, entry);
    }

    @GetMapping("/qryEventScoutResult")
    public List<EventScoutData> qryEventScoutResult(@RequestParam int event) {
        return this.apiGroup.qryEventScoutResult(event);
    }

    @GetMapping("/refreshCurrentEventScoutResult")
    public void refreshCurrentEventScoutResult(@RequestParam int entry) {
        this.apiGroup.refreshCurrentEventScoutResult(entry);
    }

    @PostMapping("/upsertEventScout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> upsertEventScout(@RequestBody ScoutData scoutData) {
        return this.apiGroup.upsertEventScout(scoutData);
    }

    @GetMapping("/updateEventScoutResult")
    @ResponseBody
    public void updateEventScoutResult(@RequestParam int event) {
        this.apiGroup.updateEventScoutResult(event);
    }

}

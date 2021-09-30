package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiGroup;
import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IGroupService;
import com.tong.fpl.service.IRefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiGroupImpl implements IApiGroup {

    private final IApiQueryService apiQueryService;
    private final IRefreshService refreshService;
    private final IGroupService groupService;

    @Override
    public Map<String, String> qryScoutEntry() {
        return this.apiQueryService.qryScoutEntry();
    }

    @Override
    public EventScoutData qryEventEntryScoutResult(int event, int entry) {
        return this.apiQueryService.qryEventScoutPickResult(event, entry);
    }

    @Override
    public List<EventScoutData> qryEventScoutResult(int event) {
        return this.apiQueryService.qryEventScoutResult(event);
    }

    @Override
    public void refreshCurrentEventScoutResult(int entry) {
        this.refreshService.refreshCurrentEventScoutResult(entry);
    }

    @Override
    public ResponseEntity<Map<String, Object>> upsertEventScout(ScoutData scoutData) {
        return this.groupService.upsertEventScout(scoutData);
    }

    @Override
    public void updateEventScoutResult(int event) {
        this.groupService.updateEventScoutResult(event);
    }
}

package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiGroup;
import com.tong.fpl.domain.letletme.scout.EventScoutData;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IGroupService;
import com.tong.fpl.service.IQueryService;
import com.tong.fpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final IQueryService queryService;
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
        int event = this.queryService.getCurrentEvent();
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEventScoutPickResult", event, entry));
        RedisUtils.removeCacheByKey(StringUtils.joinWith("::", "api::qryEventScoutPickResult", event));
    }

    @Override
    public String upsertEventScout(ScoutData scoutData) {
        return this.groupService.upsertEventScout(scoutData);
    }

    @Override
    public void updateEventScoutResult(int event) {
        this.groupService.updateEventScoutResult(event);
    }
}

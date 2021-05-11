package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiGroup;
import com.tong.fpl.domain.letletme.scout.ScoutData;
import com.tong.fpl.service.IApiQueryService;
import com.tong.fpl.service.IGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiGroupImpl implements IApiGroup {

	private final IApiQueryService apiQueryService;
	private final IGroupService groupService;

	@Override
	public Map<String, String> qryScoutEntry() {
		return this.apiQueryService.qryScoutEntry();
	}

	@Override
	public void upsertEventScout(ScoutData scoutData) {
		this.groupService.upsertEventScout(scoutData);
	}

	@Override
	public void updateEventScoutResult(int event) {
		this.groupService.updateEventScoutResult(event);
	}

}

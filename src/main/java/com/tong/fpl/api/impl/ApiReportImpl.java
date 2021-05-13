package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiReport;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.service.IApiQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiReportImpl implements IApiReport {

	private final IApiQueryService apiQueryService;

	@Override
	public Map<String, List<PlayerValueData>> qryPlayerVuleByChangeDate(String changeDate) {
		return this.apiQueryService.qryPlayerValueByChangeDate(changeDate);
	}

}

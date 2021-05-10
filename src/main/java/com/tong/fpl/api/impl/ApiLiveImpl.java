package com.tong.fpl.api.impl;

import com.tong.fpl.api.IApiLive;
import com.tong.fpl.domain.letletme.live.LiveMatchData;
import com.tong.fpl.service.IApiQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Create by tong on 2021/5/10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiLiveImpl implements IApiLive {

	private final IApiQueryService apiQueryService;

	@Override
	public List<LiveMatchData> qryLiveMatchDataByStatus(String playStatus) {
		return this.apiQueryService.qryLiveMatchDataByStatus(playStatus);
	}

}

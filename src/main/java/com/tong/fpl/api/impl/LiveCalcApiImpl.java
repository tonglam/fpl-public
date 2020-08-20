package com.tong.fpl.api.impl;

import com.tong.fpl.api.ILiveCalcApi;
import com.tong.fpl.domain.data.letletme.api.LiveCalaData;
import com.tong.fpl.service.ILiveCalcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Create by tong on 2020/8/3
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveCalcApiImpl implements ILiveCalcApi {

	private final ILiveCalcService liveCalcService;

	@Override
	public LiveCalaData calcLivePointsByEntry(int event, int entry) {
		return this.liveCalcService.calcLivePointsByEntry(event, entry);
	}

	@Override
	public LiveCalaData calcLivePointsByElementList(int event, Map<Integer, Integer> elementMap, int captain, int viceCaptain) {
		return this.liveCalcService.calcLivePointsByElementList(event, elementMap, captain, viceCaptain);
	}

}

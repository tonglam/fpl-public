package com.tong.fpl.api.impl;

import com.tong.fpl.api.ILiveApi;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.live.ElementLiveData;
import com.tong.fpl.service.ITableQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/8/3
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LiveApiImpl implements ILiveApi {

	private final ITableQueryService tableQueryService;

	@Override
	public TableData<ElementLiveData> qryEntryLivePoints(int entry) {
		return this.tableQueryService.qryEntryLivePoints(entry);
	}

}

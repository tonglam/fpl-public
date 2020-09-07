package com.tong.fpl.api.impl;

import com.tong.fpl.api.IMyFplApi;
import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.service.ITableQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/8/15
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyFplApiImpl implements IMyFplApi {

	private final ITableQueryService tableQueryService;

	@Override
	public TableData<PlayerInfoData> qryPlayerDataList(long page, long limit) {
		return this.tableQueryService.qryPagePlayerDataList(page, limit);
	}

}

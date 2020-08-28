package com.tong.fpl.api.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.api.IMyFplApi;
import com.tong.fpl.domain.data.letletme.table.PlayTableData;
import com.tong.fpl.service.IPageQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Create by tong on 2020/8/15
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MyFplApiImpl implements IMyFplApi {

	private final IPageQueryService pageQueryService;

	@Override
	public Page<PlayTableData> qryPlayerDataList(long current, long size) {
		return this.pageQueryService.qryPagePlayerDataList(current, size);
	}

}

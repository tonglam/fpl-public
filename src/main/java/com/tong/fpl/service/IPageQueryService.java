package com.tong.fpl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.domain.data.letletme.table.PlayTableData;

/**
 * Create by tong on 2020/8/28
 */
public interface IPageQueryService {

	Page<PlayTableData> qryPagePlayerDataList(long current, long size);

}

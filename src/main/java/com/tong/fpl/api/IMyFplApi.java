package com.tong.fpl.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.domain.data.letletme.table.PlayTableData;

/**
 * Create by tong on 2020/8/15
 */
public interface IMyFplApi {

	Page<PlayTableData> qryPlayerDataList(long current, long size);

}

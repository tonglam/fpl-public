package com.tong.fpl.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.domain.data.letletme.PlayerData;

/**
 * Create by tong on 2020/8/15
 */
public interface IMyFplApi {

    Page<PlayerData> qryPlayerDataList(long current, long size);

}

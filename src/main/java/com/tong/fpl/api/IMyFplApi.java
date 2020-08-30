package com.tong.fpl.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;

/**
 * Create by tong on 2020/8/15
 */
public interface IMyFplApi {

	Page<PlayerInfoData> qryPlayerDataList(long current, long size);

}

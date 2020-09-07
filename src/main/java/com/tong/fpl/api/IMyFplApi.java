package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.global.TableData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;

/**
 * Create by tong on 2020/8/15
 */
public interface IMyFplApi {

	TableData<PlayerInfoData> qryPlayerDataList(long page, long limit);

}

package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.table.TableData;

/**
 * Create by tong on 2020/8/15
 */
public interface IMyFplApi {

	TableData<PlayerInfoData> qryPlayerDataList(long current, long size);

}

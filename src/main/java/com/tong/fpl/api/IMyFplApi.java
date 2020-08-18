package com.tong.fpl.api;

import com.tong.fpl.domain.data.letletme.PlayerData;

import java.util.List;

/**
 * Create by tong on 2020/8/15
 */
public interface IMyFplApi {

    List<PlayerData> qryPlayerDataList(long current, long size);

}

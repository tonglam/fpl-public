package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.player.PlayerValueData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiReport {

    Map<String, List<PlayerValueData>> qryPlayerVuleByChangeDate(String changeDate);

}

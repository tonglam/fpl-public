package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiReport {

    /**
     * 根据日期查询球员身价变化
     */
    Map<String, List<PlayerValueData>> qryPlayerValueByDate(String date);

    /**
     * 查询存储的所有联赛
     */
    List<String> qryLeagueInfo();

    /**
     * 根据联赛名称查询阵容选择结果
     */
    LeagueStatData qryTeamSelectByLeagueName(int event, String leagueName);

}

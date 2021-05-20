package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.league.LeagueStatData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiStat {

    /**
     * 根据日期查询球员身价变化
     */
    Map<String, List<PlayerValueData>> qryPlayerValueByDate(String date);

    /**
     * 根据球员查询球员身价变化
     */
    List<PlayerValueData> qryPlayerValueByElement(int element);

    /**
     * 根据球队查询球员身价变化
     */
    Map<String, List<PlayerValueData>> qryPlayerValueByTeamId(int teamId);

    /**
     * 查询存储的所有联赛
     */
    List<String> qryAllLeagueName();

    /**
     * 根据联赛名称查询阵容选择结果
     */
    LeagueStatData qryTeamSelectByLeagueName(int event, String leagueName);

}

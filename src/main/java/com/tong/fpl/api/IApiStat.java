package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.league.LeagueEventSelectData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerSummaryData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.team.TeamSummaryData;

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
     * 刷新player_value：
     * 1.player cache
     * 3.player_value
     * 4.cache
     */
    void refreshPlayerValue();

    /**
     * 根据联赛名称查询阵容选择结果
     */
    LeagueEventSelectData qryLeagueSelectByName(int event, String leagueName);

    /**
     * 刷新联赛阵容选择结果
     */
    void refreshLeagueSelect(int event, String leagueName);

    /**
     * 获取所有赛程
     */
    List<List<String>> qrySeasonFixture();

    /**
     * 获取球员信息
     */
    PlayerInfoData qryPlayerInfo(String season, int code);

    /**
     * 获取球员数据
     */
    PlayerSummaryData qryPlayerSummary(String season, int code);

    /**
     * 刷新球员数据
     */
    void refreshPlayerSummary(String season, int code);

    /**
     * 获取球队数据
     */
    TeamSummaryData qryTeamSummary(String season, String name);

    /**
     * 刷新球队数据
     */
    void refreshTeamSummary(String season, String name);

}

package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.player.PlayerDetailData;
import com.tong.fpl.domain.letletme.player.PlayerFilterData;
import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2021/5/10
 */
public interface IApiPlayer {

    /**
     * 根据element获取球员信息
     */
    PlayerInfoData qryPlayerInfoByElement(String season, int element);

    /**
     * 根据code获取球员信息
     */
    PlayerInfoData qryPlayerInfoByCode(String season, int code);

    /**
     * 根据位置获取球员列表(team_short_name -> player_info)
     */
    LinkedHashMap<String, List<PlayerInfoData>> qryPlayerInfoByElementType(int elementType);

    /**
     * 获取球员详情
     */
    PlayerDetailData qryPlayerDetailByElement(int element);

    /**
     * 根据球队缩写获取赛程(team_short_name -> player_fixture)
     */
    Map<String, List<PlayerFixtureData>> qryTeamFixtureByShortName(String shortName);

    /**
     * 获取赛季所有球员信息
     */
    List<PlayerFilterData> qryFilterPlayers(String season);

    /**
     * 刷新player_stat
     */
    void refreshPlayerStat();

}

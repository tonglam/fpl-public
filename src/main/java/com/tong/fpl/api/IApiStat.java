package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.element.ElementAgainstInfoData;
import com.tong.fpl.domain.letletme.element.ElementAgainstRecordData;
import com.tong.fpl.domain.letletme.element.ElementEventLiveExplainData;
import com.tong.fpl.domain.letletme.element.ElementSummaryData;
import com.tong.fpl.domain.letletme.league.LeagueEventSelectData;
import com.tong.fpl.domain.letletme.player.PlayerSummaryData;
import com.tong.fpl.domain.letletme.player.PlayerValueData;
import com.tong.fpl.domain.letletme.team.TeamAgainstInfoData;
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
     * 根据日期查询球员身价变化
     */
    List<PlayerValueData> qryPlayerPriceChange(String date);

    /**
     * 根据球员查询球员身价变化
     */
    List<PlayerValueData> qryPlayerValueByElement(int element);

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
    LeagueEventSelectData qryLeagueSelectByName(int event, int leagueId, String leagueName);

    /**
     * 刷新联赛阵容选择结果
     */
    void refreshLeagueSelect(int event, String leagueName);

    /**
     * 获取所有赛程
     */
    List<List<String>> qrySeasonFixture();

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

    /**
     * 获取球员比赛周得分详情
     */
    ElementEventLiveExplainData qryElementEventExplainResult(int event, int element);

    /**
     * 获取对阵球队的交手记录简介
     */
    TeamAgainstInfoData qryTeamAgainstRecordInfo(int teamId, int againstId);

    /**
     * 获取对阵球队的某次交手记录得分结果
     */
    List<ElementSummaryData> qryTeamAgainstRecordResult(String season, int event, int teamHId, int teamAId);

    /**
     * 获取对阵球队的交手记录最好的球员（active:是否只看球队现役球员）
     */
    List<ElementAgainstInfoData> qryTopElementAgainstInfo(int teamId, int againstId, boolean active);

    /**
     * 获取对阵球队的球员的交手记录
     */
    List<ElementAgainstRecordData> qryElementAgainstRecord(int teamId, int againstId, int elementCode);

}

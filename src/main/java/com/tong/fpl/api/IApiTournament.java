package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryAgainstInfoData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.entry.SearchEntryEventResultData;
import com.tong.fpl.domain.letletme.tournament.*;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 联赛指本系统创建的联赛
 * <p>
 * Create by tong on 2021/5/10
 */
public interface IApiTournament {

    /**
     * 查询参加的积分联赛
     */
    List<TournamentInfoData> qryEntryPointsRaceTournament(int entry);

    /**
     * 查询参加的淘汰联赛
     */
    List<TournamentInfoData> qryEntryKnockoutTournament(int entry);

    /**
     * 查询参加的冠军杯
     */
    List<TournamentInfoData> qryEntryPointsRaceChampionLeague(int entry);

    /**
     * 查询所在冠军杯阶段
     */
    String qryEntryChampionLeagueStage(int entry, int tournamentId);

    /**
     * 查询冠军杯比赛阶段
     */
    LinkedHashMap<String, List<String>> qryChampionLeagueStage(int tournamentId);

    /**
     * 查询冠军杯比赛小组赛阶段
     */
    LinkedHashMap<String, List<String>> qryChampionLeagueStageGroup(int tournamentId);

    /**
     * 查询冠军杯比赛小组赛晋级名单
     */
    List<List<TournamentGroupData>> qryChampionLeagueGroupQualifications(int tournamentId);

    /**
     * 查询冠军杯比赛淘汰赛制定轮次阶段
     */
    List<List<TournamentKnockoutData>> qryChampionLeagueStageKnockoutRound(int tournamentId, int round);

    /**
     * 根据id查询联赛信息
     */
    TournamentInfoData qryTournamentInfo(int id);

    /**
     * 获取联赛周得分
     */
    List<EntryEventResultData> qryTournamentEventResult(int event, int tournamentId);

    /**
     * 刷新联赛周得分
     */
    void refreshTournamentEventResult(int event, int tournamentId);

    /**
     * 获取搜索后的联赛周得分
     */
    SearchEntryEventResultData qryTournamentEventSearchResult(int event, int tournamentId, int element);

    /**
     * 获取冠军杯周得分
     */
    List<EntryEventResultData> qryChampionLeagueEventResult(int event, int tournamentId);

    /**
     * 刷新冠军杯周得分
     */
    void refreshChampionLeagueEventResult(int event, int tournamentId);

    /**
     * 指定周获取联赛周得分总结
     */
    List<TournamentPointsGroupEventResultData> qryTournamentEventSummary(int event, int tournamentId);

    /**
     * 指定球队获取联赛周得分总结
     */
    List<TournamentPointsGroupEventResultData> qryTournamentEntryEventSummary(int tournamentId, int entry);

    /**
     * 获取联赛周冠军榜
     */
    TournamentGroupEventChampionData qryTournamentEventChampion(int tournamentId);

    /**
     * 获取联赛抽签名单
     */
    List<Integer> qryDrawKnockoutEntries(int tournamentId);

    /**
     * 获取联赛抽签结果
     */
    List<EntryAgainstInfoData> qryDrawKnockoutResults(int tournamentId);

    /**
     * 获取联赛球队可抽签列表
     */
    List<EntryInfoData> qryDrawKnockoutOpponents(int tournamentId, int entry);

    /**
     * 获取可抽签联赛通知
     */
    String qryDrawKnockoutNotice(int tournamentId);

    /**
     * 可抽签联赛抽签
     */
    String drawKnockoutSinglePair(int tournamentId, String groupName, int entry, int position);

    /**
     * 可抽签联赛抽签结果
     */
    List<List<EntryInfoData>> qryDrawKnockoutPairs(int tournamentId);

}

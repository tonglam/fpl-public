package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.SearchEntryEventResultData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentPointsGroupEventResultData;

import java.util.List;

/**
 * 联赛指本系统创建的联赛
 * <p>
 * Create by tong on 2021/5/10
 */
public interface IApiTournament {

    /**
     * 查询参加的联赛
     */
    List<TournamentInfoData> qryEntryPointsRaceTournament(int entry);

    /**
     * 根据id查询联赛信息
     */
    TournamentInfoData qryTournamentInfo(int id);

    /**
     * 获取联赛周得分
     */
    List<EntryEventResultData> qryTournamentEventResult(int event, int tournamentId);

    /**
     * 获取搜索后的联赛周得分
     */
    SearchEntryEventResultData qryTournamentEventSearchResult(int event, int tournamentId, int element);

    /**
     * 指定周获取联赛周得分总结
     */
    List<TournamentPointsGroupEventResultData> qryTournamentEventSummary(int event, int tournamentId);

    /**
     * 指定球队获取联赛周得分总结
     */
    List<TournamentPointsGroupEventResultData> qryTournamentEntryEventSummary(int tournamentId, int entry);


}

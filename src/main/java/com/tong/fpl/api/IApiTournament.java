package com.tong.fpl.api;

import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.tournament.TournamentInfoData;

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
     * 获取联赛成员是否拥有球员
     */
    List<Integer> qryTournamentEntryContainElement(int event, int tournamentId, int element);

    /**
     * 获取联赛成员是否出场球员
     */
    List<Integer> qryTournamentEntryPlayElement(int event, int tournamentId, int element);

}

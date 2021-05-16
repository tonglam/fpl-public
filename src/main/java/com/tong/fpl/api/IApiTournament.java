package com.tong.fpl.api;

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
    TournamentInfoData qryTournamentInfoById(int id);

}

package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Create by tong on 2020/6/23
 */
public interface TournamentInfoMapper extends BaseMapper<TournamentInfoEntity> {

    @Select("SELECT * FROM tournament_info " +
            "WHERE knockout_mode <> 'No_knockout'" +
            "AND #{event} BETWEEN knockout_start_gw AND knockout_end_gw" +
            "AND state = 1 ")
    List<TournamentInfoEntity> getAllKnockoutTournamentsByEvent(int event);

    @Select("SELECT * FROM tournament_info " +
            "WHERE group_mode = 'Points_race'" +
            "AND #{event} BETWEEN group_start_gw AND group_end_gw ")
    List<TournamentInfoEntity> getAllPointsRaceGroupByEvent(int event);

    @Select("SELECT * FROM tournament_info " +
            "WHERE group_mode = 'Battle_race'" +
            "AND #{event} BETWEEN group_start_gw AND group_end_gw" +
            "AND state = 1 ")
    List<TournamentInfoEntity> getAllBattleRaceGroupByEvent(int event);

}

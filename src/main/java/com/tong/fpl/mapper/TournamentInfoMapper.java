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

}

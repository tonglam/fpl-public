package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.TournamentGroupEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Create by tong on 2020/6/11
 */
public interface TournamentGroupMapper extends BaseMapper<TournamentGroupEntity> {

    @Select("select distinct group_name from tournament_group " +
            "where tournament_id = #{tournamentId} " +
            "and group_name != '' and group_name is not null ")
    List<String> getZjGroupNameList(int tournamentId);

}

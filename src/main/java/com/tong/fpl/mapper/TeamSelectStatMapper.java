package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.TeamSelectStatEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Create by tong on 2020/9/17
 */
public interface TeamSelectStatMapper extends BaseMapper<TeamSelectStatEntity> {

    @Select("select distinct league_name from team_select_stat")
    List<String> qryLeagueNameList();

}

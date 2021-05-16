package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.LeagueEventReportEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Create by tong on 2020/11/6
 */
public interface LeagueEventReportMapper extends BaseMapper<LeagueEventReportEntity> {

    @Select("select DISTINCT(league_name) from league_event_report")
    List<String> qryLeagueNameList();

    @Select("select DISTINCT(league_name) from league_event_report where event = #{event}")
    List<String> qryLeagueNameListByEvent(int event);

}

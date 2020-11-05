package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.entity.LeagueEventStatEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Create by tong on 2020/9/17
 */
public interface LeagueEventStatMapper extends BaseMapper<LeagueEventStatEntity> {

	@Select("select distinct league_name from league_event_stat")
	List<String> qryLeagueNameList();

}

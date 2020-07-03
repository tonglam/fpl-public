package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.data.fpl.QueryParam;
import com.tong.fpl.domain.entity.TournamentInfoEntity;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Create by tong on 2020/6/23
 */
public interface TournamentInfoMapper extends BaseMapper<TournamentInfoEntity> {

	@Select(" SELECT * FROM tournament_info " +
			"WHERE 1=1 " +
			"<if test=\"param.cupName != null and param.cupName != ''\"> " +
			"AND cup_name = #{param.cupName} " +
			"</if> " +
			"<if test=\"param.creator != null and param.creator != ''\"> " +
			"AND creator = #{param.creator} " +
			"</if> " +
			"<if test=\"param.leagueId != null and param.leagueId != ''\"> " +
			"AND league_id = #{param.leagueId} " +
			"</if> " +
			"ORDER BY create_time ASC ")
	List<TournamentInfoEntity> queryTournamentInfo(QueryParam param);

}

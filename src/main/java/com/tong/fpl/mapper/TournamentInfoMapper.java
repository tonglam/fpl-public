package com.tong.fpl.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.fpl.domain.data.letletme.global.QueryParam;
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

	@Select("SELECT * FROM tournament_info " +
			"WHERE knockout_mode <> 'No_knockout'" +
			"And #{event} BETWEEN knockout_start_gw AND knockout_end_gw ")
	List<TournamentInfoEntity> getAllKnockoutTournamentsByEvent(int event);

	@Select("SELECT * FROM tournament_info " +
			"WHERE group_mode = 'Points_race'" +
			"And #{event} BETWEEN group_start_gw AND group_end_gw ")
	List<TournamentInfoEntity> getAllPointsRaceGroupByEvent(int event);

	@Select("SELECT * FROM tournament_info " +
			"WHERE group_mode = 'Battle_race'" +
			"And #{event} BETWEEN group_start_gw AND group_end_gw ")
	List<TournamentInfoEntity> getAllBattleRaceGroupByEvent(int event);

}

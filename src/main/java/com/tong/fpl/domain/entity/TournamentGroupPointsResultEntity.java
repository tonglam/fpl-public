package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/7/17
 */
@Data
@Accessors(chain = true)
@TableName("tournament_group_points_reslut")
public class TournamentGroupPointsResultEntity {

	@TableId
	private int id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int tournamentId;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int groupId;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int event;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int entry;
	private int eventGroupRank;
	private int eventPoints;
	private int eventCost;
	private int eventNetPoints;
	private int eventRank;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}

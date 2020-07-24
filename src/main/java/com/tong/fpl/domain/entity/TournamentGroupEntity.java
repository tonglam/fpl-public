package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/6/11
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_group")
public class TournamentGroupEntity {

	@TableId
	private int id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int tournamentId;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int groupId;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int groupIndex;
	private int entry;
	private int startGw;
	private int endGw;
	private int groupPoints;
	private int groupRank;
	private int play;
	private int win;
	private int draw;
	private int lose;
	private boolean qualified;
	private int overallPoints;
	private int overallRank;
	@TableField(fill = FieldFill.INSERT)
	private String createTime;
	@TableField(fill = FieldFill.UPDATE)
	private String updateTime;

}

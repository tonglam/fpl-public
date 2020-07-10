package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/6/11
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_group_result")
public class TournamentGroupResultEntity {

	private Integer id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer tournamentId;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer groupId;
	@TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer event;
	private Integer round;
	private Integer homeIndex;
	private Integer homeEntry;
	private Integer homeEntryNetPoint;
	private Integer homeEntryRank;
	private Integer awayIndex;
	private Integer awayEntry;
	private Integer awayEntryNetPoint;
	private Integer awayEntryRank;
	private Integer roundWinner;
	@TableField(fill = FieldFill.UPDATE)
	private String updateTime;

}

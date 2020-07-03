package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Create by tong on 2020/6/11
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_knockout_result")
public class TournamentKnockoutResultEntity {

	@TableId
	private Integer id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer tournamentId;
	@TableField(value = "`round`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer round;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer matchId;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer playAginstId;
	@TableField(value = "`event`")
	private Integer event;
	private Integer homeEntry;
	private Integer homeEntryNetPoint;
	private Integer homeEntryRank;
	private String homeEntryChip;
	private Integer awayEntry;
	private Integer awayEntryNetPoint;
	private Integer awayEntryRank;
	private String awayEntryChip;
	private Integer matchWinner;
	@TableField(fill = FieldFill.INSERT)
	private Date createTime;
	@TableField(fill = FieldFill.UPDATE)
	private Date updateTime;

}

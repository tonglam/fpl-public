package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/6/11
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_knockout_result")
public class TournamentKnockoutResultEntity {

	@TableId
	private int id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int tournamentId;
	@TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int event;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int matchId;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int playAginstId;
	private boolean eventFinished;
	private int homeEntry;
	private int homeEntryNetPoints;
	private int homeEntryRank;
	private int awayEntry;
	private int awayEntryNetPoints;
	private int awayEntryRank;
	private int matchWinner;
	@TableField(fill = FieldFill.INSERT)
	private String createTime;
	@TableField(fill = FieldFill.UPDATE)
	private String updateTime;

}

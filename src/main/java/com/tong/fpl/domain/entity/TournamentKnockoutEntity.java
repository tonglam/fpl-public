package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/6/11
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_knockout")
public class TournamentKnockoutEntity {

	@TableId
	private int id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int tournamentId;
	@TableField(value = "`round`")
	private int round;
	private int startGw;
	private int endGw;
	private int matchId;
	private int nextMatchId;
	private int homeEntry;
	private int awayEntry;
	private int roundWinner;
	@TableField(fill = FieldFill.INSERT)
	private String createTime;
	@TableField(fill = FieldFill.UPDATE)
	private String updateTime;

}

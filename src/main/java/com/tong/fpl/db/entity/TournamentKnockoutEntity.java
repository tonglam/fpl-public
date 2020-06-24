package com.tong.fpl.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Create by tong on 2020/6/11
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_knockout")
public class TournamentKnockoutEntity {

	@TableId
	private Integer id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer tournamentId;
	private Integer homeEntry;
	private Integer awayEntry;
	private Integer knockoutRound;
	private Integer matchId;
	private Integer nextMatchId;
	private Integer roundWinner;
	@TableField(fill = FieldFill.INSERT)
	private Date createTime;
	@TableField(fill = FieldFill.UPDATE)
	private Date updateTIme;

}

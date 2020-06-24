package com.tong.fpl.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Create by tong on 2020/6/23
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_info")
public class TournamentInfoEntity {

	@TableId
	private Integer id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private String name;
	private String creator;
	private String leagueType;
	private String leagueId;
	private Integer startGw;
	private Integer endGw;
	private Integer teamPerGroup;
	private Integer totalTeam;
	private Integer groupNum;
	private Integer qualifiers;
	private Boolean fillAverage;
	private Boolean drawKnockoutNow;
	private Integer knockoutRounds;
	private Boolean homeAwayMode;
	@TableField(fill = FieldFill.INSERT)
	private Date createTime;

}

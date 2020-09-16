package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/3/10
 */
@Data
@Accessors(chain = true)
@TableName(value = "league_result_stat")
public class LeagueResultStatEntity {

	@TableField
	private int id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int leagueId;
	private String leagueType;
	private String leagueName;
	private int entry;
	private String entryName;
	private String playerName;
	private int overallPoints;
	private int overallRank;
	private int bank;
	private int teamValue;
	@TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int event;
	private int eventPoints;
	private int eventTransfers;
	private int eventTransfersCost;
	private int eventNetPoints;
	private int eventBenchPoints;
	private int eventRank;
	private String eventChip;
	private String eventCaptain;
	private int eventCaptainPoints;
	private String eventPicks;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}

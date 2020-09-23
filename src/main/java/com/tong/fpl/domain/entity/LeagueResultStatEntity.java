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
	private Integer id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer leagueId;
	private String leagueType;
	private String leagueName;
	private Integer entry;
	private String entryName;
	private String playerName;
	private Integer overallPoints;
	private Integer overallRank;
	private Integer bank;
	private Integer teamValue;
	@TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer event;
	private Integer eventPoints;
	private Integer eventTransfers;
	private Integer eventTransfersCost;
	private Integer eventNetPoints;
	private Integer eventBenchPoints;
	private Integer eventRank;
	private String eventChip;
	private String eventCaptain;
	private Integer eventCaptainPoints;
	private String eventPicks;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}

package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/3/10
 */
@Data
@Accessors(chain = true)
@TableName(value = "entry_event_result")
public class EntryEventResultEntity {

	@TableField
	@JsonIgnore
	private int id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int entry;
	@TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int event;
	private int eventPoints;
	private int eventTransfers;
	private int eventTransfersCost;
	private int eventNetPoints;
	private int eventBenchPoints;
	private int eventRank;
	private int overallRank;
	private String eventChip;
	private String eventPicks;
	private boolean eventFinished;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}

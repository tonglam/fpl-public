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
@TableName(value = "event_result")
public class EventResultEntity {

	@TableField
	private Integer id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer entry;
	@TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer event;
	private Integer eventPoints;
	private Integer eventTransfers;
	private Integer eventTransfersCost;
	private Integer eventNetPoints;
	private Integer eventCaptain;
	private Integer eventBenchPoints;
	private Integer eventRank;
	private Integer overallRank;
	private String eventChip;
	private String eventPicks;
	private Boolean eventFinished;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}

package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/7/9
 */
@Data
@Accessors(chain = true)
@TableName(value = "event_fixture")
public class EventFixtureEntity {

	@TableId(type = IdType.INPUT)
	private int id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int code;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int event;
	private String kickoffTime;
	private boolean started;
	private boolean finished;
	private boolean provisionalStartTime;
	private boolean finishedProvisional;
	private int minutes;
	private int teamH;
	private int teamHScore;
	private int teamA;
	private int teamAScore;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}

package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/7/9
 */
@Data
@Accessors(chain = true)
@TableName(value = "player_value")
public class PlayerValueEntity {

	@TableId
	private int id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int element;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int elementType;
	private int event;
	@TableField(value = "`value`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int value;
	private String changeDate;
	private String changeType;
	@TableField(value = "`last_value`")
	private int lastValue;
	private String selectedByPercent;
	private String lastSelectedByPercent;
	private int transfersInEvent;
	private int transfersOutEvent;
	private int transfersIn;
	private int transfersOut;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}

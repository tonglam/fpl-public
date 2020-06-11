package com.tong.fpl.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/6/11
 */
@Data
@Accessors(chain = true)
@TableName(value = "cup_group")
public class CupGroupEntity {

	@TableId
	private int cupId;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int groupId;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int entry;
	private int qualifiers;
	private boolean qualified;
	@TableField(fill = FieldFill.INSERT)
	private int createTime;

}

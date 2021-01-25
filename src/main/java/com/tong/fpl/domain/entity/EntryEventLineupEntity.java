package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/1/11
 */
@Data
@Accessors(chain = true)
@TableName(value = "entry_event_lineup")
public class EntryEventLineupEntity {

	@TableId
	private Integer id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer entry;
	@TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer event;
	private Integer teamValue;
	private Integer bank;
	private Integer freeTransfers;
	private Integer transfers;
	private Integer transfersCost;
	private String transfersIn;
	private String transfersOut;
	private String lineup;
	@TableField(fill = FieldFill.INSERT)
	private String createTime;
	@TableField(fill = FieldFill.UPDATE)
	private String updateTime;

}

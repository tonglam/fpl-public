package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/12/14
 */
@Data
@Accessors(chain = true)
@TableName(value = "entry_event_transfer")
public class EntryEventTransferEntity {

	@TableId
	private Integer id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer entry;
	@TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer event;
	private Integer elementIn;
	private Integer elementInCost;
	private Boolean elementInPlayed;
	private Integer elementOut;
	private Integer elementOutCost;
	private String time;

}

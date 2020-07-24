package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName(value = "event")
public class EventEntity {

	@TableId(value = "`event`", type = IdType.INPUT)
	private int event;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private String name;
	private String deadlineTime;
	private int averageEntryScore;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private boolean finished;
	private int highestScore;
	private int highestScoringEntry;
	private boolean isPrevious;
	private boolean isCurrent;
	private boolean isNext;
	private int mostSelected;
	private int mostTransferredIn;
	private int mostCaptained;
	private int mostViceCaptained;

}
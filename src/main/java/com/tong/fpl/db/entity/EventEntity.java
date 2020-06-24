package com.tong.fpl.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName(value = "event")
public class EventEntity {

	@TableId(type = IdType.INPUT)
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String name;
    private Date deadlineTime;
    private Integer averageEntryScore;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Boolean finished;
    private Integer highestScore;
    private Integer highestScoringEntry;
	private Boolean isPrevious;
	private Boolean isCurrent;
	private Boolean isNext;
    private Integer mostSelected;
    private Integer mostTransferredIn;
    private Integer mostCaptained;
    private Integer mostViceCaptained;

}
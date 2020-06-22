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
    private boolean finished;
    private Integer highestScore;
    private Integer highestScoringEntry;
    private boolean isPrevious;
    private boolean isCurrent;
    private boolean isNext;
    private Integer mostSelected;
    private Integer mostTransferredIn;
    private Integer mostCaptained;
    private Integer mostViceCaptained;

}
package com.tong.fpl.db.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "event")
public class EventEntity {

    @TableId
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
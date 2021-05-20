package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/1/28
 */
@Data
@Accessors(chain = true)
@TableName(value = "entry_event_simulate_pick")
public class EntryEventSimulatePickEntity {

    @TableId
    private Integer id;
    @TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer event;
    private Integer operator;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer entry;
    private String lineup;
    @TableField(fill = FieldFill.INSERT)
    private String createTime;
    @TableField(fill = FieldFill.UPDATE)
    private String updateTime;

}

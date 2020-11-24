package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/10/1
 */
@Data
@Accessors(chain = true)
@TableName(value = "zj_tournament_captain")
public class ZjTournamentCaptainEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer tournamentId;
    private Integer groupId;
    private Integer captainEntry;
    private String phaseTwoDeadline;
    private String pkDeadline;
    @TableField(fill = FieldFill.INSERT)
    private String createTime;

}

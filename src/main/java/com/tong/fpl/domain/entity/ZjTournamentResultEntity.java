package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/10/21
 */
@Data
@Accessors(chain = true)
@TableName(value = "zj_tournament_result")
public class ZjTournamentResultEntity {

    @TableId
    private Integer id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer tournamentId;
    private Integer groupId;
    private String groupName;
    private Integer phaseOneTotalPoints;
    private Integer phaseOneGroupPoints;
    private Integer phaseOneTotalGroupPoints;
    private Integer phaseTwoTotalPoints;
    private Integer phaseTwoGroupPoints;
    private Integer phaseTwoTotalGroupPoints;
    private Integer pkTotalPoints;
    private Integer pkGroupPoints;
    private Integer pkTotalGroupPoints;
    private Integer tournamentTotalPoints;
    private Integer tournamentTotalGroupPoints;
    private Integer tournamentRank;
    @TableField(fill = FieldFill.INSERT)
    private String createTime;
    @TableField(fill = FieldFill.UPDATE)
    private String updateTime;

}

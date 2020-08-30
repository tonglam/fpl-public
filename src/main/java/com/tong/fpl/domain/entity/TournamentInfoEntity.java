package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/6/23
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_info")
public class TournamentInfoEntity {

    @TableId
    private int id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String name;
    private String creator;
    private int adminerEntry;
    private String season;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String leagueType;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private int leagueId;
    private int totalTeam;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String groupMode;
    private int groupPlayAgainstNum;
    private int teamPerGroup;
    private int groupStartGw;
    private int groupEndGw;
    private int groupRounds;
    private int groupQualifiers;
    private boolean groupFillAverage;
    private int groupNum;
    private int knockoutTeam;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String knockoutMode;
    private int knockoutPlayAgainstNum;
    private int knockoutStartGw;
    private int knockoutEndGw;
    private int knockoutRounds;
    private int state;
    @TableField(fill = FieldFill.INSERT)
    private String createTime;
    @TableField(fill = FieldFill.UPDATE)
    private String updateTime;

}

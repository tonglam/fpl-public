package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/12/5
 */
@Data
@Accessors(chain = true)
@TableName(value = "scout")
public class ScoutEntity {

    @TableId
    private Integer id;
    private Integer event;
    private Integer entry;
    private String scoutName;
    private Integer transfers;
    private Integer leftTransfers;
    private Integer gkp;
    private Integer gkpTeamId;
    private Integer gkpPoints;
    private Integer def;
    private Integer defTeamId;
    private Integer defPoints;
    private Integer mid;
    private Integer midTeamId;
    private Integer midPoints;
    private Integer fwd;
    private Integer fwdTeamId;
    private Integer fwdPoints;
    private Integer captain;
    private Integer captainTeamId;
    private Integer captainPoints;
    private String reason;
    private Integer eventPoints;
    private Integer totalPoints;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateTime;

}

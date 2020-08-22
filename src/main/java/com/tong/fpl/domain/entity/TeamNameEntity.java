package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/8/22
 */
@Data
@Accessors(chain = true)
@TableName(value = "team_name")
public class TeamNameEntity {

    private String season;
    private int teamId;
    private String name;
    private String shortName;

}

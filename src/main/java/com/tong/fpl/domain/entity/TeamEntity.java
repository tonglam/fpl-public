package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName(value = "team")
public class TeamEntity {

	@TableId(type = IdType.INPUT)
    private Integer id;
    private Integer code;
    private Integer draw;
    private Integer form;
    private Integer loss;
    private String name;
    private Integer played;
    private Integer points;
    private Integer position;
    private String shortName;
    private Integer strength;
    private Integer teamDivision;
	private Boolean unavailable;
    private Integer win;
    private Integer strengthOverallHome;
    private Integer strengthOverallAway;
    private Integer strengthAttackHome;
    private Integer strengthAttackAway;
    private Integer strengthDefenceHome;
    private Integer strengthDefenceAway;
    private Integer pulseId;

}
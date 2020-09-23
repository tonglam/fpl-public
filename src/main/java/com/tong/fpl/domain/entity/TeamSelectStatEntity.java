package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/3/10
 */
@Data
@Accessors(chain = true)
@TableName(value = "team_select_stat")
public class TeamSelectStatEntity {

	@TableField
	private Integer id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private String leagueName;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer event;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer entry;
	private String chip;
	private Integer position1;
	private Integer position2;
	private Integer position3;
	private Integer position4;
	private Integer position5;
	private Integer position6;
	private Integer position7;
	private Integer position8;
	private Integer position9;
	private Integer position10;
	private Integer position11;
	private Integer position12;
	private Integer position13;
	private Integer position14;
	private Integer position15;
	private Integer captain;
	private Integer viceCaptain;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}

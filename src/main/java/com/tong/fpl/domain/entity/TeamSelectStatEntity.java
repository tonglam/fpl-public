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
	private int id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private String leagueName;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int event;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int entry;
	private String chip;
	private int position1;
	private int position2;
	private int position3;
	private int position4;
	private int position5;
	private int position6;
	private int position7;
	private int position8;
	private int position9;
	private int position10;
	private int position11;
	private int position12;
	private int position13;
	private int position14;
	private int position15;
	private int captain;
	private int viceCaptain;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}

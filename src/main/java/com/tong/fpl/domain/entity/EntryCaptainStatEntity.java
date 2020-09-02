package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/9/2
 */
@Data
@Accessors(chain = true)
@TableName(value = "entry_captain_stat")
public class EntryCaptainStatEntity {

	@TableField
	private int id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int entry;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int event;
	private String entryName;
	private String playerName;
	private int overallPoints;
	private int overallRank;
	private String chip;
	private int element;
	private String webName;
	private int points;
	private int totalPoints;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}

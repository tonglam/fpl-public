package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Create by tong on 2020/6/11
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_group")
public class TournamentGroupEntity {

	private Integer id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer tournamentId;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer groupId;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer index;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private Integer entry;
	@TableField(fill = FieldFill.INSERT)
	private Integer points;
	private Integer play;
	private Integer win;
	private Integer draw;
	private Integer lose;
	@TableField(value = "`rank`")
	private Integer rank;
	private Boolean qualified;
	private Date createTime;
	private Date updateTime;

}

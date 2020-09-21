package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName(value = "event_live")
public class EventLiveEntity {

	@TableId
	private int id;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int element;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY)
	private int elementType;
	private int teamId;
	@TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int event;
	private int minutes;
	private int goalsScored;
	private int assists;
	private int cleanSheets;
	private int goalsConceded;
	private int ownGoals;
	private int penaltiesSaved;
	private int penaltiesMissed;
	private int yellowCards;
	private int redCards;
	private int saves;
	private int bonus;
	private int bps;
	private int totalPoints;

}
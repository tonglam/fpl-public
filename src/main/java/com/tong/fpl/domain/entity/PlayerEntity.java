package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName(value = "player")
public class PlayerEntity {

	@TableId(type = IdType.INPUT)
	private int element;
	private int code;
	private int price;
	private int chanceOfPlayingNextRound;
	private int chanceOfPlayingThisRound;
	private int dreamteamCount;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private int elementType;
	private int eventPoints;
	private String firstName;
	private String secondName;
	@TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
	private String webName;
	private int teamId;
	private String form;
	private boolean inDreamteam;
	private String news;
	private String newsAdded;
	private String pointsPerGame;
	private String selectedByPercent;
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
	private String influence;
	private String creativity;
	private String threat;
	private String ictIndex;

}
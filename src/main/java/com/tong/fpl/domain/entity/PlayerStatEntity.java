package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName(value = "player_stat")
public class PlayerStatEntity {

	@TableId
	private Integer id;
	private Integer event;
	private Integer element;
	private Integer code;
	private Integer matchPlayed;
	private Integer chanceOfPlayingNextRound;
	private Integer chanceOfPlayingThisRound;
	private Integer dreamteamCount;
	private Integer eventPoIntegers;
	private String form;
	private Boolean inDreamteam;
	private String news;
	private String newsAdded;
	private String poIntegersPerGame;
	private String selectedByPercent;
	private Integer minutes;
	private Integer goalsScored;
	private Integer assists;
	private Integer cleanSheets;
	private Integer goalsConceded;
	private Integer ownGoals;
	private Integer penaltiesSaved;
	private Integer penaltiesMissed;
	private Integer yellowCards;
	private Integer redCards;
	private Integer saves;
	private Integer bonus;
	private Integer bps;
	private String influence;
	private String creativity;
	private String threat;
	private String ictIndex;
	private Integer transfersInEvent;
	private Integer transfersOutEvent;
	private Integer transfersIn;
	private Integer transfersOut;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}
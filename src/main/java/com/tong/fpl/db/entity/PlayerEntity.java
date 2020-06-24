package com.tong.fpl.db.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName(value = "player", autoResultMap = true)
public class PlayerEntity {

    @TableId(type = IdType.INPUT)
    private Integer id;
    private Integer code;
    private Integer chanceOfPlayingNextRound;
    private Integer chanceOfPlayingThisRound;
    private Integer dreamteamCount;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private Integer elementType;
    private Integer eventPoints;
    private String firstName;
    private String secondName;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private String webName;
    private String form;
	private Boolean inDreamteam;
    private String news;
    private String newsAdded;
    private String pointsPerGame;
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

}
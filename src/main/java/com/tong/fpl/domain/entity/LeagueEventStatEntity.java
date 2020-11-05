package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/3/10
 */
@Data
@Accessors(chain = true)
@TableName(value = "league_event_stat")
public class LeagueEventStatEntity {

	@TableId
	private Integer id;
	private Integer leagueId;
	private String leagueType;
	private String leagueName;
	private Integer entry;
	private String entryName;
	private String playerName;
	private Integer overallPoints;
	private Integer overallRank;
	private Integer teamValue;
	private Integer bank;
	private Integer event;
	private Integer eventPoints;
	private Integer eventTransfers;
	private Integer eventTransfersCost;
	private Integer eventNetPoints;
	private Integer eventBenchPoints;
	private Integer eventRank;
	private String eventChip;
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
	private Integer captainPoints;
	private Boolean captainBlank;
	private Integer viceCaptain;
	private Integer viceCaptainPoints;
	private Boolean viceCaptainBlank;
	private Integer highestScore;
	private Integer highestScorePoints;
	private Boolean highestScoreBlank;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}

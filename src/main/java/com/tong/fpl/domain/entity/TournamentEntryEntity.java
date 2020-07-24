package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/7/7
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_entry")
public class TournamentEntryEntity {

	@TableId
	private int id;
	private int tournamentId;
	private int leagueId;
	private int entry;
	@TableField(fill = FieldFill.INSERT)
	private String createTime;

}

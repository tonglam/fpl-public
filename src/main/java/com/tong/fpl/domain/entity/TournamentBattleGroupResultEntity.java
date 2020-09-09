package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/6/11
 */
@Data
@Accessors(chain = true)
@TableName(value = "tournament_battle_group_result")
public class TournamentBattleGroupResultEntity {

    @TableId
    private int id;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private int tournamentId;
    @TableField(insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private int groupId;
    @TableField(value = "`event`", insertStrategy = FieldStrategy.NOT_EMPTY, updateStrategy = FieldStrategy.NOT_EMPTY)
    private int event;
    private int homeIndex;
	private int homeEntry;
	private int homeEntryNetPoints;
	private int homeEntryRank;
	private int homeEntryMatchPoints;
	private int awayIndex;
	private int awayEntry;
	private int awayEntryNetPoints;
	private int awayEntryRank;
	private int awayEntryMatchPoints;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateTime;

}

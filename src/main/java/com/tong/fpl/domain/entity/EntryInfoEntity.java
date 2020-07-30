package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/6/11
 */
@Data
@Accessors(chain = true)
@TableName("entry_info")
public class EntryInfoEntity {

	@TableId(type = IdType.INPUT)
	private int entry;
	private String entryName;
	private String playerName;
	private String region;
	private int startedEvent;
	private int overallPoints;
	private int overallRank;
	private int bank;
	private int teamValue;
	private int totalTransfers;
	@TableField(fill = FieldFill.INSERT)
	private String createTime;
	@TableField(fill = FieldFill.UPDATE)
	private String updateTime;

}

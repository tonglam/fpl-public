package com.tong.fpl.db.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Create by tong on 2020/6/11
 */
@Data
@Accessors(chain = true)
@TableName("entry_info")
public class EntryInfoEntity {

	@TableId
	private int entry;
	private String entryName;
	private String playerName;
	private int rank;
	private int lastRank;
	@TableField(fill = FieldFill.UPDATE)
	private Date updatTime;

}

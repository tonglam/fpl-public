package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/5/2
 */
@Data
@Accessors(chain = true)
@TableName(value = "lottery")
public class LotteryEntity {

    @TableId
    private Integer id;
    private String name;
    private String creator;
    private String prizeMap;
    private String entry;
    private String result;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String createTime;

}

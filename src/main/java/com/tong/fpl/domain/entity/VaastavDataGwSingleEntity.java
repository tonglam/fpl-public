package com.tong.fpl.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Created by tong on 2022/07/07
 */
@Data
@Accessors(chain = true)
@TableName("vaastav_data_gw_single")
public class VaastavDataGwSingleEntity {

    private Integer id;
    private String name;
    private String position;
    private String team;
    @TableField(value = "`xP`")
    private String xp;
    private Integer assist;
    private Integer bonus;
    private Integer bps;
    private Integer cleanSheets;
    private String creativity;
    private Integer element;
    private Integer fixture;
    private Integer goalsConceded;
    private Integer goalsScored;
    private String ictIndex;
    private String influence;
    private String kickoffTime;
    private Integer minutes;
    private Integer opponentTeam;
    private Integer ownGoals;
    private Integer penaltiesMissed;
    private Integer penaltiesSaved;
    private Integer redCards;
    private Integer round;
    private Integer saves;
    private Integer selected;
    private Integer teamAScore;
    private Integer teamHScore;
    private String threat;
    private Integer totalPoints;
    private Integer transfersBalance;
    private Integer transfersIn;
    private Integer transfersOut;
    private Integer value;
    private String wasHome;
    private Integer yellowCards;

}

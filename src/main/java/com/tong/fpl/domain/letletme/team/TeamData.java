package com.tong.fpl.domain.letletme.team;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/7/26
 */
@Data
@Accessors(chain = true)
public class TeamData {

    private int id;
    private int code;
    private String name;
    private String shortName;

}

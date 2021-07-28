package com.tong.fpl.domain.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2021/7/28
 */
@Data
@Accessors(chain = true)
public class PlayerSelectData {

    private int element;
    private String webName;
    private String teamName;
    private String teamShortName;
    private String selectByPercent;
    private String eoByPercent;

}

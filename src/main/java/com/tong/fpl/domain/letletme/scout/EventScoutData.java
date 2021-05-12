package com.tong.fpl.domain.letletme.scout;

import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Create by tong on 2020/12/7
 */
@Data
@Accessors(chain = true)
public class EventScoutData {

    private int id;
    private int event;
    private int entry;
    private String scoutName;
    private PlayerInfoData gkpInfo;
    private PlayerInfoData defInfo;
    private PlayerInfoData midInfo;
    private PlayerInfoData fwdInfo;
    private PlayerInfoData captainInfo;
    private String reason;
    private int eventPoints;
    private int totalPoints;
}

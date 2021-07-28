package com.tong.fpl.domain.letletme.league;

import com.tong.fpl.domain.letletme.player.PlayerSelectData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2020/9/18
 */
@Data
@Accessors(chain = true)
public class LeagueEventSelectData {

    private int event;
    private String name;
    private List<PlayerSelectData> captainSelect;
    private List<PlayerSelectData> viceCaptainSelect;
    private List<PlayerSelectData> mostTransferIn;
    private List<PlayerSelectData> mostTransferOut;
    private List<PlayerSelectData> mostSelectPlayer;
    private List<PlayerSelectData> mostSelectTeamGkp;
    private List<PlayerSelectData> mostSelectTeamDef;
    private List<PlayerSelectData> mostSelectTeamMid;
    private List<PlayerSelectData> mostSelectTeamFwd;

}

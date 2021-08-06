package com.tong.fpl.domain.letletme.team;

import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerSummaryData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Create by tong on 2021/8/5
 */
@Data
@Accessors(chain = true)
public class TeamSummaryData {

    private int teamId;
    private String season;
    private String teamName;
    private String teamShortName;
    private List<PlayerSummaryData> playerList;
    private List<PlayerFixtureData> fixtureList;
    private List<String> cornersAndIndirectFreekicksOrders;
    private List<String> directFreekicksOrders;
    private List<String> penaltiesOrders;
    private TeamDetailData detailData;

}

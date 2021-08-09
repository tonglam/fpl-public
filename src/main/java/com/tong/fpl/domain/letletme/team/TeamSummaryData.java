package com.tong.fpl.domain.letletme.team;

import com.tong.fpl.domain.letletme.player.PlayerFixtureData;
import com.tong.fpl.domain.letletme.player.PlayerSummaryData;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

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
    private Map<Integer, List<PlayerSummaryData>> playerMap;
    private List<PlayerFixtureData> fixtureList;
    private List<String> cornersAndIndirectFreekicksOrders;
    private List<String> directFreekicksOrders;
    private List<String> penaltiesOrders;
    private TeamDetailData detailData;

}

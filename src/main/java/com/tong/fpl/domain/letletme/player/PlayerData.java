package com.tong.fpl.domain.letletme.player;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PlayerData {

    private PlayerInfoData infoData;
    private List<PlayerFixtureData> fixtureDataList;
    private PlayerDetailData currentSeason;
    private List<PlayerDetailData> historySeasonList;

}

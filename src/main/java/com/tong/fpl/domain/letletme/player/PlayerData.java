package com.tong.fpl.domain.letletme.player;

import lombok.Data;

import java.util.List;

@Data
public class PlayerData {

	private PlayerInfoData infoData;
	private List<PlayerFixtureData> fixtureDataList;
	private PlayerDetailData currentSeason;
	private List<PlayerDetailData> historySeasonList;

}

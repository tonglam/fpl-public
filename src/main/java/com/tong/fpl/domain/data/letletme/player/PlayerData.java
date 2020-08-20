package com.tong.fpl.domain.data.letletme.player;

import lombok.Data;

import java.util.List;

@Data
public class PlayerData {

	private PlayerInfoData infoData;
	private List<PlayerFixtureData> fixtureDataList;
	private PlayerCurrentData currentSeasonData;
	private List<PlayerHistoryData> historySeasonDataList;

}

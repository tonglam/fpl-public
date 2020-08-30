package com.tong.fpl.service;

import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.PlayerStatEntity;
import com.tong.fpl.domain.letletme.api.EntryEventData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.utils.CommonUtils;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/7/31
 */
public interface IQuerySerivce {

	default Map<Integer, PlayerEntity> qryAllPlayerList() {
		return this.qryAllPlayerList(CommonUtils.getCurrentSeason());
	}

	Map<Integer, PlayerEntity> qryAllPlayerList(String season);

	default Map<Integer, PlayerStatEntity> qryAllPlayerStatList() {
		return this.qryAllPlayerStatList(CommonUtils.getCurrentSeason());
	}

	Map<Integer, PlayerStatEntity> qryAllPlayerStatList(String season);

	default int qryPlayerElementByCode(int code) {
		return this.qryPlayerElementByCode(CommonUtils.getCurrentSeason(), code);
	}

	int qryPlayerElementByCode(String season, int code);

	default int qryPlayerElementByWebName(String webName) throws Exception {
		return this.qryPlayerElementByWebName(CommonUtils.getCurrentSeason(), webName);
	}

	int qryPlayerElementByWebName(String season, String webName) throws Exception;

	EntryEventData qryEntryResult(String season, int entry);

	EntryEventData qryEntryEventResult(String season, int event, int entry);

	List<EventLiveEntity> qryEventLiveAll(String season, int element);

	List<EventLiveEntity> qryEventLive(String season, int event, int element);

	PlayerData qryPlayerData(int element);

	PlayerInfoData initPlayerInfo(PlayerEntity playerEntity);

	List<PlayerInfoData> qryAllPlayers(String season);

	List<Pick> qryPickListFromPicks(String season, String picks);

}

package com.tong.fpl.service;

import com.tong.fpl.domain.entity.*;
import com.tong.fpl.domain.letletme.entry.EntryEventData;
import com.tong.fpl.domain.letletme.entry.EntryEventResultData;
import com.tong.fpl.domain.letletme.entry.EntryPickData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.tournament.TournamentKnockoutResultData;
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

	EntryEventData qryEntryInfoData(String season, int entry);

	List<EntryEventResultData> qryEntryResult(String season, int entry);

	EntryEventResultData qryEntryEventResult(String season, int event, int entry);

	List<EventLiveEntity> qryEventLiveAll(String season, int element);

	List<EventLiveEntity> qryEventLive(String season, int event, int element);

	PlayerData qryPlayerData(int element);

	PlayerInfoData initPlayerInfo(String season, PlayerEntity playerEntity);

	List<PlayerInfoData> qryAllPlayers(String season);

	List<EntryPickData> qryPickListFromPicks(String season, String picks);

	EntryInfoEntity qryEntryInfo(int entry);

	TournamentInfoEntity qryTournamentInfoById(int tournamentId);

	List<TournamentKnockoutEntity> qryKnockoutListByTournamentId(int tournamentId);

	List<TournamentKnockoutResultData> qryKnockoutResultByTournament(int tournamentId);

}

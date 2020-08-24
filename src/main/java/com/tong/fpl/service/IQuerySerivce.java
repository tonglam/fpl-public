package com.tong.fpl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.domain.data.letletme.api.EntryEventData;
import com.tong.fpl.domain.data.letletme.player.PlayerData;
import com.tong.fpl.domain.data.letletme.player.PlayerQueryParam;
import com.tong.fpl.domain.data.letletme.player.PlayerValueData;
import com.tong.fpl.domain.entity.EventEntity;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.TeamEntity;

import java.util.List;

/**
 * Create by tong on 2020/7/31
 */
public interface IQuerySerivce {

	TeamEntity qryTeamEntityByTeamId(int teamId);

	TeamEntity qryTeamEntityByTeamId(String season, int teamId);

	EventEntity qryEventEntityByEvent(int event);

	EventEntity qryEventEntityByEvent(String season, int event);

	PlayerEntity qryPlayerEntityByElement(int element);

	PlayerEntity qryPlayerEntityByElement(String season, int element);

	List<PlayerValueData> qryDayChangePlayerValue(String changeDate);

	EntryEventData qryEntryResult(String season, int entry);

	EntryEventData qryEntryEventResult(String season, int event, int entry);

	List<EventLiveEntity> qryEventLiveAll(String season, int element);

	List<EventLiveEntity> qryEventLive(String season, int event, int element);

	PlayerData qryPlayerData(PlayerQueryParam playerQueryParam) throws Exception;

	Page<PlayerData> qryPagePlayerDataList(long current, long size);

	List<PlayerData> qryAllPlayers(String season);

}

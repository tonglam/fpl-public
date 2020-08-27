package com.tong.fpl.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tong.fpl.domain.data.letletme.api.EntryEventData;
import com.tong.fpl.domain.data.letletme.player.PlayerData;
import com.tong.fpl.domain.data.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.data.userpick.Pick;
import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.entity.PlayerEntity;
import com.tong.fpl.domain.entity.PlayerStatEntity;

import java.util.List;
import java.util.Map;

/**
 * Create by tong on 2020/7/31
 */
public interface IQuerySerivce {

    Map<Integer, PlayerEntity> qryAllPlayerList(String season);

    Map<Integer, PlayerStatEntity> qryAllPlayerStatList(String season);

    int qryPlayerElementByCode(String season, int code);

    int qryPlayerElementByWebName(String season, String webName) throws Exception;

    EntryEventData qryEntryResult(String season, int entry);

    EntryEventData qryEntryEventResult(String season, int event, int entry);

    List<EventLiveEntity> qryEventLiveAll(String season, int element);

    List<EventLiveEntity> qryEventLive(String season, int event, int element);

    PlayerData qryPlayerData(int element);

    Page<PlayerData> qryPagePlayerDataList(long current, long size);

    List<PlayerInfoData> qryAllPlayers(String season);

    List<Pick> qryPickListFromPicks(String season, String picks);

}

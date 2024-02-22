package com.tong.fpl.letletmeApi;

import com.tong.fpl.domain.entity.EventLiveEntity;
import com.tong.fpl.domain.letletme.entry.EntryEventData;
import com.tong.fpl.domain.letletme.player.PlayerData;
import com.tong.fpl.domain.letletme.player.PlayerInfoData;
import com.tong.fpl.domain.letletme.player.PlayerQueryParam;

import java.util.List;

/**
 * Create by tong on 2020/7/20
 */
public interface IHttpApi {

    EntryEventData qryEntryResult(String season, int entry);

    EntryEventData qryEntryEventResult(String season, int event, int entry);

    List<EventLiveEntity> qryEventLiveAll(String season, int element);

    EventLiveEntity qryEventLive(String season, int event, int element);

    PlayerData qryPlayerData(PlayerQueryParam playerQueryParam) throws Exception;

    List<PlayerInfoData> qryAllPlayers(String season);

    String getUtcDeadlineByEvent(int event);

    int getCurrentEvent();

    int getNextEvent();

}

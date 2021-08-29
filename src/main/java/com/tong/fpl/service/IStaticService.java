package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;

import java.util.List;

/**
 * Create by tong on 2020/6/29
 */
public interface IStaticService {

    StaticRes getBootstrapStatic();

    List<EventFixturesRes> getEventFixture(int event);

    UserPicksRes getUserPicks(int event, int entry);

    List<UserTransfersRes> getUserTransfers(int entry);

    UserHistoryRes getUserHistory(int entry);

    EntryRes getEntry(int entry);

    EntryCupRes getEntryCup(int entry);

    List<EntryInfoData> getNewEntryInfoListFromClassic(int classicId);

    List<EntryInfoData> getNewEntryInfoListFromH2h(int h2hId);

    List<EntryInfoData> getEntryInfoListFromClassic(int classicId);

    List<EntryInfoData> getEntryInfoListFromH2h(int h2hId);

    LeagueInfoData getEntryInfoListFromClassicByLimit(int classicId, int limit);

    LeagueInfoData getEntryInfoListFromH2hByLimit(int h2hId, int limit);

    EventLiveRes getEventLive(int event);

}

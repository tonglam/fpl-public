package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;
import com.tong.fpl.domain.letletme.wechat.AuthSessionData;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2020/6/29
 */
public interface IInterfaceService {

    /**
     * @apiNote fantasy
     */
    Optional<StaticRes> getBootstrapStatic();

    Optional<EntryRes> getEntry(int entry);

    Optional<EntryCupRes> getEntryCup(int entry);

    Optional<UserHistoryRes> getUserHistory(int entry);

    Optional<UserPicksRes> getUserPicks(int event, int entry);

    Optional<LeagueClassicRes> getNewLeaguesClassic(int classicId, int page);

    Optional<LeagueH2hRes> getNewLeagueH2H(int h2hId, int page);

    List<EntryInfoData> getNewEntryInfoListFromClassic(int classicId);

    List<EntryInfoData> getNewEntryInfoListFromH2h(int h2hId);

    Optional<LeagueClassicRes> getLeaguesClassic(int classicId, int page);

    Optional<LeagueH2hRes> getLeagueH2H(int h2hId, int page);

    List<EntryInfoData> getEntryInfoListFromClassic(int classicId);

    List<EntryInfoData> getEntryInfoListFromH2h(int h2hId);

    LeagueInfoData getEntryInfoListFromClassicByLimit(int classicId, int limit);

    LeagueInfoData getEntryInfoListFromH2hByLimit(int h2hId, int limit);

    Optional<EventLiveRes> getEventLive(int event);

    Optional<List<EventFixturesRes>> getEventFixture(int event);

    Optional<List<UserTransfersRes>> getUserTransfers(int entry);

    Optional<InputStream> getPlayerPicture(int code);

    /**
     * @apiNote fpl-data
     */
    Optional<AuthSessionData> getAuthSessionInfo(String appId, String secretId, String code);

}

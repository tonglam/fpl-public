package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.*;

import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2020/6/29
 */
public interface IInterfaceService {

	Optional<String> getPlProfileViaLogin(String username, String password);

	Optional<UserPicksRes> getUserPicks(int entry, int event);

	Optional<UserHistoryRes> getUserHistory(int entry);

	Optional<LeagueClassicRes> getLeaguesClassic(int classicId, int page);

	Optional<LeagueH2hRes> getH2HClassic(int classicId, int page);

	Optional<EventLiveRes> getEventLive(int event);

	Optional<StaticRes> getBootstrapStaic();

	Optional<List<FixturesRes>> getFixturesInfo(int event);

}

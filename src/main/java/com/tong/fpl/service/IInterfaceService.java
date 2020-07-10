package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.*;

import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2020/6/29
 */
public interface IInterfaceService {

	Optional<String> getPlProfileViaLogin(String username, String password);

	Optional<EntryRes> getEntry(int entry);

	Optional<UserPicksRes> getUserPicks(int entry, int event);

	Optional<UserHistoryRes> getUserHistory(int entry);

	Optional<LeagueClassicRes> getLeaguesClassic(int classicId, int page);

	Optional<LeagueH2hRes> getH2HClassic(int classicId, int page);

	Optional<EventLiveRes> getEventLive(int event);

	Optional<List<EventFixturesRes>> getEventFixture(int event);

	Optional<StaticRes> getBootstrapStaic();

	Optional<ElementSummaryRes> getElementSummary(int element);

}

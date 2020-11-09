package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.*;

import java.util.List;
import java.util.Optional;

/**
 * http calls
 * <p>
 * Create by tong on 2020/6/29
 */
public interface IInterfaceService {

	Optional<EntryRes> getEntry(int entry);

	Optional<UserPicksRes> getUserPicks(int event, int entry);

	Optional<UserHistoryRes> getUserHistory(int entry);

	Optional<LeagueClassicRes> getLeaguesClassic(int classicId, int page);

	Optional<LeagueH2hRes> getLeagueH2H(int h2hId, int page);

	Optional<EventLiveRes> getEventLive(int event);

	Optional<List<EventFixturesRes>> getEventFixture(int event);

	Optional<StaticRes> getBootstrapStatic();

	Optional<ElementSummaryRes> getElementSummary(int element);

}

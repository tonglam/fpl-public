package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.domain.letletme.entry.EntryInfoData;
import com.tong.fpl.domain.letletme.league.LeagueInfoData;

import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2020/6/29
 */
public interface IStaticService {

	List<EntryInfoData> getEntryInfoListFromClassic(int classicId);

	List<EntryInfoData> getEntryInfoListFromH2h(int h2hId);

	LeagueInfoData getEntryInfoListFromClassicByLimit(int classicId, int limit);

	LeagueInfoData getEntryInfoListFromH2hByLimit(int h2hId, int limit);

	Optional<UserHistoryRes> getUserHistory(int entry);

	Optional<EntryRes> getEntry(int entry);

	Optional<EntryCupRes> getEntryCup(int entry);

	Optional<UserPicksRes> getUserPicks(int event, int entry);

	Optional<List<TransferRes>> getTransfer(int entry);

}

package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.*;
import com.tong.fpl.domain.entity.EntryInfoEntity;

import java.util.List;
import java.util.Optional;

/**
 * Create by tong on 2020/6/29
 */
public interface IStaticSerive {

    void insertAverageEventResult(int event, StaticRes staticRes);

    List<EntryInfoEntity> getEntryInfoListFromClassic(int classicId);

    List<EntryInfoEntity> getEntryInfoListFromH2h(int h2hId);

    List<EntryInfoEntity> getEntryInfoListFromH2hByPage(int h2hId, int page);

    Optional<UserPicksRes> getUserPicks(int event, int entry);

    Optional<UserHistoryRes> getUserHistory(int entry);

    Optional<ElementSummaryRes> getElementSummary(int element);

    Optional<EntryRes> getEntry(int entry);

}

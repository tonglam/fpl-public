package com.tong.fpl.service;

import com.tong.fpl.domain.data.response.ElementSummaryRes;
import com.tong.fpl.domain.data.response.EntryRes;
import com.tong.fpl.domain.data.response.UserHistoryRes;
import com.tong.fpl.domain.entity.EntryInfoEntity;

import java.util.List;

/**
 * Create by tong on 2020/6/29
 */
public interface IStaticSerive {

	void insertPlayers();

	void insertEvent();

	void insertPlayerValue();

	void insertBaseData(int event);

	void insertEventFixture(int event);

	void insertEventLive(int event);

	List<EntryInfoEntity> getEntryInfoListFromClassic(int classicId);

	List<EntryInfoEntity> getEntryInfoListFromH2h(int h2hId);

	UserHistoryRes getUserHistory(int entry);

	ElementSummaryRes getElementSummary(int element);

	EntryRes getEntry(int entry);

}

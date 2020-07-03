package com.tong.fpl.service;

import com.tong.fpl.domain.entity.EntryInfoEntity;

import java.util.List;

/**
 * Create by tong on 2020/6/29
 */
public interface IStaticSerive {

	void insertTeam();

	void insertPlayers();

	void insertEvent();

	void insertBaseData(int event);

	void insertEventLive(int event);

	List<EntryInfoEntity> getEntryInfoListFromClassic(int classicId);

	List<EntryInfoEntity> getEntryInfoListFromH2h(int h2hId);

}

package com.tong.fpl.service;

/**
 * Create by tong on 2020/8/23
 */
public interface ICacheSerive {

	void insertTeam();

	void insertHisTeam(String season);

	void insertEvent();

	void insertHisEvent(String season);

	void insertEventFixture();

	void insertHisEventFixture(String season);

	void insertPlayer();

	void insertHisPlayer(String season);

	void insertPlayerStat();

	void insertHisPlayerStat(String season);

	void insertPlayerValue();

}
